package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable

@Suppress("unused")
abstract class DeliveryNoInputSubscription<O: Serializable>(
    name: String,
    timeout: Long = -1,
    description: String? = null,
    private val receivers: List<SubscriptionReceiver>
): Subscription<Unit, O>(name, timeout, description), AmqpManager<O>, AmqpSender {

    @delegate:Transient
    protected val client: AmqpClient<O> by lazy { createClient(name, 0) }

    override fun deactivate() {
        client.close()
    }

    override fun onSuccess(input: Unit, output: O?) {
        if (output != null)
            sendToReceivers(output, client, receivers)
    }

    override fun onEvent(sender: String, input: Unit): O? {
        // DO NOTHING
        return null
    }


}