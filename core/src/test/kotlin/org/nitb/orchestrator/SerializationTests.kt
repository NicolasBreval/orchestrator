package org.nitb.orchestrator

import org.junit.Test
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscription.SubscriptionReceiver
import org.nitb.orchestrator.subscription.consumer.ConsumerSubscription
import org.nitb.orchestrator.subscription.delivery.*
import org.nitb.orchestrator.subscription.detached.DetachedCronSubscription
import org.nitb.orchestrator.subscription.detached.DetachedPeriodicalSubscription
import java.io.Serializable
import kotlin.test.*

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

    companion object {
        val serializableDetachedPeriodicalSubscription = SerializableDetachedPeriodicalSubscription("serializableDetachedPeriodicalSubscription", 100, 0, -1, "")
        val serializableDetachedCronSubscription = SerializableDetachedCronSubscription("serializableDetachedCronSubscription", "* * * * *", -1, "")
        val serializableDeliverySubscription = SerializableDeliverySubscription("serializableDeliverySubscription", listOf(), -1, "")
        val serializableDeliveryPeriodicalSubscription = SerializableDeliveryPeriodicalSubscription("serializableDeliveryPeriodicalSubscription", 100, 0, listOf(), -1, "")
        val serializableDeliveryCronSubscription = SerializableDeliveryCronSubscription("serializableDeliveryCronSubscription", "* * * * *", listOf(), -1, "")
        val serializableDeliveryMultiInputSubscription = SerializableDeliveryMultiInputSubscription("serializableDeliveryMultiInputSubscription", listOf(), listOf(), 10, -1, "")
        val serializableConsumerSubscription = SerializableConsumerSubscription("serializableConsumerSubscription", -1, null)
    }

    @Test
    fun binarySerialization() {

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

    @Test
    fun jsonSerialization() {

        // region SERIALIZATION

        val simple = "Simple object"
        val complex1 = listOf("A", "B", 2, 4, 8.67)
        val complex2 = mapOf("1" to 1, "56" to 9.0, "[a]" to true)

        val serializedSimple = JSONSerializer.serialize(simple)
        val serializedComplex1 = JSONSerializer.serialize(complex1)
        val serializedComplex2 = JSONSerializer.serialize(complex2)

        // endregion

        // region DESERIALIZATION

        val deserializedSimple = JSONSerializer.deserialize(serializedSimple, String::class.java)
        val deserializedComplex1 = JSONSerializer.deserialize(serializedComplex1, List::class.java)
        val deserializedComplex2 = JSONSerializer.deserialize(serializedComplex2, Map::class.java)

        assertEquals(deserializedSimple, simple)
        assertEquals(deserializedComplex1, complex1)
        assertEquals(deserializedComplex2, complex2)

        // endregion

        // region SERIALIZATION WITH CLASSNAME

        val jsonDetachedPeriodicalSubscription = JSONSerializer.serializeWithClassName(serializableDetachedPeriodicalSubscription)
        val jsonDetachedCronSubscription = JSONSerializer.serializeWithClassName(serializableDetachedCronSubscription)
        val jsonDeliverySubscription = JSONSerializer.serializeWithClassName(serializableDeliverySubscription)
        val jsonDeliveryPeriodicalSubscription = JSONSerializer.serializeWithClassName(serializableDeliveryPeriodicalSubscription)
        val jsonDeliveryCronSubscription = JSONSerializer.serializeWithClassName(serializableDeliveryCronSubscription)
        val jsonDeliveryMultiInputSubscription = JSONSerializer.serializeWithClassName(serializableDeliveryMultiInputSubscription)
        val jsonConsumerSubscription = JSONSerializer.serializeWithClassName(serializableConsumerSubscription)

        // endregion

        // region DESERIALIZATION WITH CLASSNAME

        val deserializedDetachedPeriodicalSubscription = JSONSerializer.deserializeWithClassName(jsonDetachedPeriodicalSubscription)
        assertEquals(deserializedDetachedPeriodicalSubscription, serializableDetachedPeriodicalSubscription)

        val deserializedDetachedCronSubscription = JSONSerializer.deserializeWithClassName(jsonDetachedCronSubscription)
        assertEquals(deserializedDetachedCronSubscription, serializableDetachedCronSubscription)

        val deserializedDeliverySubscription = JSONSerializer.deserializeWithClassName(jsonDeliverySubscription)
        assertEquals(deserializedDeliverySubscription, serializableDeliverySubscription)

        val deserializedDeliveryPeriodicalSubscription = JSONSerializer.deserializeWithClassName(jsonDeliveryPeriodicalSubscription)
        assertEquals(deserializedDeliveryPeriodicalSubscription, serializableDeliveryPeriodicalSubscription)

        val deserializedDeliveryCronSubscription = JSONSerializer.deserializeWithClassName(jsonDeliveryCronSubscription)
        assertEquals(deserializedDeliveryCronSubscription, serializableDeliveryCronSubscription)

        val deserializedDeliveryMultiInputSubscription = JSONSerializer.deserializeWithClassName(jsonDeliveryMultiInputSubscription)
        assertEquals(deserializedDeliveryMultiInputSubscription, serializableDeliveryMultiInputSubscription)

        val deserializedConsumerSubscription = JSONSerializer.deserializeWithClassName(jsonConsumerSubscription)
        assertEquals(deserializedConsumerSubscription, serializableConsumerSubscription)

        // endregion

    }

}