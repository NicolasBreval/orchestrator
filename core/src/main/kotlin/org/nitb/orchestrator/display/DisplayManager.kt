package org.nitb.orchestrator.display

import com.fasterxml.jackson.core.type.TypeReference
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
import org.nitb.orchestrator.subscription.SubscriptionStatus
import org.nitb.orchestrator.subscription.entities.DirectMessage
import org.nitb.orchestrator.web.entities.UploadSubscriptionsRequest
import java.io.Serializable
import java.lang.RuntimeException
import kotlin.system.exitProcess

/**
 * Object used to start controller used by display node. This object contains all required logic to retrieve worker nodes
 * information and make requests to them.
 */
object DisplayManager: AmqpManager<Serializable>, AmqpConsumer<Serializable>, AmqpSender {

    /**
     * Name of display node, taken from properties file. If property [ConfigNames.DISPLAY_NODE_NAME] doesn't exists, throws a RuntimeException.
     */
    private val name: String = ConfigManager.getProperty(ConfigNames.DISPLAY_NODE_NAME, RuntimeException("You must set a value for ${ConfigNames.DISPLAY_NODE_NAME} property"))

    /**
     * Initializes this object. When display node is initialized, registers a new consumer to attach all worker nodes connected
     * to same AMQP server and registers them in their memory map.
     */
    fun start() {
        try {
            client.purge()

            registerConsumer(client) { message ->
                when (message.message) {
                    is SubscriberInfo -> {
                        logger.debug("New subscriber info received")

                        if (!this::mainNode.isInitialized || mainNode != message.message) {
                            logger.info("New main node registered: ${message.message.name}")
                            mainNode = message.message
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            exitProcess(1)
        }
    }

    /**
     * Disconnects the display node.
     */
    fun stop() {
        client.close()
    }

    /**
     * Used to retrieve all subscribers connected to orchestrator's cluster.
     *
     * @return List of subscribers retrieved from main worker node using HTTP client.
     */
    @Suppress("UNCHECKED_CAST")
    fun listSubscribers(): Map<String, SubscriberInfo> {
        val url = "${mainNodeUri}/subscribers/list"
        logger.debug("Making request to $url")
        HttpClient(url).basicRequest("GET").use { response ->
            return JSONSerializer.deserialize(response.body!!.string(), object : TypeReference<Map<String, SubscriberInfo>>() {})
        }
    }

    fun listSubscriptions(): List<SubscriptionInfo> {
        return if (!this::mainNode.isInitialized) {
            listOf()
        } else {
            val url = "${mainNodeUri}/subscriptions/list"
            logger.debug("Making request to $url")
            HttpClient(url)
                .jsonRequest("GET", object: TypeReference<List<SubscriptionInfo>>() {})
        }
    }

    fun getSubscriptionInfo(name: String): SubscriptionInfo {
        val url = "${mainNodeUri}/subscription/info"
        logger.debug("Making request to $url")
        return HttpClient(url, params = mapOf("name" to listOf(name)))
            .jsonRequest("GET", SubscriptionInfo::class.java)
    }

    fun getLogs(name: String, count: Int): List<String> {
        val url = "${mainNodeUri}/subscription/logs"
        logger.debug("Making request to $url")
        return HttpClient(url,
            params = mapOf("name" to listOf(name), "count" to listOf("$count")))
            .jsonRequest("GET", object: TypeReference<List<String>>() {})
    }

    fun downloadLogs(name: String): ByteArray? {
        val url = "${mainNodeUri}/subscription/logs/download"
        HttpClient(url,
            params = mapOf("name" to listOf(name))).basicRequest("GET").use { response ->
                return response.body?.byteStream()?.readBytes()
        }
    }

    fun subscriptionInfo(name: String): SubscriptionInfo {
        val url = "${mainNodeUri}/subscriptions/info"
        logger.debug("Making request to $url")
        return HttpClient(url,
            params = mapOf("subscription" to listOf(name))).jsonRequest("GET", SubscriptionInfo::class.java)
    }

    fun setSubscriptions(subscriptions: List<String>, stop: Boolean): SubscriptionOperationResponse {
        val url = "${mainNodeUri}/subscriptions/${if (stop) "stop" else "start"}"
        logger.debug("Making request to $url")
        return HttpClient(url,
            params = mapOf("subscriptions" to subscriptions)).jsonRequest("GET", SubscriptionOperationResponse::class.java)
    }

    fun addSubscriptions(request: UploadSubscriptionsRequest): SubscriptionOperationResponse {
        val url = "${mainNodeUri}/subscriptions/upload"
        logger.debug("Making request to $url")
        return HttpClient(url)
            .jsonRequest("PUT", request, SubscriptionOperationResponse::class.java)
    }

    fun removeSubscriptions(subscriptions: List<String>): SubscriptionOperationResponse {
        val url = "${mainNodeUri}/subscriptions/delete"
        logger.debug("Making request to $url")
        return HttpClient(url,
            params = mapOf("subscriptions" to subscriptions)).jsonRequest("DELETE", SubscriptionOperationResponse::class.java)
    }

    fun handleSubscription(name: String, message: DirectMessage<*>): Any {
        val url = "${mainNodeUri}/subscriptions/handle"
        logger.debug("Making request to $url")
        return HttpClient(url, params = mapOf("name" to listOf(name)))
            .jsonRequest("POST", message, Any::class.java)
    }

    fun getSubscriptionSchemas(): Map<String, String?> {
        val url = "${mainNodeUri}/subscriptions/schemas"
        logger.debug("Making request to $url")
        return HttpClient(url)
            .jsonRequest("GET", object: TypeReference<Map<String, String?>>() {})
    }

    fun getSubscriptionHistorical(name: String): List<SubscriptionSerializableEntry> {
        val url = "${mainNodeUri}/subscription/historical"
        logger.debug("Making request to $url")
        return HttpClient(url, params = mapOf("name" to listOf(name)))
            .jsonRequest("GET", object: TypeReference<List<SubscriptionSerializableEntry>>() {})
    }

    fun getSubscriptionStatus(names: List<String>): Map<String, SubscriptionStatus> {
        val url = "${mainNodeUri}/subscription/status"
        logger.debug("Making request to $url")
        return HttpClient(url, params = mapOf("names" to listOf(names.joinToString(","))))
            .jsonRequest("GET", object: TypeReference<Map<String, SubscriptionStatus>>() {})
    }

    @Suppress
    val mainNodeUri get() = "http://${mainNode.fixedHost}:${mainNode.httpPort}"

    private val logger = LoggingManager.getLogger("display.node")
    private val client = createClient(name)

    private lateinit var mainNode: SubscriberInfo

}