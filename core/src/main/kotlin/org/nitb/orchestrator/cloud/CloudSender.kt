package org.nitb.orchestrator.cloud

import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable

/**
 * Interface used to make easy sending messages to a specific queue.
 */
interface CloudSender {

    // region PUBLIC METHODS

    /**
     * Sends same message to all receivers specified in list.
     * @param message Message to be sent.
     * @param client Client used to make sent.
     * @param receivers List of Subscriptions to send message.
     */
    fun sendToReceivers(message: Serializable, client: CloudClient<*>, receivers: List<SubscriptionReceiver>) {
        for (receiver in receivers) {
            client.send(receiver.name, message)
        }
    }

    /**
     * Sends a single message to a queue.
     * @param message Message to be sent.
     * @param client Client used to make sent
     * @param receiver Name of queue to send message.
     */
    fun sendMessage(message: Serializable, client: CloudClient<*>, receiver: String) {
        client.send(receiver, message)
    }

    // endregion
}