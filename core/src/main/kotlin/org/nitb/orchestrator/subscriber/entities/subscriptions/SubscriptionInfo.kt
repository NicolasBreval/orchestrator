package org.nitb.orchestrator.subscriber.entities.subscriptions

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscription.SubscriptionStatus
import java.io.Serializable
import java.math.BigInteger

@NoArgsConstructor
class SubscriptionInfo (
    val name: String,
    val status: SubscriptionStatus,
    val creation: Long = System.currentTimeMillis(),
    val inputVolume: BigInteger = BigInteger.ZERO,
    val outputVolume: BigInteger = BigInteger.ZERO,
    val starts: BigInteger = BigInteger.ZERO,
    val stops: BigInteger = BigInteger.ZERO,
    val success: BigInteger = BigInteger.ZERO,
    val error: BigInteger = BigInteger.ZERO,
    val lastExecution: Long = -1,
    val schema: String? = null,
    val content: String = ""
): Serializable