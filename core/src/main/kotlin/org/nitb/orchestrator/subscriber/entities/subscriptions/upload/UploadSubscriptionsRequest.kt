package org.nitb.orchestrator.subscriber.entities.subscriptions.upload

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractRequest
import java.io.Serializable

@NoArgsConstructor
class UploadSubscriptionsRequest(
    val subscriptions: List<String>,
    val subscriber: String? = null,
    id: String
): AbstractRequest(id), Serializable