package org.nitb.orchestrator.subscriber.entities.subscriptions.upload

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import java.io.Serializable

@NoArgsConstructor
class UploadSubscriptionResponse(
    val status: ResponseStatus,
    id: String
): AbstractResponse(id), Serializable