package org.nitb.orchestrator.display

import com.fasterxml.jackson.core.type.TypeReference
import io.micronaut.http.client.exceptions.EmptyResponseException
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.http.HttpClient
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
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

        httpClient.jsonRequest("GET").use { response ->
            return JSONSerializer.deserialize(response.body!!.string(), object : TypeReference<Map<String, SubscriberInfo>>() {})
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun listSubscriptions(): List<SubscriptionInfo> {
        val httpClient = HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/list")

        httpClient.jsonRequest("GET").use { response ->
            return JSONSerializer.deserialize(response.body!!.string(), object: TypeReference<List<SubscriptionInfo>>() {})

        }
    }

    fun subscriptionInfo(name: String): SubscriptionInfo {
        val httpClient = HttpClient("http://${mainNode.hostname}:${mainNode.httpPort}/subscriptions/list", params = mapOf("subscription" to listOf(name)))

        return httpClient.jsonRequest("GET", SubscriptionInfo::class.java)
    }


    private val logger = LoggingManager.getLogger("display.node")
    private val client = createClient(name)

    private lateinit var mainNode: SubscriberInfo

    init {
        start()
    }
}