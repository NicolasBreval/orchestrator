package org.nitb.orchestrator.web.initializer

import ch.qos.logback.classic.Level
import io.micronaut.runtime.Micronaut
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager

object MicronautServerInitializer {

    fun init(vararg args: String) {
        configureLogs()

        System.setProperty("micronaut.config.files", ConfigManager.getPropertiesFileLocation())
        System.setProperty("micronaut.server.port", ConfigManager.getProperty(ConfigNames.HTTP_PORT, ConfigNames.HTTP_PORT_DEFAULT.toString()))

        Micronaut.build()
            .args(*args)
            .packages("org.nitb.orchestrator.web.controllers")
            .eagerInitSingletons(true)
            .start()
    }

    private fun configureLogs() {
        LoggingManager.setLoggerLevel("io.micronaut")
        LoggingManager.setLoggerLevel("io.netty")
        LoggingManager.setLoggerLevel("reactor")

        if (ConfigManager.getBoolean(ConfigNames.DATABASE_SHOW_LOGS)) {
            LoggingManager.setLoggerLevel("com.zaxxer")
            LoggingManager.setLoggerLevel("Exposed")
        } else {
            LoggingManager.setLoggerLevel("com.zaxxer", Level.OFF)
            LoggingManager.setLoggerLevel("Exposed", Level.OFF)
        }

        if (ConfigManager.getBoolean(ConfigNames.CLOUD_SHOW_LOGS)) {
            LoggingManager.setLoggerLevel("org.apache.activemq.")
        } else {
            LoggingManager.setLoggerLevel("org.apache.activemq", Level.OFF)
        }
    }
}