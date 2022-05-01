package org.nitb.orchestrator

import org.junit.Test
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy
import org.nitb.orchestrator.web.initializr.ApiInitializer
import org.slf4j.event.Level

class CompleteTest {

    companion object {
        init {
            ConfigManager.setProperties(mapOf(
                ConfigNames.DATABASE_JDBC_URL to "jdbc:sqlite:database.db",
                ConfigNames.DATABASE_DRIVER_CLASSNAME to "org.sqlite.JDBC",
                ConfigNames.DATABASE_PASSWORD to "",
                ConfigNames.DATABASE_USERNAME to "",
                ConfigNames.DATABASE_CREATE_SCHEMAS_ON_STARTUP to "true",
                ConfigNames.CLOUD_TYPE to "ACTIVEMQ",
                ConfigNames.ACTIVEMQ_BROKER_URL to "failover:tcp://localhost:61616",
                ConfigNames.ACTIVEMQ_USERNAME to "admin",
                ConfigNames.ACTIVEMQ_PASSWORD to "admin",
                ConfigNames.PRIMARY_NAME to "master.name",
                ConfigNames.LOGGING_LEVEL to Level.DEBUG.name,
                ConfigNames.ALLOCATION_STRATEGY to AllocationStrategy.OCCUPATION.name
            ))
        }
    }

    @Test
    fun test() {
        ApiInitializer.initialize()

        Thread.sleep(Long.MAX_VALUE)
    }


}