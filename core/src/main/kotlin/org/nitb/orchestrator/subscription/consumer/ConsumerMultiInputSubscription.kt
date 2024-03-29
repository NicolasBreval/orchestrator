package org.nitb.orchestrator.subscription.consumer

import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.delivery.SerializableMap
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.LinkedBlockingDeque

@HeritableSubscription
abstract class ConsumerMultiInputSubscription(
    name: String,
    private val senders: List<String>,
    private val limit: Int = Int.MAX_VALUE,
    timeout: Long = -1,
    description: String? = null,
    private val workers: Int = 1
): Subscription<SerializableMap<String, Serializable>, Unit>(name, timeout, description),
    AmqpManager<Serializable>, AmqpConsumer<Serializable>, AmqpSender {

    @Transient
    private val senderQueues = senders.associateWith { LinkedBlockingDeque<String>(limit) }

    @delegate:Transient
    private val client: AmqpClient<Serializable> by lazy { createClient(name, workers) }

    override fun onStart() {
        client.createConsumer { cloudMessage ->

            senderQueues[cloudMessage.sender].let { queue ->
                queue?.push(push(cloudMessage.message))
            }

            if (senderQueues.all { queue -> queue.value.size > 0 }) {
                val values = SerializableMap(senders.associateWith { sender -> pop(sender) })
                runEvent(cloudMessage.size, cloudMessage.sender, values)
            }
        }
    }

    override fun deactivate() {
        client.close()
    }

    private fun push(input: Serializable): String {
        val uuid = System.nanoTime().toString()
        val path = Paths.get("queues/$name/$uuid")

        Files.createDirectories(path.parent)
        Files.write(path, BinarySerializer.serialize(input))

        return uuid
    }

    private fun pop(sender: String): Serializable {
        val uuid = senderQueues[sender]?.pop()
        val path = Paths.get("./queues/$name/$uuid")

        val message = BinarySerializer.deserialize<Serializable>(Files.readAllBytes(path))
        Files.deleteIfExists(path)

        return message
    }

}