package org.nitb.orchestrator.subscriber.entities.subscriptions

import io.swagger.v3.oas.annotations.media.Schema
import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@Schema(description = "Response related to an operation applied to one or many subscriptions, like start, stop, upload or delete.")
@NoArgsConstructor
class SubscriptionOperationResponse(
    @Schema(description = "Type of operation applied.")
    val type: RequestType,
    @Schema(description = "Message to summarise response")
    val message: String? = null,
    @Schema(description = "List of subscriptions successfully modified")
    val modified: List<String> = listOf(),
    @Schema(description = "List of subscription that couldn't be modified.")
    val notModified: List<String> = listOf(),
    @Schema(description = "Result of operation based in number of modified and not modified subscriptions.")
    val result: SubscriptionOperationResult = if (notModified.isEmpty()) SubscriptionOperationResult.TOTAL
    else if (modified.isEmpty() && notModified.isNotEmpty()) SubscriptionOperationResult.NONE else SubscriptionOperationResult.PARTIAL
): Serializable