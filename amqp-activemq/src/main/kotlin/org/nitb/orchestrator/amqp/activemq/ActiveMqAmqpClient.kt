package org.nitb.orchestrator.amqp.activemq

import org.apache.activemq.*
import org.apache.activemq.advisory.AdvisorySupport
import org.nitb.orchestrator.amqp.AmqpBlockingException
import org.nitb.orchestrator.amqp.AmqpClient
import org.nitb.orchestrator.amqp.AmqpMessage
import org.nitb.orchestrator.amqp.AmqpType
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.serialization.json.JSONSerializer
import java.io.Serializable
import java.lang.RuntimeException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import javax.jms.*
import javax.jms.Message

/**
 * Implementation of the AmqpClient interface with ActiveMQ technology, based on JMS.
 * With this implementation, is possible to send data to a specific queue and register one o multiple consumers on
 * a queue and process received messages. When you create a new instance of this client, always creates automatically
 * a queue with the name passed as parameter. Also, it's possible register a consumer to process received messages
 * to mentioned queue, and you can specify number of consumers to create. If you create more than one consumer ([workers] > 1),
 * all consumers created contains same code, but messages will be processed at same time by multiple consumers, this is
 * a good solution for parallel processing.
 *
 * @param name Name of queue that is automatically created in ActiveMQ server.
 * @param workers Number of consumers that client creates.
 *
 * @see AmqpClient
 * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
 */
