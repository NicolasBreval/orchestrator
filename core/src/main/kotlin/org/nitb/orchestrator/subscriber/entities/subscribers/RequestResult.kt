package org.nitb.orchestrator.subscriber.entities.subscribers

class RequestResult (
    val status: RequestStatus,
    val message: String? = null,
    val parent: String? = null,
    val creation: Long = System.currentTimeMillis()
) {
    companion object {
        fun mergeResults(a: RequestResult, b: RequestResult, messages: Map<RequestStatus, String> = mapOf()): RequestResult {

            val status: RequestStatus = if (a.status == RequestStatus.DELETED || b.status == RequestStatus.DELETED) {
                RequestStatus.DELETED
            } else if (a.status == RequestStatus.WAITING || b.status == RequestStatus.WAITING) {
                RequestStatus.WAITING
            } else if (a.status == RequestStatus.ERROR || b.status == RequestStatus.ERROR) {
                RequestStatus.ERROR
            } else {
                RequestStatus.OK
            }

            return RequestResult(status, messages[status], a.parent)
        }
    }
}