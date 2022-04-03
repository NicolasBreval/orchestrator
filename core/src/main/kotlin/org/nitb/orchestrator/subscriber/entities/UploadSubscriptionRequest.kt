package org.nitb.orchestrator.subscriber.entities

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
class UploadSubscriptionsReq(
    val subscriptions: List<String>,
    val subscriber: String? = null,
    val id: String
): Serializable