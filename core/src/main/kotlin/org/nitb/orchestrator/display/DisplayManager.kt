package org.nitb.orchestrator.display

import com.fasterxml.jackson.core.type.TypeReference
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
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

class DisplayManager(
    private val name: String = ConfigManager.getProperty(ConfigNames.DISPLAY_NODE_NAME, RuntimeException("You must set a value for ${ConfigNames.DISPLAY_NODE_NAME} property"))
): AmqpManager<Serializable>, AmqpConsumer<Serializable>, AmqpSender {

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
        val httpClient = HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscribers/list")

        httpClient.basicRequest("GET").use { response ->
            return JSONSerializer.deserialize(response.body!!.string(), object : TypeReference<Map<String, SubscriberInfo>>() {})
        }
    }

    fun listSubscriptions(): List<SubscriptionInfo> {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/list")
            .jsonRequest("GET", object: TypeReference<List<SubscriptionInfo>>() {})
    }

    fun getSubscriptionInfo(name: String): SubscriptionInfo {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscription/info", params = mapOf("name" to listOf(name)))
            .jsonRequest("GET", SubscriptionInfo::class.java)
    }

    fun getLogs(name: String, count: Int): List<String> {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscription/logs",
            params = mapOf("name" to listOf(name), "count" to listOf("$count")))
            .jsonRequest("GET", object: TypeReference<List<String>>() {})
    }

    fun subscriptionInfo(name: String): SubscriptionInfo {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/list",
            params = mapOf("subscription" to listOf(name))).jsonRequest("GET", SubscriptionInfo::class.java)
    }

    fun setSubscriptions(subscriptions: List<String>, stop: Boolean): SubscriptionOperationResponse {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/${if (stop) "stop" else "start"}",
            params = mapOf("subscriptions" to subscriptions)).jsonRequest("GET", SubscriptionOperationResponse::class.java)
    }

    fun addSubscriptions(request: UploadSubscriptionsRequest): SubscriptionOperationResponse {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/upload")
            .jsonRequest("PUT", request, SubscriptionOperationResponse::class.java)
    }

    fun removeSubscriptions(subscriptions: List<String>): SubscriptionOperationResponse {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/delete",
            params = mapOf("subscriptions" to subscriptions)).jsonRequest("DELETE", SubscriptionOperationResponse::class.java)
    }

    fun handleSubscription(name: String, message: DirectMessage<*>): Any {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/handle", params = mapOf("name" to listOf(name)))
            .jsonRequest("POST", message, Any::class.java)
    }

    fun getSubscriptionSchemas(): Map<String, String?> {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/schemas")
            .jsonRequest("GET", object: TypeReference<Map<String, String?>>() {})
    }

    fun getSubscriptionHistorical(name: String): List<SubscriptionSerializableEntry> {
        return HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscription/historical", params = mapOf("name" to listOf(name)))
            .jsonRequest("GET", object: TypeReference<List<SubscriptionSerializableEntry>>() {})
    }

    private val logger = LoggingManager.getLogger("display.node")
    private val client = createClient(name)

    private lateinit var mainNode: SubscriberInfo

    init {
        start()
    }
}