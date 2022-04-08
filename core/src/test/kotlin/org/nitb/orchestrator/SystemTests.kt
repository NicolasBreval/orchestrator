package org.nitb.orchestrator

import com.cronutils.model.CronType
import org.junit.Test
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.Subscriber
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionsRequest
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.consumer.ConsumerSubscription
import org.nitb.orchestrator.subscription.delivery.*
import java.io.Serializable
import kotlin.math.pow
import kotlin.test.assertEquals

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
                ConfigNames.ALLOCATION_STRATEGY to AllocationStrategy.OCCUPATION.name
            ))
        }
    }

    @Test(timeout = 10000)
    fun systemTest() {

        val sender = object : Sender("test") {}

        val subscribers = (0..2).map { Subscriber() }

        val subscriptions = listOf(
            NumberGeneratorPeriodical("numberGeneratorPeriodical", 1000, 1000, listOf(SubscriptionReceiver("numbersAdder")), 10),
            NumberGeneratorCron("numberGeneratorCron", "* * * * * ?", listOf(SubscriptionReceiver("numbersAdder")), 20),
            NumbersAdder("numbersAdder", listOf(SubscriptionReceiver("numberPower")), listOf("numberGeneratorPeriodical", "numberGeneratorCron")),
            NumberPower("numberPower", listOf(SubscriptionReceiver("numberPrinter")), 2),
            NumberPrinter("numberPrinter")
        )

        sender.send(UploadSubscriptionsRequest(subscriptions.map { JSONSerializer.serializeWithClassName(it) }), ConfigManager.getProperty(ConfigNames.PRIMARY_NAME)!!)

        while (NumberPrinter.lastResult == null) {
            Thread.sleep(100)
        }

        subscribers.forEach { it.stop() }

        sender.close()

        assertEquals(NumberPrinter.lastResult, 900)
    }

}