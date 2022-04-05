package org.nitb.orchestrator.web.entities

class PendingResponse(
    val id: String,
    val received: Boolean = false,
    val creation: Long = System.currentTimeMillis()
)