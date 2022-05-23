package org.nitb.orchestrator.web.controllers

import io.micronaut.context.annotation.Parameter
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.subscriber.Subscriber
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionOperationResponse
import org.nitb.orchestrator.subscription.entities.DirectMessage
import org.nitb.orchestrator.web.entities.UploadSubscriptionsRequest
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Controller
class WorkerController {

    @Operation(summary = "Used to fetch all subscribers of same cluster.")
    @ApiResponse(description = "List of subscribers registered in cluster with their system information.")
    @Get("/subscribers/list")
    fun listSubscribers(): Map<String, SubscriberInfo> {
        return subscriber.listSubscribers()
    }

    @Operation(summary = "Used to fetch all subscriptions in all subscribers of same cluster.")
    @ApiResponse(description = "List of subscriptions registered in a subscriber with their system information.")
    @Get("/subscriptions/list")
    fun listSubscriptions(): List<SubscriptionInfo> {
        return subscriber.listSubscriptions()
    }

    @Operation(summary = "Used to fetch information about an specific subscription.")
    @ApiResponse(description = "Information related to a subscription.")
    @Get("/subscriptions/info")
    fun subscriptionInfo(@Parameter subscription: String): SubscriptionInfo? {
        return subscriber.getSubscriptionInfo(subscription)
    }

    @Operation(summary = "Used to stop some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Get("/subscriptions/stop")
    fun stopSubscriptions(@QueryValue subscriptions: List<String>): SubscriptionOperationResponse {
        return subscriber.setSubscriptions(subscriptions, true)
    }

    @Operation(summary = "Used to start some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Get("/subscriptions/start")
    fun startSubscriptions(@QueryValue subscriptions: List<String>): SubscriptionOperationResponse {
        return subscriber.setSubscriptions(subscriptions, false)
    }

    @Operation(summary = "Used to upload some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Put("/subscriptions/upload")
    fun addSubscriptions(@Body request: UploadSubscriptionsRequest): SubscriptionOperationResponse {
        return subscriber.uploadSubscriptions(request.subscriptions, request.subscriber)
    }

    @Operation(summary = "Used to delete some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Delete("/subscriptions/delete")
    fun removeSubscriptions(@QueryValue subscriptions: List<String>): SubscriptionOperationResponse {
        return subscriber.removeSubscriptions(subscriptions)
    }

    @Operation(summary = "Used to invoke subscription handler.")
    @Post("/subscriptions/handle/{name}")
    fun dynamicSubscriptionEndpointPost(@PathVariable("name") name: String, @Body message: DirectMessage): Any? {
        return subscriber.handleSubscriptionMessage(name, message)
    }

    @Get("/subscriber/api/definition")
    fun apiDefinition(): HttpResponse<String> {
        if (swaggerPath == null)
            throw IllegalAccessException()

        return HttpResponse.redirect(URI("/swagger/" + swaggerPath?.toFile()?.name))
    }

    private val log = LoggingManager.getLogger("controller")
    private var swaggerPath: Path?
    private val subscriber = Subscriber()

    init {
        val swaggerLocation = WorkerController::class.java.classLoader.getResource("META-INF/swagger")
        swaggerPath = swaggerLocation?.let { Files.walk(Paths.get(it.file.replaceFirst("/", ""))).filter { file -> Files.isRegularFile(file) }.findFirst().orElse(null) }


        log.info("Subscriber controller initialized!!!")
    }
}