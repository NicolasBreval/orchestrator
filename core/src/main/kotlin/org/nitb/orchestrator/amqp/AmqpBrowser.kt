package org.nitb.orchestrator.amqp

import org.reflections.Reflections
import java.io.Serializable
import org.nitb.orchestrator.config.ConfigNames

/**
 * Used to get AMQP client based on [ConfigNames.AMQP_TYPE] property, used to load selected AMQP type dependency and
 * create a new AMQP client. You must add the Orchestrator's AMQP dependency related to type of [ConfigNames.AMQP_TYPE]
 * property, else raises an exception.
 */
object AmqpBrowser {

    // region PRIVATE PROPERTIES

    /**
     * List of all clients loaded in project.
     */
    private val amqpClients = Reflections("org.nitb.orchestrator.amqp").getSubTypesOf(AmqpClient::class.java).filter { it.isAnnotationPresent(AmqpType::class.java) }

    // endregion

    // region PUBLIC METHODS

    /**
     * Method used to create a new AMQP client based on [ConfigNames.AMQP_TYPE] property.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Serializable> getAmqpClient(type: String, name: String, consumers: Int = 1): AmqpClient<T> {
        return amqpClients.first { it.getAnnotation(AmqpType::class.java).type.lowercase() == type.lowercase() }?.getConstructor(String::class.java, Int::class.java)?.newInstance(name, consumers) as AmqpClient<T>
    }

    // endregion
}