package org.nitb.orchestrator.web.controllers

import io.micronaut.context.annotation.Parameter
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.nitb.orchestrator.database.relational.entities.SubscriptionSerializableEntry
import org.nitb.orchestrator.display.DisplayManager
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionOperationResponse
import org.nitb.orchestrator.subscription.SubscriptionStatus
import org.nitb.orchestrator.subscription.entities.DirectMessage
import org.nitb.orchestrator.web.entities.UploadSubscriptionsRequest
import org.nitb.orchestrator.web.entities.VersionInfo
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

@Controller
class DisplayController {

    @Operation(summary = "Used to fetch all subscribers of same cluster.")
    @ApiResponse(description = "List of subscribers registered in cluster with their system information.")
    @Get("/subscribers/list")
    fun listSubscribers(): Map<String, SubscriberInfo> {
        return DisplayManager.listSubscribers()
    }

    @Operation(summary = "Used to fetch all subscriptions in all subscribers of same cluster.")
    @ApiResponse(description = "List of subscriptions registered in a subscriber with their system information.")
    @Get("/subscriptions/list")
    fun listSubscriptions(): List<SubscriptionInfo> {
        return DisplayManager.listSubscriptions()
    }

    @Operation(summary = "Used to fetch information about an specific subscription.")
    @ApiResponse(description = "Information related to a subscription.")
    @Get("/subscriptions/info")
    fun subscriptionInfo(@Parameter subscription: String): SubscriptionInfo {
        return DisplayManager.subscriptionInfo(subscription)
    }

    @Operation(summary = "Used to stop some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Get("/subscriptions/stop")
    fun stopSubscriptions(@QueryValue subscriptions: List<String>): SubscriptionOperationResponse {
        return DisplayManager.setSubscriptions(subscriptions, true)
    }

    @Operation(summary = "Used to start some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Get("/subscriptions/start")
    fun startSubscriptions(@QueryValue subscriptions: List<String>): SubscriptionOperationResponse {
        return DisplayManager.setSubscriptions(subscriptions, false)
    }

    @Operation(summary = "Used to upload some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Put("/subscriptions/upload")
    fun addSubscriptions(@Body request: UploadSubscriptionsRequest): SubscriptionOperationResponse {
        return DisplayManager.addSubscriptions(request)
    }

    @Operation(summary = "Used to delete some subscriptions.")
    @ApiResponse(description = "Result of operation, with list of subscriptions that have been modified and that have not.")
    @Delete("/subscriptions/delete")
    fun removeSubscriptions(@QueryValue subscriptions: List<String>): SubscriptionOperationResponse {
        return DisplayManager.removeSubscriptions(subscriptions)
    }

    @Operation(summary = "Used to get all available subscription schemas")
    @Get("/subscriptions/schemas")
    fun getSubscriptionSchemas(): Map<String, String?> {
        return DisplayManager.getSubscriptionSchemas()
    }

    @Operation(summary = "Used to get information about a single subscription")
    @Get("/subscription/info")
    fun getSubscriptionInfo(@QueryValue name: String): SubscriptionInfo {
        return DisplayManager.getSubscriptionInfo(name)
    }

    @Operation(summary = "Used to retrieve all log lines of a subscription")
    @Get("/subscription/logs")
    fun getLogs(@QueryValue name: String, @QueryValue(defaultValue = "100") count: Int): List<String> {
        return DisplayManager.getLogs(name, count)
    }

    @Operation(summary = "Used to download log files related to a subscription")
    @Get("/subscription/logs/download")
    fun downloadLogs(@QueryValue name: String): ByteArray {
        return DisplayManager.downloadLogs(name) ?: ByteArray(0)
    }

    @Get("/subscription/historical")
    fun getSubscriptionHistorical(@QueryValue name: String): List<SubscriptionSerializableEntry> {
        return DisplayManager.getSubscriptionHistorical(name)
    }

    @Operation(summary = "Used to invoke subscription handler.")
    @Post("/subscriptions/handle")
    fun dynamicSubscriptionEndpointPost(@QueryValue("name") name: String, @Body message: DirectMessage<*>): Any? {
        return DisplayManager.handleSubscription(name, message)
    }

    @Get("/display/api/definition")
    fun apiDefinition(): String {
        return swaggerConfig ?: "NOT FOUND"
    }

    @Operation(summary = "Used to retrieve environment and version information")
    @Get("/version")
    fun versionInfo(): VersionInfo {
        return VersionInfo()
    }

    @Get("/subscriptions/status")
    fun getSubscriptionStatus(@QueryValue("names") names: List<String>): Map<String, SubscriptionStatus> {
        return DisplayManager.getSubscriptionStatus(names)
    }

    private val logger = LoggingManager.getLogger("controller")
    private var swaggerConfig: String? = null

    init {
        val swaggerLocation = DisplayController::class.java.classLoader.getResource("META-INF/swagger")

        swaggerConfig = if (swaggerLocation?.toURI()?.scheme == "jar") {
            val fileSystem = FileSystems.newFileSystem(swaggerLocation.toURI(), mapOf<String, Any>())
            val path = Files.walk(fileSystem.getPath("META-INF/swagger")).filter { Files.isRegularFile(it) }.findFirst().orElse(null)
            String(Files.readAllBytes(path))
        } else {
            val path = swaggerLocation?.let {
                Files.walk(Paths.get(it.file.replaceFirst("/", ""))).filter { file -> Files.isRegularFile(file) }
                    .findFirst().orElse(null)
            }
            path?.let { String(Files.readAllBytes(it)) }

        }

        logger.info("Display node initialized!!!")
    }
}