@AmqpType("activemq")
class ActiveMqAmqpClient<T: Serializable>(
    name: String,
    workers: Int = 1
): AmqpClient<T>(name, workers), MessageListener {

    // region STATIC

    companion object {

        // region PRIVATE PROPERTIES

        /**
         * Factory object used to create new connections with same configuration. All properties needed are taken from
         * properties file. If property related to server url, called [ConfigNames.ACTIVEMQ_BROKER_URL], is not set,
         * returns a [RuntimeException]
         */
        private val connectionFactory: ActiveMQConnectionFactory = ActiveMQConnectionFactory(
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_USERNAME, ConfigNames.ACTIVEMQ_DEFAULT_USERNAME),
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_PASSWORD, ConfigNames.ACTIVEMQ_DEFAULT_PASSWORD),
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_BROKER_URL, RuntimeException("Needed property doesn't exists: ${ConfigNames.ACTIVEMQ_USERNAME}")))

        // endregion
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
            val amqpMessage = AmqpMessage(name, message)
            val bytes = BinarySerializer.serialize(amqpMessage)
            val bytesMessage = session.createBytesMessage()
            bytesMessage.writeBytes(bytes)
            val producer = session.createProducer(declareQueue(receiver))
            producer.deliveryMode = DeliveryMode.NON_PERSISTENT
            producer.send(bytesMessage)
        } catch (e: Exception) {
            logger.error("Error sending message from $name to $receiver", e)
        }
    }

    /**
     * Allows to create a consumer to process received messages from Amqp server. A consumer is defined by their consume
     * function, that specifies how it should process a new received message.
     *
     * @param onConsume Function used to process a new received message.
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    @Suppress("UNCHECKED_CAST")
    override fun createConsumer(onConsume: Consumer<AmqpMessage<T>>) {
        if (!session.isClosed) {
            for (i in 0 until workers) {
                consumers.add(session.createConsumer(declareQueue(name)) { message ->

                    var sendAck = true
                    var retries = ConfigManager.getInt(ConfigNames.AMQP_RETRIES, ConfigNames.AMQP_RETRIES_DEFAULT).let { if (it < 0) 0 else it }

                    val task = executor.submit {
                        while (retries > -1) {
                            try {
                                val amqpMessage = when (message) {
                                    is BytesMessage -> {
                                        val bytes = ByteArray(message.bodyLength.toInt())
                                        message.readBytes(bytes)
                                        BinarySerializer.deserialize(bytes)
                                    }
                                    is TextMessage -> JSONSerializer.deserialize(message.text, AmqpMessage::class.java) as AmqpMessage<T>
                                    else -> throw IllegalArgumentException("Invalid input message type")
                                }
                                onConsume.accept(amqpMessage)
                                retries = -1
                            } catch (e: Exception) {
                                retries--

                                if (e !is InterruptedException)
                                    logger.error("Error consuming message", e)

                                sendAck = e !is AmqpBlockingException

                            }
                        }
                    }
                    task.get()

                    if (sendAck)
                        message.acknowledge()

                } as ActiveMQMessageConsumer)
            }
        }
    }

    /**
     * Allows to cancel previously declared consumer
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun cancelConsumer() {
        consumers.forEach { it.close() }
        consumers.clear()
    }

    /**
     * Purges queue related to this client. When a queue is purged, all messages enqueued on it are deleted permanently.
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun purge() {
        if (!session.isClosed) {
            consumers.forEach { it.close() }

            val destination = declareQueue(name)
            val messageListeners = consumers.map { it.messageListener }

            val browser = session.createBrowser(destination as Queue)
            val messages = browser.enumeration.asSequence().count()
            val simpleConsumer = session.createConsumer(destination)
            var purged = 0

            while (purged < messages) {
                simpleConsumer.receive(1000)?.acknowledge()
                purged++
            }

            simpleConsumer.close()
            consumers.clear()
            consumers.addAll(messageListeners.map {
                session.createConsumer(
                    destination,
                    it
                ) as ActiveMQMessageConsumer
            })
        }
    }

    /**
     * Used to initialize client. Creates a new connection and a new session.
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun start() {
        try {
            connection = initConnection()
            session = initSession(connection)
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

            advisoryConsumer.close()
            session.close()
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
        return masterConsumersCount.get() > 0
    }

    /**
     * Checks for connection with Amqp server.
     *
     * @return `true` if connection is already open, else false
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun isConnected(): Boolean {
        return !(session.isClosed || connection.isClosed)
    }

    /**
     * Used to process advisory messages and allows checking if main queue contains any consumer.
     *
     * @param message Message received with information about main queue
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    override fun onMessage(message: Message?) {
        val source = message?.jmsDestination

        if (source == AdvisorySupport.getConsumerAdvisoryTopic(monitored)) {
            masterConsumersCount.set(message?.getIntProperty("consumerCount") ?: masterConsumersCount.get())
        }

        message?.acknowledge()
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Logger object used to show information to developer and client.
     */
    private val logger = LoggingManager.getLogger(name)

    /**
     * Connection object used to send and receive data from ActiveMQ queues.
     *
     * @see ActiveMQConnection
     */
    private var connection: ActiveMQConnection = initConnection()

    /**
     * Session object used to send and receive data from ActiveMQ queues. It's created from [connection] object.
     *
     * @see ActiveMQSession
     */
    private var session: ActiveMQSession = initSession(connection)

    /**
     * List of consumers. Length of this list is equal that [workers] property, and all consumers have same
     * consume function.
     */
    private val consumers: MutableList<ActiveMQMessageConsumer> = mutableListOf()

    /**
     * Consumer object used to update information about master node queue.
     */
    private var advisoryConsumer: ActiveMQMessageConsumer

    /**
     * Queue used for main queue monitoring
     */
    private val monitored: Destination by lazy { declareQueue(ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("Mandatory property not found ${ConfigNames.PRIMARY_NAME}"))) }

    /**
     * Variable used for main queue checking, if this value is greater than 1, is a bad signal, because main node queue only must contain a consumer
     */
    private var masterConsumersCount: AtomicInteger = AtomicInteger(0)

    /**
     * Executor object to run consumer function in another thread and wait for their termination without loops.
     */
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Name configured for main node.
     */
    private val mainNodeName = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME)

    /**
     * Name configured for display node.
     */
    private val displayNodeName = ConfigManager.getProperty(ConfigNames.DISPLAY_NODE_NAME)

    // endregion

    // region PRIVATE METHODS

    /**
     * Creates a new connection with Amqp server using [connectionFactory].
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    private fun initConnection(): ActiveMQConnection {
        return connectionFactory.createConnection() as ActiveMQConnection
    }

    /**
     * Creates a new session with Amqp server using [connection].
     *
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    private fun initSession(connection: Connection): ActiveMQSession {
        return connection.createSession(false, Session.CLIENT_ACKNOWLEDGE) as ActiveMQSession
    }

    /**
     * Creates a queue for this client. Queue only is exclusive if is not related to display or main node,
     * and have only a worker, else not.
     *
     * @param name Name of queue to be created
     * @author Nicolas Breval Rodriguez - nicolasbrevalrodriguez@gmail.com
     */
    private fun declareQueue(name: String): Destination {
        return session.createQueue("$name?consumer.exclusive=${ if (name != displayNodeName && workers == 1 && name != mainNodeName) "true" else "false" }")
    }

    // endregion

    // region INIT

    init {
        val destination = session.createTopic(AdvisorySupport.getConsumerAdvisoryTopic(monitored).physicalName
                + "," + AdvisorySupport.getProducerAdvisoryTopic(monitored).physicalName)
        advisoryConsumer = session.createConsumer(destination) as ActiveMQMessageConsumer
        advisoryConsumer.messageListener = this
        connection.start()
    }

    // endregion
}