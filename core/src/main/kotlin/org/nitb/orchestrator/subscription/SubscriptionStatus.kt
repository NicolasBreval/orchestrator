package org.nitb.orchestrator.subscription

enum class SubscriptionStatus(
    val isStoppedStatus: Boolean
) {
    IDLE(false),
    RUNNING(false),
    STOPPED(false)
}