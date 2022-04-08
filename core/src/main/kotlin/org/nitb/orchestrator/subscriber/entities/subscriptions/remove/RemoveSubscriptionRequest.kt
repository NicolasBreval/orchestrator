package org.nitb.orchestrator.subscriber.entities.subscriptions.remove

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractRequest
import java.io.Serializable

/**
 * Request sent to a subscriber to remove subscriptions by their names.
 *
 * @property subscriptions List of subscription names to remove
 * @param id Identifier to distinguish this request
 */
@NoArgsConstructor
class RemoveSubscriptionRequest(
    val subscriptions: List<String>,
    id: String
): AbstractRequest(id), Serializable