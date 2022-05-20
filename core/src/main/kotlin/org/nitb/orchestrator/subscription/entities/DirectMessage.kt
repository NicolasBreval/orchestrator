package org.nitb.orchestrator.subscription.entities

import io.swagger.v3.oas.annotations.media.Schema
import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@Schema(description = "Request used to invoke a subscription's handler.")
@NoArgsConstructor
class DirectMessage(
    @Schema(description = "Name of handler to invoke")
    val handler: String,
    @Schema(description = "Options to pass as parameters to handler")
    val options: Map<String, Any?>
): Serializable