package org.nitb.orchestrator.subscription.entities

import io.swagger.v3.oas.annotations.media.Schema
import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@Schema(description = "Request used to invoke a subscription's handler.")
@NoArgsConstructor
open class DirectMessage<T: Serializable>(
    @Schema(description = "Name of handler to invoke.")
    val handler: String,
    @Schema(description = "Information related to handler. Type of this information is specific to each implementation")
    val info: T
): Serializable