package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable

@HeritableSubscription
abstract class DeliverySubscription<I: Serializable, O: Serializable>(
    name: String,
    private val receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null,
    private val workers: Int = 1
): Subscription<I, O>(name, timeout, description), AmqpManager<I>, AmqpConsumer<I>, AmqpSender {

    @delegate:Transient
    protected val client: AmqpClient<I> by lazy { createClient(name, workers) }

    protected fun activeSendOutput(output: O) {
        sendToReceivers(output, client, receivers)
    }

    override fun initialize() {
        client.createConsumer() { cloudMessage ->
            val result = runEvent(cloudMessage.size, cloudMessage.sender, cloudMessage.message)

            if (result != null)
                sendToReceivers(result, client, receivers)
        }
    }

    override fun deactivate() {
        client.close()
    }
}