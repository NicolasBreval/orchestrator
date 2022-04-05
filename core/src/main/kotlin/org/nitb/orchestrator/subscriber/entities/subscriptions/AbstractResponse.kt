package org.nitb.orchestrator.subscriber.entities.subscriptions

import java.io.Serializable
import java.util.*

abstract class AbstractResponse(
    val id: String = UUID.randomUUID().toString()
): Serializable