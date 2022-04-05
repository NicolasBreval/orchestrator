package org.nitb.orchestrator.subscriber.entities.subscriptions.status

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractRequest
import java.io.Serializable

@NoArgsConstructor
class SetSubscriptionStatusRequest(
    val subscriptions: List<String>,
    val action: SetStatusAction,
    id: String
): AbstractRequest(id), Serializable