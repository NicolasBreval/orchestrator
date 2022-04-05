package org.nitb.orchestrator.subscriber.entities.subscriptions.status

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import java.io.Serializable

@NoArgsConstructor
class SetSubscriptionsResponse(
    val status: ResponseStatus,
    id: String
): AbstractResponse(id), Serializable