package org.nitb.orchestrator.subscriber.entities.subscriptions.remove

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import java.io.Serializable

@NoArgsConstructor
class RemoveSubscriptionResponse(
    val status: ResponseStatus,
    id: String,
    val parentId: String? = null
): AbstractResponse(id), Serializable