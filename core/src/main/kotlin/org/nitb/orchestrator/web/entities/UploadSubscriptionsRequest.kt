package org.nitb.orchestrator.web.entities

class UploadSubscriptionsRequest(
    val subscriptions: List<String>,
    val subscriber: String? = null
)