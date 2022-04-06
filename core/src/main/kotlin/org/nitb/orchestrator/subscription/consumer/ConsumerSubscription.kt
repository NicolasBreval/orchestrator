package org.nitb.orchestrator.subscription.consumer

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.cloud.CloudClient
import org.nitb.orchestrator.cloud.CloudConsumer
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.subscription.Subscription
import java.io.Serializable

@HeritableSubscription
abstract class ConsumerSubscription<I: Serializable>(
    name: String,
    timeout: Long = -1,
    description: String? = null
): Subscription<I, Unit>(name, timeout, description), CloudManager<I>, CloudConsumer<I> {

    @delegate:Transient
    protected val client: CloudClient<I> by lazy { createClient(name) }

    override fun initialize() {
        client.createConsumer() { cloudMessage ->
            runEvent(cloudMessage.size, cloudMessage.sender, cloudMessage.message)
        }
    }

    override fun deactivate() {
        client.close()
    }
}