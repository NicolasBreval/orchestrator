package org.nitb.orchestrator.serialization.binary

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

/**
 * Allows serializing objects to a byte array.
 */
object BinarySerializer {

    /**
     * Serializes an object to a [ByteArray].
     * @param message Object to be serialized.
     */
    fun serialize(message: Any): ByteArray {
        val output = Output(0, Int.MAX_VALUE)
        conf.get().writeClassAndObject(output, message)
        return output.toBytes()
    }

    /**
     * Deserializes an object from a [ByteArray].
     * @param bytes Bytes to be deserialized to an object.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> deserialize(bytes: ByteArray): T {
        val input = Input(bytes)
        return conf.get().readClassAndObject(input) as T
    }

    /**
     * Kryo object used for serialization.
     */
    private val conf = ThreadLocal.withInitial {
        val kryo = Kryo()
        kryo.references = true
        kryo.isRegistrationRequired = false
        kryo
    }
}