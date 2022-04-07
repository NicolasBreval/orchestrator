package org.nitb.orchestrator

import com.cronutils.model.CronType
import org.junit.Test
import org.nitb.orchestrator.cloud.*
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.consumer.ConsumerSubscription
import org.nitb.orchestrator.subscription.delivery.*
import org.nitb.orchestrator.subscription.detached.DetachedCronSubscription
import org.nitb.orchestrator.subscription.detached.DetachedPeriodicalSubscription
import java.io.Serializable
import kotlin.test.assertEquals

abstract class SenderAndConsumer<T: Serializable>(
    name: String
): CloudManager<T>, CloudSender, CloudConsumer<T> {
    private val client by lazy { createClient(name) }

    fun start() {
        registerConsumer(client) { cloudMessage ->
            onConsume(cloudMessage)
        }
    }

    fun stop() {
        client.close()
    }

    fun send(value: T, receiver: String) {
        sendMessage(value, client, receiver)
    }

    abstract fun onConsume(message: CloudMessage<T>)

}

class SubscriptionsTests {

    companion object {
        init {
            ConfigManager.clearProperties()
            ConfigManager.setProperties(mapOf(
                ConfigNames.CLOUD_TYPE to "ACTIVEMQ",
                ConfigNames.ACTIVEMQ_BROKER_URL to "failover:tcp://localhost:61616",
                ConfigNames.ACTIVEMQ_USERNAME to "admin",
                ConfigNames.ACTIVEMQ_PASSWORD to "admin",
                ConfigNames.PRIMARY_NAME to "master.name"
            ))
        }
    }

    @Test
    fun detachedPeriodicalSubscriptionTest() {
        val detachedPeriodicalSubscription = object : DetachedPeriodicalSubscription(
            "detachedPeriodicalSubscription",
            100,
            200
        ) {
            var count: Int = 0

            override fun onEvent(sender: String, input: Unit) {
                count++
            }
        }

        detachedPeriodicalSubscription.start()

        Thread.sleep(1000)

        detachedPeriodicalSubscription.stop()

        assertEquals(detachedPeriodicalSubscription.count, 8)
    }

    @Test
    fun detachedCronSubscriptionTest() {
        val detachedCronSubscription = object : DetachedCronSubscription(
            "detachedCronSubscription",
            "* * * * * ?",
            CronType.QUARTZ
        ) {
            var count: Int = 0

            override fun onEvent(sender: String, input: Unit) {
                count++
            }
        }

        detachedCronSubscription.start()

        Thread.sleep(1500)

        detachedCronSubscription.stop()

        assertEquals(detachedCronSubscription.count, 1)
    }

    @Test(timeout = 3000)
    fun deliverySubscriptionTest() {
        val deliverySubscription = object : DeliverySubscription<Int, Int>(
            "delivery.subscription",
            listOf(SubscriptionReceiver("test.delivery.subscription"))
        ) {
            override fun onEvent(sender: String, input: Int): Int {
                return input * 2
            }
        }

        val senderAndConsumer = object : SenderAndConsumer<Int>("test.delivery.subscription") {
            var count = 0

            override fun onConsume(message: CloudMessage<Int>) {
                count += message.message
            }
        }

        deliverySubscription.start()
        senderAndConsumer.start()

        for (i in 0..100) {
            senderAndConsumer.send(i, "delivery.subscription")
        }

        val result = (0..100).sumOf { it * 2 }

        while (senderAndConsumer.count < result) {
            Thread.sleep(100)
        }

        deliverySubscription.stop()
        senderAndConsumer.stop()

        assertEquals(senderAndConsumer.count, result)
    }

    @Test(timeout = 10000)
    fun deliveryPeriodicalSubscriptionTest() {
        val deliveryPeriodicalSubscription = object : DeliveryPeriodicalSubscription<Int>(
            "deliveryPeriodicalSubscription",
            100,
            0,
            listOf(SubscriptionReceiver("test.delivery.subscription"))
        ) {
            override fun onEvent(sender: String, input: Unit): Int {
                return 10
            }
        }

        val senderAndConsumer = object : SenderAndConsumer<Int>("test.delivery.subscription") {
            var count = 0

            override fun onConsume(message: CloudMessage<Int>) {
                count += message.message
            }
        }

        senderAndConsumer.start()
        deliveryPeriodicalSubscription.start()

        while (senderAndConsumer.count < 50) {
            Thread.sleep(100)
        }

        deliveryPeriodicalSubscription.stop()
        senderAndConsumer.stop()
    }

    @Test(timeout = 10000)
    fun deliveryCronSubscriptionTest() {
        val deliveryCronSubscription = object : DeliveryCronSubscription<Int>(
            "deliveryCronSubscription",
            "* * * * * ?",
            listOf(SubscriptionReceiver("test.delivery.subscription")),
            CronType.QUARTZ
        ) {
            override fun onEvent(sender: String, input: Unit): Int {
                return 10
            }
        }

        val senderAndConsumer = object : SenderAndConsumer<Int>("test.delivery.subscription") {
            var count = 0

            override fun onConsume(message: CloudMessage<Int>) {
                count += message.message
            }
        }

        senderAndConsumer.start()
        deliveryCronSubscription.start()

        while (senderAndConsumer.count < 20) {
            Thread.sleep(1000)
        }

        deliveryCronSubscription.stop()
        senderAndConsumer.stop()
    }

    @Test(timeout = 10000)
    fun deliveryMultiInputSubscriptionTest() {
        val multiInputSubscription = object : DeliveryMultiInputSubscription<Int>(
            "multiInputSubscription",
            listOf(SubscriptionReceiver("test.delivery.subscription")),
            listOf("first", "second", "third")
        ) {
            override fun onEvent(sender: String, input: SerializableMap<String, Serializable>): Int {
                return input.values.sumOf { it as Int }
            }
        }

        val first = object : SenderAndConsumer<Int>("first") {
            override fun onConsume(message: CloudMessage<Int>) {}
        }

        val second = object : SenderAndConsumer<Int>("second") {
            override fun onConsume(message: CloudMessage<Int>) {}
        }

        val third = object : SenderAndConsumer<Int>("third") {
            override fun onConsume(message: CloudMessage<Int>) {}
        }

        val consumer = object : SenderAndConsumer<Int>("test.delivery.subscription") {
            var count = 0

            override fun onConsume(message: CloudMessage<Int>) {
                count += message.message
            }
        }

        first.start()
        second.start()
        third.start()
        consumer.start()
        multiInputSubscription.start()

        first.send(3, "multiInputSubscription")
        second.send(4, "multiInputSubscription")
        third.send(5, "multiInputSubscription")

        while (consumer.count == 0) {
            Thread.sleep(100)
        }

        first.stop()
        second.stop()
        third.stop()
        consumer.stop()
        multiInputSubscription.stop()

        assertEquals(consumer.count, 12)

    }

    @Test(timeout = 10000)
    fun consumerSubscriptionTest() {
        val consumerSubscription = object : ConsumerSubscription<Int>(
            "consumerSubscription"
        ) {
            var count: Int = 0

            override fun onEvent(sender: String, input: Int) {
                count += input
            }
        }

        val sender = object : SenderAndConsumer<Int>("sender") {
            override fun onConsume(message: CloudMessage<Int>) {
                // do nothing
            }
        }

        sender.start()
        consumerSubscription.start()

        for (i in 0..100) {
            sender.send(i, "consumerSubscription")
        }

        while (consumerSubscription.count < (0..100).sum()) {
            Thread.sleep(100)
        }

        sender.stop()
        consumerSubscription.stop()
    }
}