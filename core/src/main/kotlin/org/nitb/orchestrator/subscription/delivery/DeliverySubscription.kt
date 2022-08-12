package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.io.Serializable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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

    override fun deactivate() {
        client.close()
    }

    override fun onStop() {
        client.cancelConsumer()
    }

    override fun onStart() {
        client.createConsumer { cloudMessage ->
            val executor = Executors.newSingleThreadExecutor()

            val task = executor.submit {
                val result = runEvent(cloudMessage.size, cloudMessage.sender, cloudMessage.message)

                if (result != null)
                    sendToReceivers(result, client, receivers)
            }

            try {
                if (timeout > 0)
                    task.get(timeout, TimeUnit.MILLISECONDS)
                else
                    task.get()
            } catch (e: TimeoutException) {
                onError(cloudMessage.message)
               throw InterruptedException()
            } finally {
                executor.shutdownNow()
            }
        }
    }
}