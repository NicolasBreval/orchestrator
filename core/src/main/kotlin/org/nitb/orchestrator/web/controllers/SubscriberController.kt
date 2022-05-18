package org.nitb.orchestrator.web.controllers

import io.micronaut.context.annotation.Parameter
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.*
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.subscriber.Subscriber
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionOperationResponse
import org.nitb.orchestrator.subscription.entities.DirectMessage
import org.nitb.orchestrator.web.entities.UploadSubscriptionsRequest

@Controller
class SubscriberController {

    @Get("/subscribers/list")
    fun listSubscribers(): Map<String, SubscriberInfo> {
        return subscriber.listSubscribers()
    }

    @Get("/subscriptions/list")
    fun listSubscriptions(): List<SubscriptionInfo> {
        return subscriber.listSubscriptions()
    }

    @Get("/subscriptions/info")
    fun subscriptionInfo(@Parameter subscription: String): SubscriptionInfo? {
        return subscriber.getSubscriptionInfo(subscription)
    }

    @Get("/subscriptions/stop")
    fun stopSubscriptions(@Body subscriptions: List<String>): SubscriptionOperationResponse {
        return subscriber.setSubscriptions(subscriptions, true)
    }

    @Get("/subscriptions/start")
    fun startSubscriptions(@Body subscriptions: List<String>): SubscriptionOperationResponse {
        return subscriber.setSubscriptions(subscriptions, false)
    }

    @Put("/subscriptions/upload")
    fun addSubscriptions(@Body request: UploadSubscriptionsRequest): SubscriptionOperationResponse {
        return subscriber.uploadSubscriptions(request.subscriptions, request.subscriber)
    }

    @Delete("/subscriptions/delete")
    fun removeSubscriptions(@Parameter subscriptions: List<String>): SubscriptionOperationResponse {
        return subscriber.removeSubscriptions(subscriptions)
    }

    @Post("/subscriptions/handle/{name}")
    fun dynamicSubscriptionEndpointPost(@PathVariable("name") name: String, @Body message: DirectMessage): Any? {
        return subscriber.handleSubscriptionMessage(name, message)
    }

    private val log = LoggingManager.getLogger("controller")
    private val subscriber = Subscriber()

    init {
        log.info("Controller initialized!!!")
    }
}