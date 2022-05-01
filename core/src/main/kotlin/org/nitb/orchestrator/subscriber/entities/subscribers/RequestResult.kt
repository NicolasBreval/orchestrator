package org.nitb.orchestrator.subscriber.entities.subscribers

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

/**
 * Object used to check result of a request sent from a subscriber to another
 *
 * @param status Status of request to check if is completed or not.
 * @param message Message related to request status.
 * @param id Identifier of request.
 * @param creation Timestamp where request where created.
 */
@NoArgsConstructor
class RequestResult (
    val status: RequestStatus,
    val sender: String,
    val message: String? = null,
    val id: String? = null,
    val creation: Long = System.currentTimeMillis()
): Serializable {
    companion object {

        /**
         * Obtains two results and merge their status.
         * @param a First result to merge
         * @param b Second result to merge
         * @param messages Custom messages to put for each merged result status. If map is not set, puts null.
         */
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

            return RequestResult(status, a.sender, messages[status], a.id)
        }
    }
}