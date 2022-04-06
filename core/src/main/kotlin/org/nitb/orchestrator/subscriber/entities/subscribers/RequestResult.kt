package org.nitb.orchestrator.subscriber.entities.subscribers

class RequestResult (
    val status: RequestStatus,
    val message: String? = null,
    val parent: String? = null,
    val creation: Long = System.currentTimeMillis()
)