package org.nitb.orchestrator.cloud.rabbitmq

import com.rabbitmq.client.*
import org.nitb.orchestrator.cloud.CloudMessage
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.serialization.json.JSONSerializer
import java.io.Serializable
import java.util.function.Consumer

class RabbitMqConsumer<T: Serializable>(
    private val name: String,
    channel: Channel,
    private val onReceive: Consumer<CloudMessage<T>>,
    private val onShutdown: Runnable
): DefaultConsumer(channel) {

    @Suppress("UNCHECKED_CAST")
    override fun handleDelivery(
        consumerTag: String?,
        envelope: Envelope?,
        properties: AMQP.BasicProperties?,
        body: ByteArray?
    ) {
        val deliveryTag = envelope?.deliveryTag

        try {
            val message = try {
                BinarySerializer.deserialize(body!!)
            } catch (e: Exception) {
                JSONSerializer.deserialize(String(body!!), CloudMessage::class.java) as CloudMessage<T>
            }

            onReceive.accept(message)
        } catch (e: Exception) {
            logger.warn("RABBITMQ WARNING: Error processing received message from server for queue $name", e)
        } finally {
            channel.basicAck(deliveryTag!!, false)
        }
    }

    override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
        var isOk = false

        while (!isOk) {
            try {
                onShutdown.run()
                isOk = true
            } catch (e: AlreadyClosedException) {
                Thread.sleep(100)
            }
        }
    }

    private val logger = LoggingManager.getLogger(name)
}