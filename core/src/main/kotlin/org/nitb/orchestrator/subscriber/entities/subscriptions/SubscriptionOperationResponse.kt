package org.nitb.orchestrator.subscriber.entities.subscriptions

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
class SubscriptionOperationResponse(
    val type: RequestType,
    val message: String? = null,
    val modified: List<String> = listOf(),
    val notModified: List<String> = listOf(),
    val result: SubscriptionOperationResult = if (notModified.isEmpty()) SubscriptionOperationResult.TOTAL
    else if (modified.isEmpty() && notModified.isNotEmpty()) SubscriptionOperationResult.NONE else SubscriptionOperationResult.PARTIAL
): Serializable