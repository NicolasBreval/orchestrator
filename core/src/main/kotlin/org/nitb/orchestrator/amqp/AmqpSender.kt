package org.nitb.orchestrator.amqp

import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.transformers.Transformer
import java.io.Serializable

/**
 * Interface used to make easy sending messages to a specific queue.
 */
interface AmqpSender {

    // region PUBLIC METHODS

    /**
     * Sends same message to all receivers specified in list.
     * @param message Message to be sent.
     * @param client Client used to make sent.
     * @param receivers List of Subscriptions to send message.
     */
    @Suppress("UNCHECKED_CAST")
    fun sendToReceivers(message: Serializable, client: AmqpClient<*>, receivers: List<SubscriptionReceiver>) {
        if (client.isConnected()) {
            for (receiver in receivers) {
                val transformed = try {
                    (Class.forName(receiver.transformer).newInstance() as Transformer<Serializable>).transform(message)
                } catch (e: Exception) {
                    message
                }

                client.send(receiver.name, transformed)
            }
        }
    }

    /**
     * Sends a single message to a queue.
     * @param message Message to be sent.
     * @param client Client used to make sent
     * @param receiver Name of queue to send message.
     */
    fun sendMessage(message: Serializable, client: AmqpClient<*>, receiver: String) {
        client.send(receiver, message)
    }

    // endregion
}