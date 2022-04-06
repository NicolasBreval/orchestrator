package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.transformers.DummyTransformer

@NoArgsConstructor
class SubscriptionReceiver(
    val name: String,
    val transformer: String = DummyTransformer::class.java.name
)