package org.nitb.orchestrator.subscriber.entities.subscriptions.upload

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import java.io.Serializable

@NoArgsConstructor
class UploadSubscriptionResponse(
    val status: ResponseStatus,
    val subscriptions: List<SubscriptionInfo>,
    id: String
): AbstractResponse(id), Serializable