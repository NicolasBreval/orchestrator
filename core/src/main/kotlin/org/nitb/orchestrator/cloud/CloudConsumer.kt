package org.nitb.orchestrator.cloud

import java.io.Serializable
import java.util.function.Consumer

/**
 * Interface used to make easy create a consumer in a class
 */
interface CloudConsumer<T: Serializable> {

    // region PUBLIC METHODS

    /**
     * Registers a new consumer for a specified [CloudClient].
     * @param client Client to register consumer.
     * @param onConsume Function to process input messages
     */
    fun registerConsumer(client: CloudClient<T>, onConsume: Consumer<CloudMessage<T>>) {
        client.cancelConsumer()
        client.createConsumer(onConsume)
    }

    // endregion

}