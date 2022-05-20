package org.nitb.orchestrator.subscriber.entities.subscriptions

import io.swagger.v3.oas.annotations.media.Schema
import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.subscription.SubscriptionStatus
import java.io.Serializable
import java.math.BigInteger

@Schema(description = "Information about a subscription registered in a subscriber of a cluster.")
@NoArgsConstructor
class SubscriptionInfo (
    @Schema(description = "Unique name related to subscription.")
    val name: String,
    @Schema(description = "Current status of subscription.")
    val status: SubscriptionStatus,
    @Schema(description = "Creation timestamp, in milliseconds, of this entity.")
    val creation: Long = System.currentTimeMillis(),
    @Schema(description = "Input messages volume amount received by subscription.")
    val inputVolume: BigInteger = BigInteger.ZERO,
    @Schema(description = "Output messages volume amount produced by subscription to another.")
    val outputVolume: BigInteger = BigInteger.ZERO,
    @Schema(description = "Number of manual starts of a subscription.")
    val starts: BigInteger = BigInteger.ZERO,
    @Schema(description = "Number of manual stops of a subscription.")
    val stops: BigInteger = BigInteger.ZERO,
    @Schema(description = "Number of success executions.")
    val success: BigInteger = BigInteger.ZERO,
    @Schema(description = "Number of error executions.")
    val error: BigInteger = BigInteger.ZERO,
    @Schema(description = "Last timestamp when subscription will be executed.")
    val lastExecution: Long = -1,
    @Schema(description = "JSON schema related to subscription, to be used as template.")
    val schema: String? = null,
    @Schema(description = "JSON schema related to subscription with filled parameters.")
    val content: String = ""
): Serializable