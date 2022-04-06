package org.nitb.orchestrator.web

import io.micronaut.context.annotation.Parameter
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import org.nitb.orchestrator.cloud.CloudConsumer
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.remove.RemoveSubscriptionRequest
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionsRequest
import org.nitb.orchestrator.web.entities.AddSubscriptionRequest
import org.nitb.orchestrator.web.entities.PendingResponse
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class BaseController: CloudManager<Serializable>, CloudSender, CloudConsumer<Serializable> {

    @Get("/subscriptions/list")
    fun listSubscriptions(): List<String> {
        return nodes.flatMap { (_, info) -> info.subscriptions.values }.map { it.content }
    }

    @Put("/subscriptions/add")
    fun addSubscriptions(
        @Body request: AddSubscriptionRequest
    ): String {
        val id = UUID.randomUUID().toString()
        client.send(masterNodeName, UploadSubscriptionsRequest(request.subscriptions, request.subscriber, id))
        pendingMessages[id] = PendingResponse(id)
        return id
    }

    @Delete("/subscriptions/delete")
    fun removeSubscriptions(
        @Parameter subscriptions: List<String>
    ): String {
        val id = UUID.randomUUID().toString()
        client.send(masterNodeName, RemoveSubscriptionRequest(subscriptions, id))
        pendingMessages[id] = PendingResponse(id)
        return id
    }

    @Get("/operations/check")
    fun checkPendingResponse(
        @Parameter id: String
    ): Boolean {
        return if (pendingMessages[id]?.received == true) {
            pendingMessages.remove(id)?.received ?: false
        } else false
    }

    private val masterNodeName = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("No mandatory property found: ${ConfigNames.PRIMARY_NAME}"))

    private val nodes = ConcurrentHashMap<String, SubscriberInfo>()

    private val pendingMessages = ConcurrentHashMap<String, PendingResponse>()

    private val client by lazy {
        val iClient = createClient(ConfigManager.getProperty(ConfigNames.CONTROLLER_NAME, RuntimeException("No mandatory property found: ${ConfigNames.CONTROLLER_NAME}")))

        registerConsumer(iClient) { cloudMessage ->
            when (cloudMessage.message) {
                is SubscriberInfo -> {
                    nodes[(cloudMessage.message as SubscriberInfo).name] = cloudMessage.message as SubscriberInfo
                }
            }
        }

        iClient
    }

}