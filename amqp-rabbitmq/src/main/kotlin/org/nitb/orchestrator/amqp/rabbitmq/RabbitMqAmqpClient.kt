package org.nitb.orchestrator.amqp.rabbitmq

import com.rabbitmq.client.AlreadyClosedException
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.ShutdownListener
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpMessage
import org.nitb.orchestrator.amqp.AmqpType
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import java.io.IOException
import java.io.Serializable
import java.lang.RuntimeException
import java.util.function.Consumer

/**
 * [AmqpClient] object based on RabbitMQ protocol.
 *
 * @param name Name of queue related to this client.
 * @see AmqpClient
 */
@AmqpType("rabbitmq")
class RabbitMqAmqpClient<T: Serializable>(
    name: String,
    workers: Int
): AmqpClient<T>(name, workers) {

    // region STATIC

    companion object {

        /**
         * Object used to create connections with same configuration, obtained from properties.
         */
        private val connectionFactory: ConnectionFactory = ConnectionFactory()

        init {
            connectionFactory.host = ConfigManager.getProperty(ConfigNames.RABBITMQ_HOST, RuntimeException("Needed property doesn't exists: ${ConfigNames.RABBITMQ_HOST}"))
            connectionFactory.port = ConfigManager.getInt(ConfigNames.RABBITMQ_PORT, ConfigNames.RABBITMQ_DEFAULT_PORT)
            connectionFactory.username = ConfigManager.getProperty(ConfigNames.RABBITMQ_USERNAME, ConfigNames.RABBITMQ_DEFAULT_USERNAME)
            connectionFactory.password = ConfigManager.getProperty(ConfigNames.RABBITMQ_PASSWORD, ConfigNames.RABBITMQ_DEFAULT_PASSWORD)
        }
    }

    // endregion

    // region PUBLIC METHODS

    override fun <M: Serializable> send(receiver: String, message: M) {
       try {
           channel.basicPublish("", receiver, null, BinarySerializer.serialize(
               AmqpMessage(
                   name,
                   message
               )
           ))
       } catch (e: AlreadyClosedException) {
           logger.error("RABBITMQ ERROR: Error connecting with server for queue $name, please check connection to server", e)
       }
    }

    override fun createConsumer(onConsume: Consumer<AmqpMessage<T>>) {
        try {
            consumerFunction = onConsume
            declareQueue()
            for (i in 0 until workers) {
                consumerTags.add(channel.basicConsume(name,
                    RabbitMqConsumer(name, channel, onConsume) {
                        createConsumer(onConsume)
                    }))
            }
        } catch (e: IOException) {
            logger.error("RABBITMQ ERROR: Error creating new consumer for queue $name", e)
        }
    }

    override fun cancelConsumer() {
        consumerTags.forEach { channel.basicCancel(it) }
        consumerTags.clear()
    }

    override fun purge() {
        if (channel.isOpen) {
            declareQueue()
            channel.queuePurge(name)
        }
    }

    override fun close() {
        try {
            cancelConsumer()
            channel.removeShutdownListener(shutdownListener)
            channel.close()
            connection.close()
        } catch (e: Exception) {
            // do nothing
        }
    }

    override fun masterConsuming(): Boolean {
        if (!channel.isOpen) return true
        return channel.queueDeclare(mainNodeName, true, false, false, null)?.consumerCount?.let { it > 0 } ?: false
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Connection object used to send and receive data from queues.
     */
    private var connection = connectionFactory.newConnection()

    /**
     * Channel object used to send and receive data from queues. It's created from [connection] object.
     */
    private var channel = connection.createChannel()

    /**
     * Logger object used to show information to developer and client.
     */
    private val logger = LoggingManager.getLogger(name)

    /**
     * Identifier of consumer. When RabbitMQ client creates new consumer for a client, returns a tag to identify the consumer.
     * This tag is used to check if client already contains a consumer, because in this project clients must contain only a consumer per queue.
     */
    private val consumerTags: MutableList<String> = mutableListOf()

    private val mainNodeName = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME)

    private lateinit var consumerFunction: Consumer<AmqpMessage<T>>

    private val shutdownListener = ShutdownListener {
        logger.warn("Recovering channel due to shutdown...")

        while (!connection.isOpen) {
            try {
                connection = connectionFactory.newConnection()
                channel = connection.createChannel()
                createConsumer(consumerFunction)
            } catch (e: Exception) {
                logger.warn("Error recovering channel, retrying after a second...")
                Thread.sleep(1000)
            }
        }
    }

    // endregion

    // region PRIVATE METHODS

    /**
     * Method used to create a new queue for this client. All queues are exclusive by default
     */
    private fun declareQueue() {
        if (!channel.isOpen) return;
        channel.queueDeclare(name, true, workers == 1 && name != mainNodeName, false, null)
    }

    // endregion

    init {
        channel.addShutdownListener(shutdownListener)
    }
}