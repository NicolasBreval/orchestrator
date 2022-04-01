package org.nitb.orchestrator.cloud

import org.nitb.orchestrator.cloud.activemq.ActiveMqCloudClient
import org.nitb.orchestrator.cloud.rabbitmq.RabbitMqCloudClient
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import java.io.Serializable
import java.lang.RuntimeException

interface CloudManager<T: Serializable> {

    fun createClient(name: String): CloudClient<T> {
        return when (ConfigManager.getEnumProperty(ConfigNames.CLOUD_TYPE, CloudType::class.java, RuntimeException("Invalid value for property ${ConfigNames.CLOUD_TYPE}"))) {
            CloudType.ACTIVEMQ -> ActiveMqCloudClient(name, true)
            CloudType.RABBITMQ -> RabbitMqCloudClient(name)
        }
    }
}