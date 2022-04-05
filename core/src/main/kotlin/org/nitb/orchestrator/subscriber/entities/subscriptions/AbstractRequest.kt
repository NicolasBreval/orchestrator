package org.nitb.orchestrator.subscriber.entities.subscriptions

import java.io.Serializable
import java.util.*

abstract class AbstractRequest(
    val id: String = UUID.randomUUID().toString()
): Serializable