package org.nitb.orchestrator.web.entities

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request body used to upload new subscriptions.")
class UploadSubscriptionsRequest(
    @Schema(description = "List of subscription schemas to upload.")
    val subscriptions: List<String>,
    @Schema(name = "subscriber", description = "Name of subscriber. This property is only needed if allocation strategy type is fixed.")
    val subscriber: String? = null
)