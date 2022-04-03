package org.nitb.orchestrator.cloud.activemq

import org.apache.activemq.*
import org.apache.activemq.advisory.AdvisorySupport
import org.nitb.orchestrator.cloud.CloudClient
import org.nitb.orchestrator.cloud.CloudMessage
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.serialization.json.JSONSerializer
import java.io.Serializable
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import javax.jms.*
import javax.jms.Message

class ActiveMqCloudClient<T: Serializable>(
    name: String,
    consumerCountListener: Boolean = false
): CloudClient<T>(name), MessageListener {

    companion object {
        val connectionFactory: ActiveMQConnectionFactory = ActiveMQConnectionFactory(
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_USERNAME, ConfigNames.ACTIVEMQ_DEFAULT_PASSWORD),
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_PASSWORD, ConfigNames.ACTIVEMQ_DEFAULT_PASSWORD),
            ConfigManager.getProperty(ConfigNames.ACTIVEMQ_BROKER_URL, RuntimeException("Needed property doesn't exists: ${ConfigNames.ACTIVEMQ_USERNAME}")))
    }

    override fun <M: Serializable> send(receiver: String, message: M) {
        try {
            val cloudMessage = CloudMessage(name, message)
            val bytes = BinarySerializer.encode(cloudMessage)
            val bytesMessage = session.createBytesMessage()
            bytesMessage.writeBytes(bytes)
            val producer = session.createProducer(session.createQueue(receiver))
            producer.deliveryMode = DeliveryMode.NON_PERSISTENT
            producer.send(bytesMessage)
        } catch (e: Exception) {
            logger.error("Error sending message", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun createConsumer(onConsume: Consumer<CloudMessage<T>>) {
        consumer = session.createConsumer(declareQueue()) { message ->

            try {
                val cloudMessage = when (message) {
                    is BytesMessage -> {
                        val bytes = ByteArray(message.bodyLength.toInt())
                        message.readBytes(bytes)
                        BinarySerializer.decode(bytes)
                    }
                    is TextMessage -> JSONSerializer.deserialize(message.text, CloudMessage::class.java) as CloudMessage<T>
                    else -> throw IllegalArgumentException("Invalid input message type")
                }
                onConsume.accept(cloudMessage)
            } catch (e: Exception) {
                if (e !is InterruptedException)
                    logger.error("Error consuming message", e)
            } finally {
                message.acknowledge()
            }
        } as ActiveMQMessageConsumer
    }

    override fun cancelConsumer() {
        if (this::consumer.isInitialized)
            consumer.close()
    }

    override fun purge() {
        consumer.close()
        val destination = declareQueue()
        val messageListener = consumer.messageListener

        val browser = session.createBrowser(destination as Queue)
        val messages = browser.enumeration.asSequence().count()
        val simpleConsumer = session.createConsumer(destination)
        var purged = 0

        while (purged < messages) {
            simpleConsumer.receive(1000)?.acknowledge()
            purged++
        }

        simpleConsumer.close()
        consumer = session.createConsumer(destination, messageListener) as ActiveMQMessageConsumer
    }

    override fun close() {
        cancelConsumer()
        session.close()
        connection.close()
    }

    override fun masterConsuming(): Boolean {
        return masterConsumersCount.get() > 0
    }

    private fun declareQueue(): Destination {
        return session.createQueue(name)
    }

    override fun onMessage(message: Message?) {
        val source = message?.jmsDestination

        if (source == AdvisorySupport.getConsumerAdvisoryTopic(monitored)) {
            masterConsumersCount.set(message?.getIntProperty("consumerCount") ?: masterConsumersCount.get())
        }

        message?.acknowledge()
    }

    private val logger = LoggingManager.getLogger(this::class.java)
    private val connection: ActiveMQConnection = connectionFactory.createConnection() as ActiveMQConnection
    private val session: ActiveMQSession by lazy { connection.createSession(false, Session.CLIENT_ACKNOWLEDGE) as ActiveMQSession }
    private lateinit var consumer: ActiveMQMessageConsumer
    private val monitored: Destination by lazy { session.createQueue(ConfigManager.getProperty(ConfigNames.PRIMARY_NAME)) }
    private var masterConsumersCount: AtomicInteger = AtomicInteger(0)

    init {
        if (consumerCountListener) {
            val destination = session.createTopic(AdvisorySupport.getConsumerAdvisoryTopic(monitored).physicalName
                    + "," + AdvisorySupport.getProducerAdvisoryTopic(monitored).physicalName)
            val advisoryConsumer = session.createConsumer(destination)
            advisoryConsumer.messageListener = this
        }

        connection.start()
    }

}