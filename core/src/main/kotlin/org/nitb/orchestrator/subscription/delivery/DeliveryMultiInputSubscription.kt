package org.nitb.orchestrator.subscription.delivery

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.cloud.CloudClient
import org.nitb.orchestrator.cloud.CloudConsumer
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.io.Serializable

@HeritableSubscription
abstract class DeliveryMultiInputSubscription<I: Serializable, O: Serializable>(
    name: String,
    private val receivers: List<SubscriptionReceiver>,
    private val senders: List<String>,
    private val limit: Int = Int.MAX_VALUE,
    timeout: Long = -1,
    description: String? = null
): Subscription<SerializableMap<String, I>, O>(name, timeout, description), CloudManager<I>, CloudConsumer<I>, CloudSender {

    @Transient
    private val senderQueues = senders.associateWith { LinkedBlockingDeque<String>(limit) }

    @delegate:Transient
    private val client: CloudClient<I> by lazy { createClient(name) }

    override fun initialize() {
        client.createConsumer() { cloudMessage ->

            senderQueues[cloudMessage.sender].let { queue ->
                queue?.push(push(cloudMessage.message))
            }

            if (senderQueues.all { queue -> queue.value.size > 0 }) {
                val values = SerializableMap(senders.associateWith { sender -> pop(sender) })
                val result = runEvent(cloudMessage.size, cloudMessage.sender, values)

                if (result != null)
                    sendToReceivers(result, client, receivers)
            }
        }
    }

    override fun deactivate() {
        client.close()
    }

    private fun push(input: I): String {
        val uuid = UUID.randomUUID().toString()
        val path = Paths.get("queues/$name/$uuid")

        Files.createDirectories(path.parent)
        Files.write(path, BinarySerializer.encode(input))

        return uuid
    }

    private fun pop(sender: String): I {
        val uuid = senderQueues[sender]?.pop()
        val path = Paths.get("./queues/$name/$uuid")

        val message = BinarySerializer.decode<I>(Files.readAllBytes(path))
        Files.deleteIfExists(path)

        return message
    }

}