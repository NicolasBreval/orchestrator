package org.nitb.orchestrator.serialization.binary

import com.caucho.hessian.io.Hessian2Input
import com.caucho.hessian.io.Hessian2Output
import org.nitb.orchestrator.serialization.json.JSONSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Serializable

/**
 * Allows serializing objects to a byte array.
 */
object BinarySerializer {

    /**
     * Serializes an object to a [ByteArray].
     * @param message Object to be serialized.
     */
    fun serialize(message: Any): ByteArray {
        try {
            ByteArrayOutputStream().use {
                val hessian2Output = Hessian2Output(it)
                hessian2Output.startMessage()
                hessian2Output.writeObject(message)
                hessian2Output.bytesOutputStream.flush()
                hessian2Output.completeMessage()
                hessian2Output.close()
                return it.toByteArray()
            }
        } catch (e: java.lang.Error) {
            return JSONSerializer.serializeWithClassName(message).toByteArray()
        } catch (e: Exception) {
            return JSONSerializer.serializeWithClassName(message).toByteArray()
        }
    }

    /**
     * Deserializes an object from a [ByteArray].
     * @param bytes Bytes to be deserialized to an object.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> deserialize(bytes: ByteArray): T {
        try {
            ByteArrayInputStream(bytes).use {
                val hessian2Input = Hessian2Input(it)
                hessian2Input.startMessage()
                val obj = hessian2Input.readObject() as T
                hessian2Input.completeMessage()
                hessian2Input.close()
                return obj
            }
        } catch (e: java.lang.Error) {
            return JSONSerializer.deserializeWithClassName(String(bytes)) as T
        } catch (e: java.lang.Exception) {
            return JSONSerializer.deserializeWithClassName(String(bytes)) as T
        }
    }
}