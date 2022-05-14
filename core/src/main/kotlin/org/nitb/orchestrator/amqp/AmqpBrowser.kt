package org.nitb.orchestrator.amqp

import org.reflections.Reflections
import java.io.Serializable

object AmqpBrowser {

    private val amqpClients = Reflections("org.nitb.orchestrator.amqp").getSubTypesOf(AmqpClient::class.java).filter { it.isAnnotationPresent(AmqpType::class.java) }

    @Suppress("UNCHECKED_CAST")
    fun <T: Serializable> getAmqpClient(type: String, name: String): AmqpClient<T> {
        return amqpClients.first { it.getAnnotation(AmqpType::class.java).type.lowercase() == type.lowercase() }?.getConstructor(String::class.java)?.newInstance(name) as AmqpClient<T>
    }
}