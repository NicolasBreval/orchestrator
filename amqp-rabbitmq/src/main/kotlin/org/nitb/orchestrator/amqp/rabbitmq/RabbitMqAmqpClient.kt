package org.nitb.orchestrator.amqp.rabbitmq

import com.rabbitmq.client.AlreadyClosedException
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
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
 * Implementation of the AmqpClient interface with RabbitMQ technology, based on JMS.
 * With this implementation, is possible to send data to a specific queue and register one o multiple consumers on
 * a queue and process received messages. When you create a new instance of this client, always creates automatically
 * a queue with the name passed as parameter. Also, it's possible register a consumer to process received messages
 * to mentioned queue, and you can specify number of consumers to create. If you create more than one consumer ([workers] > 1),
 * all consumers created contains same code, but messages will be processed at same time by multiple consumers, this is
 * a good solution for parallel processing.
 *
 * @param name Name of queue that is automatically created in RabbitMQ server.
 * @param workers Number of consumers that client creates.
 *
 * @see AmqpClient
 * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
 */
@AmqpType("rabbitmq")
class RabbitMqAmqpClient<T: Serializable>(
    name: String,
    workers: Int
): AmqpClient<T>(name, workers) {

    // region STATIC

    companion object {

        /**
         * Factory object used to create new connections with same configuration. All properties needed are taken from
         * properties file. If property related to server url, called [ConfigNames.RABBITMQ_HOST], is not set,
         * returns a [RuntimeException].
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

    /**
     * Method used to send a message from current client to another queue. All messages to send must inherit from
     * Serializable interface, to endure object will be correctly binary-serialized.
     *
     * @param receiver Name of queue to send message.
     * @param message Data to be sent to [receiver]. This object must inherit from Serializable.
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
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

    /**
     * Allows to create a consumer to process received messages from Amqp server. A consumer is defined by their consume
     * function, that specifies how it should process a new received message.
     *
     * @param onConsume Function used to process a new received message.
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun createConsumer(onConsume: Consumer<AmqpMessage<T>>) {
        if (!isConnected()) {
            initConnection()
        }

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

    /**
     * Allows to cancel previously declared consumer
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun cancelConsumer() {
        consumerTags.forEach { channel.basicCancel(it) }
        consumerTags.clear()
    }

    /**
     * Purges queue related to this client. When a queue is purged, all messages enqueued on it are deleted permanently.
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun purge() {
        if (isConnected()) {
            declareQueue()
            channel.queuePurge(name)
        }
    }

    /**
     * Used to initialize client. Creates a new connection and a new session.
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun start() {
        try {
            if (!isConnected()) {
                initConnection()
            }
        } catch (e: Exception) {
            // do nothing
        }
    }

    /**
     * Brakes all connections with Amqp server, including declaring consumer.
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
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

    /**
     * Used to check if master node is consuming their queue. This method is used to check periodically
     * if master node is fallen, if is true, another node tries to consume from master node and obtain their role.
     *
     * @return `true` if there are any node with master role consuming from master queue, else `false`
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun masterConsuming(): Boolean {
        if (!isConnected()) return true
        return channel.queueDeclare(mainNodeName, true, false, false, null)?.consumerCount?.let { it > 0 } ?: false
    }

    /**
     * Checks for connection with Amqp server.
     *
     * @return `true` if connection is already open, else false
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun isConnected(): Boolean {
        return this::channel.isInitialized && this::connection.isInitialized && channel.isOpen && connection.isOpen
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Connection object used to send and receive data from RabbitMQ queues.
     *
     * @see Connection
     */
    private lateinit var connection: Connection

    /**
     * Session object used to send and receive data from RabbitMQ queues. It's created from [connection] object.
     *
     * @see Channel
     */
    private lateinit var channel: Channel

    /**
     * Logger object used to show information to developer and client.
     */
    private val logger = LoggingManager.getLogger(name)

    /**
     * Identifier of consumer. When RabbitMQ client creates new consumer for a client, returns a tag to identify the consumer.
     * This tag is used to check if client already contains a consumer, because in this project clients must contain only a consumer per queue.
     */
    private val consumerTags: MutableList<String> = mutableListOf()

    /**
     * Name configured for main node.
     */
    private val mainNodeName = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME)

    /**
     * Name configured for display node.
     */
    private val displayNodeName = ConfigManager.getProperty(ConfigNames.DISPLAY_NODE_NAME)

    /**
     * Function that consumer uses to process a new message.
     */
    private lateinit var consumerFunction: Consumer<AmqpMessage<T>>

    /**
     * Listener used to receive an event whtn connection is closed and try to recover it
     */
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
     * Creates a new connection and new channel.
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    @Synchronized
    private fun initConnection() {
        if (!isConnected()) {
            try {
                if (this::channel.isInitialized )
                    channel.close()
                if (this::connection.isInitialized)
                    connection.close()
            } catch (e: Exception) {
                // do nothing
            }

            connection = connectionFactory.newConnection()
            channel = connection.createChannel()
        }
    }

    /**
     * Method used to create a new queue for this client. All queues are exclusive by default
     */
    private fun declareQueue() {
        if (!channel.isOpen) return;
        channel.queueDeclare(name, true,  name != displayNodeName && workers == 1 && name != mainNodeName, false, null)
    }

    // endregion

    init {
        initConnection()
        channel.addShutdownListener(shutdownListener)
    }
}