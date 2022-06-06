package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.subscription.CyclicalSubscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.entities.PeriodType
import org.nitb.orchestrator.transformers.Transformer
import java.io.Serializable

@HeritableSubscription
abstract class DeliveryPeriodicalSubscription<O: Serializable>(
    name: String,
    timeout: Long = -1,
    description: String? = null,
    periodExpression: String,
    type: PeriodType,
    private val receivers: List<SubscriptionReceiver> = listOf(),
): CyclicalSubscription<O>(name, timeout, description, periodExpression, type), AmqpManager<O>, AmqpSender {

    @delegate:Transient
    private val client: AmqpClient<O> by lazy { createClient(name) }

    override fun deactivate() {
        super.deactivate()
        client.close()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSuccess(input: Unit, output: O?) {
        if (output != null)
            sendToReceivers(output, client, receivers)
    }
}