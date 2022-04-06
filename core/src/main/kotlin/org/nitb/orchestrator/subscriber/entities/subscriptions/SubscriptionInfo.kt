package org.nitb.orchestrator.subscriber.entities.subscriptions

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscription.SubscriptionStatus
import java.io.Serializable
import java.math.BigInteger

@NoArgsConstructor
class SubscriptionInfo (
    val name: String,
    val creation: Long,
    val status: SubscriptionStatus,
    val inputVolume: BigInteger,
    val outputVolume: BigInteger,
    val starts: BigInteger,
    val stops: BigInteger,
    val success: BigInteger,
    val error: BigInteger,
    val lastExecution: Long,
    val schema: String?,
    val content: String
): Serializable