package org.nitb.orchestrator.cloud

import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable

interface CloudSender {

    fun sendToReceivers(message: Serializable, client: CloudClient<*>, receivers: List<SubscriptionReceiver>) {
        for (receiver in receivers) {
            client.send(receiver.name, message)
        }
    }

    fun sendMessage(message: Serializable, client: CloudClient<*>, receiver: String) {
        client.send(receiver, message)
    }
}