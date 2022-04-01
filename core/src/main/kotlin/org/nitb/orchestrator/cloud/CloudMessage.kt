package org.nitb.orchestrator.cloud

import java.io.Serializable
import java.math.BigInteger

@org.nitb.orchestrator.annotations.NoArgsConstructor
data class CloudMessage<T: Serializable>(
    val sender: String,
    val message: T,
    val timestamp: Long = System.currentTimeMillis(),
    var size: BigInteger = BigInteger.ZERO
): Serializable