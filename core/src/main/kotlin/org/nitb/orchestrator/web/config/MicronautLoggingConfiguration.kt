package org.nitb.orchestrator.web.config

import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.nitb.orchestrator.logging.LoggingManager

@Singleton
class MicronautLoggingConfiguration {

    @EventListener
    fun configureLogging(startupEvent: StartupEvent) {
        LoggingManager.setLoggerLevel("ROOT")
    }

}