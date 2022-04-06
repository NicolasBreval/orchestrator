package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.cloud.CloudClient
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.scheduling.CronScheduler
import org.nitb.orchestrator.scheduling.Scheduler
import org.nitb.orchestrator.subscription.CyclicalSubscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable
import java.math.BigInteger

@HeritableSubscription
abstract class DeliveryCronSubscription<O: Serializable>(
    name: String,
    private val cronExpression: String,
    private val receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null
): CyclicalSubscription<O>(name, timeout, description), CloudManager<O>, CloudSender {

    @delegate:Transient
    private val client: CloudClient<O> by lazy { createClient(name) }

    override fun createScheduler(): Scheduler {
        return object : CronScheduler(cronExpression, timeout) {
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