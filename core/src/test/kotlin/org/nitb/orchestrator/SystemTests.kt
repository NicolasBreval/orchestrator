package org.nitb.orchestrator

import com.cronutils.model.CronType
import org.junit.Test
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.DbController
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.Subscriber
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.consumer.ConsumerSubscription
import org.nitb.orchestrator.subscription.delivery.*
import org.nitb.orchestrator.subscription.detached.DetachedPeriodicalSubscription
import org.slf4j.event.Level
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import kotlin.test.*

abstract class Sender(
    name: String
): CloudManager<Serializable>, CloudSender {

    private val client by lazy { createClient(name) }

    fun send(message: Serializable, receiver: String) {
        sendMessage(message, client, receiver)
    }

    fun close() {
        client.close()
    }
}

class NumberGeneratorPeriodical(
    name: String,
    delay: Long,
    initialDelay: Long,
    receivers: List<SubscriptionReceiver>,
    private val number: Int
) : DeliveryPeriodicalSubscription<Int>(name, delay, initialDelay, receivers = receivers) {
    override fun onEvent(sender: String, input: Unit): Int {
        logger.info("Generating number from period")
        return number
    }
}

class NumberGeneratorCron(
    name: String,
    cronExpression: String,
    receivers: List<SubscriptionReceiver>,
    private val number: Int
) : DeliveryCronSubscription<Int>(name, cronExpression, receivers, CronType.QUARTZ) {
    override fun onEvent(sender: String, input: Unit): Int {
        logger.info("Generating number from cron")
        return number
    }
}

class NumbersAdder(
    name: String,
    receivers: List<SubscriptionReceiver>,
    senders: List<String>
) : DeliveryMultiInputSubscription<Int>(name, receivers, senders) {
    override fun onEvent(sender: String, input: SerializableMap<String, Serializable>): Int {
        logger.info("Adding numbers")
        return input.values.sumOf { it as Int }
    }
}

class NumberPower(
    name: String,
    receivers: List<SubscriptionReceiver> = listOf(),
    private val pow: Int
): DeliverySubscription<Int, Int>(name, receivers) {
    override fun onEvent(sender: String, input: Int): Int {
        logger.info("Raising number")
        return input.toDouble().pow(pow.toDouble()).toInt()
    }
}

class NumberPrinter(
    name: String
): ConsumerSubscription<Int>(name) {
    companion object {
        var lastResult: Int? = null
    }

    override fun onEvent(sender: String, input: Int) {
        lastResult = input
        logger.info("Generated number: $lastResult")
    }
}

class HelloWorld(
    name: String,
    delay: Long
): DetachedPeriodicalSubscription(name, delay) {
    companion object {
        val executions = ConcurrentHashMap<String, Boolean>()
    }

    override fun onEvent(sender: String, input: Unit) {
        logger.info("Hello world!")
        executions[name] = true
    }
}

class SystemTests {

    companion object {
        init {
            ConfigManager.setProperties(mapOf(
                ConfigNames.DATABASE_JDBC_URL to "jdbc:sqlite:database.db",
                ConfigNames.DATABASE_DRIVER_CLASSNAME to "org.sqlite.JDBC",
                ConfigNames.DATABASE_PASSWORD to "",
                ConfigNames.DATABASE_USERNAME to "",
                ConfigNames.DATABASE_CREATE_SCHEMAS_ON_STARTUP to "true",
                ConfigNames.CLOUD_TYPE to "ACTIVEMQ",
                ConfigNames.ACTIVEMQ_BROKER_URL to "failover:tcp://localhost:61616",
                ConfigNames.ACTIVEMQ_USERNAME to "admin",
                ConfigNames.ACTIVEMQ_PASSWORD to "admin",
                ConfigNames.PRIMARY_NAME to "master.name",
                ConfigNames.LOGGING_LEVEL to Level.DEBUG.name
            ))
        }
    }

    @Test
    fun databaseStartupFixedAllSubscribersExistsTest() {
        ConfigManager.setProperties(mapOf(ConfigNames.ALLOCATION_STRATEGY to AllocationStrategy.FIXED.name))

        DbController.clearSubscriptions()

        DbController.insertSubscriptions(listOf(
            SubscriptionEntry("helloworld-1", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-1", 1000)).toByteArray(), "subscriber-1", true, true),
            SubscriptionEntry("helloworld-2", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-2", 1000)).toByteArray(), "subscriber-2", true, true),
            SubscriptionEntry("helloworld-3", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-3", 1000)).toByteArray(), "subscriber-3", true, true),
            SubscriptionEntry("helloworld-4", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-4", 1000)).toByteArray(), "subscriber-4", true, true)
        ))

        val subscribers = listOf(
            Subscriber("subscriber-1"),
            Subscriber("subscriber-2"),
            Subscriber("subscriber-3"),
            Subscriber("subscriber-4"),
        )

        Thread.sleep(10000000)

        subscribers.forEach { it.stop() }

        for (i in 1..4) {
            assertTrue(HelloWorld.executions["helloworld-$i"] ?: false)
        }
    }

    @Test
    fun databaseStartupFixedOneSubscriberNotExistsTest() {
        ConfigManager.setProperties(mapOf(ConfigNames.ALLOCATION_STRATEGY to AllocationStrategy.FIXED.name))

        DbController.clearSubscriptions()

        DbController.insertSubscriptions(listOf(
            SubscriptionEntry("helloworld-1", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-1", 1000)).toByteArray(), "subscriber-1", true, true),
            SubscriptionEntry("helloworld-2", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-2", 1000)).toByteArray(), "subscriber-2", true, true),
            SubscriptionEntry("helloworld-3", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-3", 1000)).toByteArray(), "subscriber-3", true, true),
            SubscriptionEntry("helloworld-4", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-4", 1000)).toByteArray(), "subscriber-4", true, true)
        ))

        val subscribers = listOf(
            Subscriber("subscriber-1"),
            Subscriber("subscriber-2"),
            Subscriber("subscriber-3")
        )

        Thread.sleep(10000)

        subscribers.forEach { it.stop() }

        for (i in 1..4) {
            val executed = HelloWorld.executions["helloworld-$i"] ?: false

            if (i < 4)
                assertTrue(executed)
            else
                assertFalse(executed)
        }
    }

    @Test
    fun databaseStartupNonFixed() {
        ConfigManager.setProperties(mapOf(ConfigNames.ALLOCATION_STRATEGY to AllocationStrategy.OCCUPATION.name))

        DbController.clearSubscriptions()

        DbController.insertSubscriptions(listOf(
            SubscriptionEntry("helloworld-1", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-1", 1000)).toByteArray(), "subscriber-1", true, true),
            SubscriptionEntry("helloworld-2", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-2", 1000)).toByteArray(), "subscriber-2", true, true),
            SubscriptionEntry("helloworld-3", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-3", 1000)).toByteArray(), "subscriber-3", true, true),
            SubscriptionEntry("helloworld-4", JSONSerializer.serializeWithClassName(HelloWorld("helloworld-4", 1000)).toByteArray(), "subscriber-4", true, true)
        ))

        val subscribers = listOf(
            Subscriber("subscriber-1"),
            Subscriber("subscriber-2"),
            Subscriber("subscriber-3"),
            Subscriber("subscriber-4"),
        )

        Thread.sleep(10000)

        subscribers.forEach { it.stop() }

        for (i in 1..4) {
            assertTrue(HelloWorld.executions["helloworld-$i"] ?: false)
        }
    }

    @Test
    fun checkOperations() {


    }
}