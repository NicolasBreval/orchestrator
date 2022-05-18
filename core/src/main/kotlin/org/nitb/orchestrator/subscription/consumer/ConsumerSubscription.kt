package org.nitb.orchestrator.subscription.consumer

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.subscription.Subscription
import java.io.Serializable

@HeritableSubscription
abstract class ConsumerSubscription<I: Serializable>(
    name: String,
    timeout: Long = -1,
    description: String? = null,
    private val workers: Int = 1
): Subscription<I, Unit>(name, timeout, description), AmqpManager<I>, AmqpConsumer<I> {

    @delegate:Transient
    protected val client: AmqpClient<I> by lazy { createClient(name, workers) }

    override fun initialize() {
        client.createConsumer() { cloudMessage ->
            runEvent(cloudMessage.size, cloudMessage.sender, cloudMessage.message)
        }
    }

    override fun deactivate() {
        client.close()
    }
}