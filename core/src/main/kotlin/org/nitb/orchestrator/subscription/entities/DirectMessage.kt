package org.nitb.orchestrator.subscription.entities

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
class DirectMessage(
    val handler: String,
    val options: Map<String, Any?>
): Serializable