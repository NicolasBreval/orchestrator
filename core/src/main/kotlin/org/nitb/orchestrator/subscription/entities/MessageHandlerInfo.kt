package org.nitb.orchestrator.subscription.entities

class MessageHandlerRequest(
    val type: String,
    val description: String
): java.io.Serializable

class MessageHandlerInfo(
    val requests: List<MessageHandlerRequest>,
    val description: String
): java.io.Serializable