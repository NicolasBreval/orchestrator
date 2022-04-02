package org.nitb.orchestrator.cloud

import java.io.Serializable
import java.util.function.Consumer

abstract class CloudClient<T: Serializable>(
    protected val name: String
) {

    abstract fun <M: Serializable> send(receiver: String, message: M)

    abstract fun createConsumer(onConsume: Consumer<CloudMessage<T>>)

    abstract fun cancelConsumer()

    abstract fun purge()

    abstract fun close()

    abstract fun masterConsuming(): Boolean
}