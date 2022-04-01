package org.nitb.orchestrator.serialization.binary

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.nitb.orchestrator.cloud.CloudMessage
import org.nitb.orchestrator.logging.LoggingManager
import java.lang.Exception
import java.math.BigInteger

object BinarySerializer {

    private val conf = ThreadLocal.withInitial {
        val kryo = Kryo()
        kryo.register(ByteArray::class.java)
        kryo.register(Array<ByteArray>::class.java)
        kryo.register(CloudMessage::class.java)
        kryo.register(BigInteger::class.java)
        kryo
    }

    fun encode(message: Any): ByteArray {
        try {
            val output = Output(0, Int.MAX_VALUE)
            conf.get().writeClassAndObject(output, message)
            return output.toBytes()
        } catch (e: Exception) {
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> decode(bytes: ByteArray): T {
        try {
            val input = Input(bytes)
            return conf.get().readClassAndObject(input) as T
        } catch (e: Exception) {
            throw e
        }
    }

    private val logger = LoggingManager.getLogger("binary.serializer")
}