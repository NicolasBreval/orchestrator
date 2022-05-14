package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.scheduling.Scheduler
import org.nitb.orchestrator.subscription.CyclicalSubscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable
import java.math.BigInteger

@HeritableSubscription
abstract class DeliveryPeriodicalSubscription<O: Serializable>(
    name: String,
    private val delay: Long,
    private val initialDelay: Long = 0,
    private val receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null
): CyclicalSubscription<O>(name, timeout, description), AmqpManager<O>, AmqpSender {

    @delegate:Transient
    private val client: AmqpClient<O> by lazy { createClient(name) }

    override fun createScheduler(): Scheduler {
        return object : PeriodicalScheduler(delay, initialDelay, timeout) {
            override fun onCycle() {
                val result = runEvent(BigInteger.ZERO, name, Unit)

                if (result != null)
                    sendToReceivers(result, client, receivers)
            }
        }
    }

    override fun deactivate() {
        super.deactivate()
        client.close()
    }
}