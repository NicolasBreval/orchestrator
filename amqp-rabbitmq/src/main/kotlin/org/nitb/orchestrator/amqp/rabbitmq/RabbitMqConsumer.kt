package org.nitb.orchestrator.amqp.rabbitmq

import com.rabbitmq.client.*
import org.nitb.orchestrator.amqp.AmqpMessage
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
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
 * @property onReceive Function which receives a variable of type [AmqpMessage] and process it.
 * @property onShutdown Function used when consumer receives a shutdown message from server.
 */
class RabbitMqConsumer<T: Serializable>(
    private val name: String,
    channel: Channel,
    private val onReceive: Consumer<AmqpMessage<T>>,
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

        var retries = ConfigManager.getInt(ConfigNames.AMQP_RETRIES, ConfigNames.AMQP_RETRIES_DEFAULT).let { if (it < 0) 0 else it }

        while (retries > -1) {
            try {
                val message = try {
                    BinarySerializer.deserialize(body!!)
                } catch (e: Exception) {
                    JSONSerializer.deserialize(String(body!!), AmqpMessage::class.java) as AmqpMessage<T>
                }

                onReceive.accept(message)
            } catch (e: Exception) {
                retries--

                if (e !is InterruptedException)
                    logger.warn("RABBITMQ WARNING: Error processing received message from server for queue $name", e)
            }
        }

        channel.basicAck(deliveryTag!!, false)
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