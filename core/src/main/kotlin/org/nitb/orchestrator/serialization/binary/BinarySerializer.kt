package org.nitb.orchestrator.serialization.binary

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.nitb.orchestrator.logging.LoggingManager

object BinarySerializer {

    @Synchronized
    fun encode(message: Any): ByteArray {
        val output = Output(0, Int.MAX_VALUE)
        conf.writeClassAndObject(output, message)
        return output.toBytes()
    }

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T> decode(bytes: ByteArray): T {
        val input = Input(bytes)
        return conf.readClassAndObject(input) as T
    }

    private val logger = LoggingManager.getLogger("binary.serializer")
    private val conf by lazy {
        val kryo = Kryo()
        kryo.references = true
        kryo.isRegistrationRequired = false
        kryo
    }
    private const val classNotRegisteredMessageStart = "Class is not registered:"
}