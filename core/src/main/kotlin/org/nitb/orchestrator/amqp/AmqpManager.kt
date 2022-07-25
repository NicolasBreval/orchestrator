package org.nitb.orchestrator.amqp

import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import java.io.Serializable

/**
 * Interface used to make easy [AmqpClient] operations
 */
interface AmqpManager<T: Serializable> {

    // region PUBLIC METHODS

    /**
     * Creates a new [AmqpClient] object with specified name.
     * @param name Name of queue related to client.
     */
    fun createClient(name: String, consumers: Int = 1): AmqpClient<T> {
        return ConfigManager.getProperty(ConfigNames.AMQP_TYPE)?.let { AmqpBrowser.getAmqpClient(it, name, consumers) } ?: error("Required property ${ConfigNames.AMQP_TYPE} doesn't exists")
    }

    /**
     * Checks if master node queue has any consumer
     */
    fun masterConsuming(client: AmqpClient<T>): Boolean {
        return client.masterConsuming()
    }

    /**
     * Determines if current AMQP connection related to client is opene or not.
     */
    fun amqpConnectionIsOpen(client: AmqpClient<T>): Boolean {
        return client.isConnected()
    }

    // endregion
}