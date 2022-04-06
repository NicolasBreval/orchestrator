package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.cloud.CloudClient
import org.nitb.orchestrator.cloud.CloudConsumer
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable

@HeritableSubscription
abstract class DeliverySubscription<I: Serializable, O: Serializable>(
    name: String,
    private val receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null
): Subscription<I, O>(name, timeout, description), CloudManager<I>, CloudConsumer<I>, CloudSender {

    @delegate:Transient
    protected val client: CloudClient<I> by lazy { createClient(name) }

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