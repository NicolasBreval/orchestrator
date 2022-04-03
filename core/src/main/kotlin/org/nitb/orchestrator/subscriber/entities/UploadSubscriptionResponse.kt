package org.nitb.orchestrator.subscriber.entities

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
class UploadSubscriptionResponse(
    val status: UploadSubscriptionStatus,
    val id: String
): Serializable