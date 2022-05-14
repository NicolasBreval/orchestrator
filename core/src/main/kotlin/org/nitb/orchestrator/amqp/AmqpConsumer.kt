package org.nitb.orchestrator.amqp

import java.io.Serializable
import java.util.function.Consumer

/**
 * Interface used to make easy create a consumer in a class
 */
interface AmqpConsumer<T: Serializable> {

    // region PUBLIC METHODS

    /**
     * Registers a new consumer for a specified [AmqpClient].
     * @param client Client to register consumer.
     * @param onConsume Function to process input messages
     */
    fun registerConsumer(client: AmqpClient<T>, onConsume: Consumer<AmqpMessage<T>>) {
        client.cancelConsumer()
        client.createConsumer(onConsume)
    }

    // endregion

}