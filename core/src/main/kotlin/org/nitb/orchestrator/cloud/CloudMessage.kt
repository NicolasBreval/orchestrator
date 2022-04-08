package org.nitb.orchestrator.cloud

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable
import java.math.BigInteger

/**
 * Entity which defines a message sent from a Subscription to another by a queue.
 *
 * @property sender Name of Subscription who sent the message.
 * @property message Content of message.
 * @property timestamp Date when message was created.
 * @property size Size of message, in bytes
 */
@NoArgsConstructor
data class CloudMessage<T: Serializable>(
    val sender: String,
    val message: T,
    val timestamp: Long = System.currentTimeMillis(),
    var size: BigInteger = BigInteger.ZERO
): Serializable