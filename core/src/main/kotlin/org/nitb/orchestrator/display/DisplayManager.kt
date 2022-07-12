package org.nitb.orchestrator.display

import com.fasterxml.jackson.core.type.TypeReference
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.entities.SubscriptionSerializableEntry
import org.nitb.orchestrator.http.HttpClient
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionOperationResponse
import org.nitb.orchestrator.subscription.entities.DirectMessage
import org.nitb.orchestrator.web.entities.UploadSubscriptionsRequest
import java.io.Serializable
import java.lang.RuntimeException

object DisplayManager: AmqpManager<Serializable>, AmqpConsumer<Serializable>, AmqpSender {

    private val name: String = ConfigManager.getProperty(ConfigNames.DISPLAY_NODE_NAME, RuntimeException("You must set a value for ${ConfigNames.DISPLAY_NODE_NAME} property"))

    fun start() {
        client.purge()

        registerConsumer(client) { message ->
            when (message.message) {
                is SubscriberInfo -> {
                    if (!this::mainNode.isInitialized || mainNode != message.message) {
                        logger.info("New main node registered: ${message.message.name}")
                        mainNode = message.message
                    }
                }
            }
        }
    }

    fun stop() {
        client.close()
    }

    @Suppress("UNCHECKED_CAST")
    fun listSubscribers(): Map<String, SubscriberInfo> {
        val httpClient = HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscribers/list")

        httpClient.basicRequest("GET").use { response ->
            return JSONSerializer.deserialize(response.body!!.string(), object : TypeReference<Map<String, SubscriberInfo>>() {})
        }
    }

    fun listSubscriptions(): List<SubscriptionInfo> {
        return if (!this::mainNode.isInitialized) {
            listOf()
        } else {
            HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscriptions/list")
                .jsonRequest("GET", object: TypeReference<List<SubscriptionInfo>>() {})
        }
    }

    fun getSubscriptionInfo(name: String): SubscriptionInfo {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscription/info", params = mapOf("name" to listOf(name)))
            .jsonRequest("GET", SubscriptionInfo::class.java)
    }

    fun getLogs(name: String, count: Int): List<String> {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscription/logs",
            params = mapOf("name" to listOf(name), "count" to listOf("$count")))
            .jsonRequest("GET", object: TypeReference<List<String>>() {})
    }

    fun downloadLogs(name: String): ByteArray? {
        HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscription/logs/download",
            params = mapOf("name" to listOf(name))).basicRequest("GET").use { response ->
                return response.body?.byteStream()?.readBytes()
        }
    }

    fun subscriptionInfo(name: String): SubscriptionInfo {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscriptions/info",
            params = mapOf("subscription" to listOf(name))).jsonRequest("GET", SubscriptionInfo::class.java)
    }

    fun setSubscriptions(subscriptions: List<String>, stop: Boolean): SubscriptionOperationResponse {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscriptions/${if (stop) "stop" else "start"}",
            params = mapOf("subscriptions" to subscriptions)).jsonRequest("GET", SubscriptionOperationResponse::class.java)
    }

    fun addSubscriptions(request: UploadSubscriptionsRequest): SubscriptionOperationResponse {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscriptions/upload")
            .jsonRequest("PUT", request, SubscriptionOperationResponse::class.java)
    }

    fun removeSubscriptions(subscriptions: List<String>): SubscriptionOperationResponse {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscriptions/delete",
            params = mapOf("subscriptions" to subscriptions)).jsonRequest("DELETE", SubscriptionOperationResponse::class.java)
    }

    fun handleSubscription(name: String, message: DirectMessage<*>): Any {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscriptions/handle", params = mapOf("name" to listOf(name)))
            .jsonRequest("POST", message, Any::class.java)
    }

    fun getSubscriptionSchemas(): Map<String, String?> {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscriptions/schemas")
            .jsonRequest("GET", object: TypeReference<Map<String, String?>>() {})
    }

    fun getSubscriptionHistorical(name: String): List<SubscriptionSerializableEntry> {
        return HttpClient("http://${mainNode.fixedHost}:${mainNode.httpPort}/subscription/historical", params = mapOf("name" to listOf(name)))
            .jsonRequest("GET", object: TypeReference<List<SubscriptionSerializableEntry>>() {})
    }

    private val logger = LoggingManager.getLogger("display.node")
    private val client = createClient(name)

    private lateinit var mainNode: SubscriberInfo

}