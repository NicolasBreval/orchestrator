package org.nitb.orchestrator.subscriber.entities.subscriptions.remove

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractRequest
import java.io.Serializable

@NoArgsConstructor
class RemoveSubscriptionRequest(
    val subscriptions: List<String>,
    id: String,
    val parentId: String? = null
): AbstractRequest(id), Serializable