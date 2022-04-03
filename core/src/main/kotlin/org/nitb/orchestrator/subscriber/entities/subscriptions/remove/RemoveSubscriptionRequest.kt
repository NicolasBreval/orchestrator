package org.nitb.orchestrator.subscriber.entities.subscriptions.remove

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
class RemoveSubscriptionRequest(
    val subscriptions: List<String>,
    val id: String
): Serializable