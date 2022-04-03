package org.nitb.orchestrator.subscriber.entities.subscriptions.remove

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
class RemoveSubscriptionResponse(
    val status: RemoveSubscriptionStatus,
    val id: String
): Serializable