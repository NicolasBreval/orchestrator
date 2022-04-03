package org.nitb.orchestrator.cloud

import java.io.Serializable
import java.util.function.Consumer

interface CloudConsumer<T: Serializable> {

    fun registerConsumer(client: CloudClient<T>, onConsume: Consumer<CloudMessage<T>>) {
        client.cancelConsumer()
        client.createConsumer(onConsume)
    }

    fun cancelConsumer(client: CloudClient<T>) {
        client.cancelConsumer()
    }
}