package org.nitb.orchestrator.cloud.rabbitmq

import com.rabbitmq.client.*
import org.nitb.orchestrator.cloud.CloudMessage
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.serialization.json.JSONSerializer
import java.io.Serializable
import java.util.function.Consumer

/**
 * Consumer object used to define a consumer for a RabbitMQ queue. A consumer receives all messages sent to a queue and process them.
 *
 * @property name Name of queue to consume their messages.
 * @param channel Channel used to declare consumer.
 * @property onReceive Function which receives a variable of type [CloudMessage] and process it.
 * @property onShutdown Function used when consumer receives a shutdown message from server.
 */
class RabbitMqConsumer<T: Serializable>(
    private val name: String,
    channel: Channel,
    private val onReceive: Consumer<CloudMessage<T>>,
    private val onShutdown: Runnable
): DefaultConsumer(channel) {

    // region PUBLIC METHODS

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

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Logger object used to show information to developer
     */
    private val logger = LoggingManager.getLogger(name)

    // endregion
}