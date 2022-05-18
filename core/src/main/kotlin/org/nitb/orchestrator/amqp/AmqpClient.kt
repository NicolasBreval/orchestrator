package org.nitb.orchestrator.amqp

import java.io.Serializable
import java.util.function.Consumer

/**
 * Abstract class used as wrapper for different queue protocols, that allows to project use them within specify their custom logic.
 *
 * @property name Name of queue to be defined
 */
abstract class AmqpClient<T: Serializable>(
    protected val name: String,
    protected val workers: Int
) {

    // region PUBLIC METHODS

    /**
     * Sends specified content to an specific queue.
     * @param receiver Name of queue to send content.
     * @param message Content to send
     */
    abstract fun <M: Serializable> send(receiver: String, message: M)

    /**
     * Creates a new consumer to listen for queue's input messages.
     * @param onConsume Function to be processed on message input.
     */
    abstract fun createConsumer(onConsume: Consumer<AmqpMessage<T>>)

    /**
     * Removes consumer, so client stops listening messages.
     */
    abstract fun cancelConsumer()

    /**
     * Removes all messages inside queue.
     */
    abstract fun purge()

    /**
     * Closes connections related to this client.
     */
    abstract fun close()

    /**
     * Checks if master node queue has a consumer
     */
    abstract fun masterConsuming(): Boolean

    // endregion

    // region INIT

    init {
        Runtime.getRuntime().addShutdownHook(Thread(this::close))
    }

    // endregion

}