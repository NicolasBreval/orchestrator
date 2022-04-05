package org.nitb.orchestrator.web.entities

import org.nitb.orchestrator.annotations.NoArgsConstructor

@NoArgsConstructor
class AddSubscriptionRequest(
    val subscriptions: List<String>,
    val subscriber: String? = null
)