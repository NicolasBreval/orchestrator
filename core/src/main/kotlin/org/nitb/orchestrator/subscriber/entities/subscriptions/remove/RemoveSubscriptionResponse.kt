package org.nitb.orchestrator.subscriber.entities.subscriptions.remove

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import java.io.Serializable

/**
 * Response to a [RemoveSubscriptionRequest].
 *
 * @property status Status related to request.
 * @param id Identifier of original request.
 * @param parentId If this request of this response is part of another request, the parent request is represented with their id.
 */
@NoArgsConstructor
class RemoveSubscriptionResponse(
    val status: ResponseStatus,
    id: String,
    val parentId: String? = null
): AbstractResponse(id), Serializable