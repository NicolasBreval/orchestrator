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
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import javax.jms.*
import javax.jms.Message

/**
 * [AmqpClient] based on ActiveMQ protocol.
 *
 * @param name Name of queue related to this client.
 *  (specified by [ConfigNames.PRIMARY_NAME]]) contains a consumer. This is needed to don't create two consumers for main node listening to same queue.
 * @constructor Creates a client based on ActiveMQ protocol, and can be used to send messages a declared consumers to queue with same name as [name].
 * @see AmqpClient
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
         * Object used to create connections with same configuration, obtained from properties.
         */
        private val connectionFactory: ActiveMQConnectionFactory = ActiveMQConnectionFactory(
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_USERNAME, ConfigNames.ACTIVEMQ_DEFAULT_USERNAME),
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_PASSWORD, ConfigNames.ACTIVEMQ_DEFAULT_PASSWORD),
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_BROKER_URL, RuntimeException("Needed property doesn't exists: ${ConfigNames.ACTIVEMQ_USERNAME}")))

        // endregion
    }

    // endregion

    // region PUBLIC METHODS

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

    @Suppress("UNCHECKED_CAST")
    override fun createConsumer(onConsume: Consumer<AmqpMessage<T>>) {
        for (i in 0 until workers) {
            consumers.add(session.createConsumer(declareQueue(name)) { message ->

                var sendAck = true
                var retries = ConfigManager.getInt(ConfigNames.AMQP_RETRIES, ConfigNames.AMQP_RETRIES_DEFAULT).let { if (it < 0) 0 else it }

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

                if (sendAck)
                    message.acknowledge()

            } as ActiveMQMessageConsumer)
        }
    }

    override fun cancelConsumer() {
        consumers.forEach { it.close() }
        consumers.clear()
    }

    override fun purge() {
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
        consumers.addAll(messageListeners.map { session.createConsumer(destination, it) as ActiveMQMessageConsumer })
    }

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

    override fun masterConsuming(): Boolean {
        return masterConsumersCount.get() > 0
    }

    /**
     * Method used to process advisory messages and allows checking if main queue contains any consumer.
     * @param message Message received with information about main queue
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
     * @see ActiveMQConnection
     */
    private val connection: ActiveMQConnection = connectionFactory.createConnection() as ActiveMQConnection

    /**
     * Session object used to send and receive data from ActiveMQ queues. It's created from [connection] object.
     * @see ActiveMQSession
     */
    private val session: ActiveMQSession by lazy { connection.createSession(false, Session.CLIENT_ACKNOWLEDGE) as ActiveMQSession }

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

    // endregion

    // region PRIVATE METHODS

    /**
     * Creates a queue for this client. All queues are created as exclusive, to ensure that,
     * if several consumers were created by mistake, only the first one would be operational
     * @param name Name of queue to be created
     */
    private fun declareQueue(name: String): Destination {
        return session.createQueue("$name?consumer.exclusive=${ if (workers > 1) "true" else "false" }")
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