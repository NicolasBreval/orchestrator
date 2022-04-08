package org.nitb.orchestrator.cloud

import org.nitb.orchestrator.cloud.activemq.ActiveMqCloudClient
import org.nitb.orchestrator.cloud.rabbitmq.RabbitMqCloudClient
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import java.io.Serializable
import java.lang.RuntimeException

/**
 * Interface used to make easy [CloudClient] operations
 */
interface CloudManager<T: Serializable> {

    // region PUBLIC METHODS

    /**
     * Creates a new [CloudClient] object with specified name.
     * @param name Name of queue related to client.
     */
    fun createClient(name: String): CloudClient<T> {
        return when (ConfigManager.getEnumProperty(ConfigNames.CLOUD_TYPE, CloudType::class.java, RuntimeException("Invalid value for property ${ConfigNames.CLOUD_TYPE}"))) {
            CloudType.ACTIVEMQ -> ActiveMqCloudClient(name, true)
            CloudType.RABBITMQ -> RabbitMqCloudClient(name)
        }
    }

    /**
     * Checks if master node queue has any consumer
     */
    fun masterConsuming(client: CloudClient<T>): Boolean {
        return client.masterConsuming()
    }

    // endregion
}