package org.nitb.orchestrator.examples

import org.nitb.orchestrator.cloud.activemq.ActiveMqCloudClient
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.Subscriber
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionsRequest
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.consumer.ConsumerSubscription
import org.nitb.orchestrator.subscription.delivery.DeliveryMultiInputSubscription
import org.nitb.orchestrator.subscription.delivery.DeliveryPeriodicalSubscription
import org.nitb.orchestrator.subscription.delivery.SerializableMap
import org.nitb.orchestrator.subscription.detached.DetachedPeriodicalSubscription
import java.io.Serializable
import java.lang.RuntimeException
import java.math.BigInteger
import kotlin.math.log
import kotlin.random.Random

class PeriodicalRandomNumberGenerator(
    name: String,
    delay: Long,
    receivers: List<SubscriptionReceiver>
): DeliveryPeriodicalSubscription<Int>(name, delay, 0, receivers, -1, null) {
    @Transient
    private val random = Random(System.nanoTime())

    override fun onEvent(sender: String, input: Unit): Int {
        val value = random.nextInt()
        logger.info("Sent value: $value")
        return value
    }
}

class Adder(
    name: String,
    receivers: List<SubscriptionReceiver>,
    senders: List<String>
): DeliveryMultiInputSubscription<Int>(name, receivers, senders) {
    override fun onEvent(sender: String, input: SerializableMap<String, Serializable>): Int {
        return input.values.map { it as Int }.reduce { acc, i -> acc + i  }
    }
}

class Printer(
    name: String
): ConsumerSubscription<Int>(name) {
    override fun onEvent(sender: String, input: Int) {
        logger.info("Data received: $input")
    }
}

class HelloWorld(
    name: String,
    delay: Long
): DetachedPeriodicalSubscription(name, delay) {
    override fun onEvent(sender: String, input: Unit) {
        logger.info("Hello world!")
    }
}

class HugeOperations(
    name: String,
    delay: Long,
    private val logs: Int
): DetachedPeriodicalSubscription(name, delay) {
    override fun onEvent(sender: String, input: Unit) {
        var result = Random(System.nanoTime()).nextDouble()

        for (i in 0..logs) {
            result = log(result, Random(System.nanoTime()).nextDouble())
        }

        logger.info("Number calculated $result")
    }
}

fun main(args: Array<String>) {
    val client = ActiveMqCloudClient<Serializable>("example")

    val node1 = Subscriber()
    val node2 = Subscriber()

//    val subscriptions = listOf(
//        JSONSerializer.serializeWithClassName(PeriodicalRandomNumberGenerator("number.generator.1", 2000, listOf(SubscriptionReceiver("adder")))),
//        JSONSerializer.serializeWithClassName(PeriodicalRandomNumberGenerator("number.generator.2", 2000, listOf(SubscriptionReceiver("adder")))),
//        JSONSerializer.serializeWithClassName(Adder("adder", listOf(SubscriptionReceiver("printer")), listOf("number.generator.1", "number.generator.2"))),
//        JSONSerializer.serializeWithClassName(Printer("printer")),
//        JSONSerializer.serializeWithClassName(HelloWorld("helloworld-1", 5000)),
//        JSONSerializer.serializeWithClassName(HelloWorld("helloworld-2", 5000)),
//        JSONSerializer.serializeWithClassName(HelloWorld("helloworld-3", 5000)),
//        JSONSerializer.serializeWithClassName(HelloWorld("helloworld-4", 5000)),
//        JSONSerializer.serializeWithClassName(HelloWorld("helloworld-5", 5000)),
//        JSONSerializer.serializeWithClassName(HugeOperations("prime-numbers-1", 10000, 9)),
//        JSONSerializer.serializeWithClassName(HugeOperations("prime-numbers-2", 10000, 99)),
//        JSONSerializer.serializeWithClassName(HugeOperations("prime-numbers-3", 10000, 999)),
//        JSONSerializer.serializeWithClassName(HugeOperations("prime-numbers-4", 10000, 9999)),
//        JSONSerializer.serializeWithClassName(HugeOperations("prime-numbers-5", 10000, 99999)),
//        JSONSerializer.serializeWithClassName(HugeOperations("prime-numbers-6", 10000, 999999)),
//        JSONSerializer.serializeWithClassName(HugeOperations("prime-numbers-7", 10000, 9999999))
//    )

//    val subscriptions = listOf(
//        JSONSerializer.serializeWithClassName(HelloWorld("helloworld-6", 5000))
//    )
//
//    client.send(ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("Property doesn't exists")), UploadSubscriptionsRequest(subscriptions))

//    Thread.sleep(2000)
//
//    node1.stop()
//    node2.stop()
//    client.close()

}