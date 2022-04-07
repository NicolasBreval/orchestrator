package org.nitb.orchestrator

import org.junit.Test
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.consumer.ConsumerSubscription
import org.nitb.orchestrator.subscription.delivery.*
import org.nitb.orchestrator.subscription.detached.DetachedCronSubscription
import org.nitb.orchestrator.subscription.detached.DetachedPeriodicalSubscription
import java.io.Serializable
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class SerializableDetachedPeriodicalSubscription(
    name: String,
    delay: Long,
    initialDelay: Long = 0,
    timeout: Long = -1,
    description: String? = null
): DetachedPeriodicalSubscription(name, delay, initialDelay, timeout, description) {
    override fun onEvent(sender: String, input: Unit) {
        // do nothing
    }
}

class SerializableDetachedCronSubscription(
    name: String,
    cronExpression: String,
    timeout: Long = -1,
    description: String? = null
): DetachedCronSubscription(name, cronExpression, timeout, description) {
    override fun onEvent(sender: String, input: Unit) {
        // do nothing
    }
}

class SerializableDeliveryPeriodicalSubscription(
    name: String,
    delay: Long,
    initialDelay: Long = 0,
    receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null
): DeliveryPeriodicalSubscription<Serializable>(name, delay, initialDelay, receivers, timeout, description) {
    override fun onEvent(sender: String, input: Unit): Serializable? {
        return null
    }
}

class SerializableDeliveryCronSubscription(
    name: String,
    cronExpression: String,
    receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null
): DeliveryCronSubscription<Serializable>(name, cronExpression, receivers, timeout, description) {
    override fun onEvent(sender: String, input: Unit): Serializable? {
        return null
    }
}

class SerializableDeliverySubscription(
    name: String,
    receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null
): DeliverySubscription<Serializable, Serializable>(name, receivers, timeout, description) {
    override fun onEvent(sender: String, input: Serializable): Serializable? {
        return null
    }
}

class SerializableDeliveryMultiInputSubscription(
    name: String,
    receivers: List<SubscriptionReceiver>,
    senders: List<String>,
    limit: Int = Int.MAX_VALUE,
    timeout: Long = -1,
    description: String? = null
): DeliveryMultiInputSubscription<Serializable>(name, receivers, senders, limit, timeout, description) {
    override fun onEvent(sender: String, input: SerializableMap<String, Serializable>): Serializable? {
        return null
    }
}

class SerializableConsumerSubscription(
    name: String,
    timeout: Long = -1,
    description: String? = null
): ConsumerSubscription<Serializable>(name, timeout, description) {
    override fun onEvent(sender: String, input: Serializable) {
        // do nothing
    }
}

class SerializationTests {

    @Test
    fun binarySerialization() {
        val serializableDetachedPeriodicalSubscription = SerializableDetachedPeriodicalSubscription("serializableDetachedPeriodicalSubscription", 100, 0, -1, "")
        val serializableDetachedCronSubscription = SerializableDetachedCronSubscription("serializableDetachedCronSubscription", "* * * * *", -1, "")
        val serializableDeliverySubscription = SerializableDeliverySubscription("serializableDeliverySubscription", listOf(), -1, "")
        val serializableDeliveryPeriodicalSubscription = SerializableDeliveryPeriodicalSubscription("serializableDeliveryPeriodicalSubscription", 100, 0, listOf(), -1, "")
        val serializableDeliveryCronSubscription = SerializableDeliveryCronSubscription("serializableDeliveryCronSubscription", "* * * * *", listOf(), -1, "")
        val serializableDeliveryMultiInputSubscription = SerializableDeliveryMultiInputSubscription("serializableDeliveryMultiInputSubscription", listOf(), listOf(), 10, -1, "")
        val serializableConsumerSubscription = SerializableConsumerSubscription("serializableConsumerSubscription", -1, null)

        // region SERIALIZATION

        val binaryDetachedPeriodicalSubscription = BinarySerializer.serialize(serializableDetachedPeriodicalSubscription)
        assertNotNull(binaryDetachedPeriodicalSubscription)
        assertNotEquals(binaryDetachedPeriodicalSubscription.size, 0)

        val binaryDetachedCronSubscription = BinarySerializer.serialize(serializableDetachedCronSubscription)
        assertNotNull(binaryDetachedCronSubscription)
        assertNotEquals(binaryDetachedCronSubscription.size, 0)

        val binaryDeliverySubscription = BinarySerializer.serialize(serializableDeliverySubscription)
        assertNotNull(binaryDeliverySubscription)
        assertNotEquals(binaryDeliverySubscription.size, 0)

        val binaryDeliveryPeriodicalSubscription = BinarySerializer.serialize(serializableDeliveryPeriodicalSubscription)
        assertNotNull(binaryDeliveryPeriodicalSubscription)
        assertNotEquals(binaryDeliveryPeriodicalSubscription.size, 0)

        val binaryDeliveryCronSubscription = BinarySerializer.serialize(serializableDeliveryCronSubscription)
        assertNotNull(binaryDeliveryCronSubscription)
        assertNotEquals(binaryDeliveryCronSubscription.size, 0)

        val binaryDeliveryMultiInputSubscription = BinarySerializer.serialize(serializableDeliveryMultiInputSubscription)
        assertNotNull(binaryDeliveryMultiInputSubscription)
        assertNotEquals(binaryDeliveryMultiInputSubscription.size, 0)

        val binaryConsumerSubscription = BinarySerializer.serialize(serializableConsumerSubscription)
        assertNotNull(binaryConsumerSubscription)
        assertNotEquals(binaryConsumerSubscription.size, 0)

        // endregion

        // region DESERIALIZATION

        val deserializedDetachedPeriodicalSubscription = BinarySerializer.deserialize<SerializableDetachedPeriodicalSubscription>(binaryDetachedPeriodicalSubscription)
        assertEquals(deserializedDetachedPeriodicalSubscription, serializableDetachedPeriodicalSubscription)

        val deserializedDetachedCronSubscription = BinarySerializer.deserialize<DetachedCronSubscription>(binaryDetachedCronSubscription)
        assertEquals(deserializedDetachedCronSubscription, serializableDetachedCronSubscription)

        val deserializedDeliverySubscription = BinarySerializer.deserialize<DeliverySubscription<*, *>>(binaryDeliverySubscription)
        assertEquals(deserializedDeliverySubscription, serializableDeliverySubscription)

        val deserializedDeliveryPeriodicalSubscription = BinarySerializer.deserialize<DeliveryPeriodicalSubscription<*>>(binaryDeliveryPeriodicalSubscription)
        assertEquals(deserializedDeliveryPeriodicalSubscription, serializableDeliveryPeriodicalSubscription)

        val deserializedDeliveryCronSubscription = BinarySerializer.deserialize<DeliveryCronSubscription<*>>(binaryDeliveryCronSubscription)
        assertEquals(deserializedDeliveryCronSubscription, serializableDeliveryCronSubscription)

        val deserializedDeliveryMultiInputSubscription = BinarySerializer.deserialize<DeliveryMultiInputSubscription<*>>(binaryDeliveryMultiInputSubscription)
        assertEquals(deserializedDeliveryMultiInputSubscription, serializableDeliveryMultiInputSubscription)

        val deserializedConsumerSubscription = BinarySerializer.deserialize<ConsumerSubscription<*>>(binaryConsumerSubscription)
        assertEquals(deserializedConsumerSubscription, serializableConsumerSubscription)

        // endregion

    }

}