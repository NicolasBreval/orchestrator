package org.nitb.orchestrator

import ch.qos.logback.classic.Level
import io.micronaut.http.annotation.Controller
import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.web.controllers.DisplayController
import org.reflections.Reflections
import java.nio.file.Files
import java.nio.file.Paths

@OpenAPIDefinition(
    info = Info(
        title = "Display Node API",
        version = "0.0.1",
        description = "API definition for subscribers communication",
        contact = Contact(name = "Nicolas Breval Rodriguez", email = "nicolasbrevalrodriguez@gmail.com")
    )
)
object DisplayNodeInitializer {

    fun init(vararg args: String) {
        configureLogs()

        val propertiesFile = ConfigManager.getPropertiesFileLocation()

        if (!Files.exists(Paths.get(propertiesFile))) {
            Files.createFile(Paths.get(propertiesFile))
        }

        System.setProperty("micronaut.server.cors.enabled", ConfigManager.getProperty("cors.enabled", "true"))
        System.setProperty("micronaut.config.files", propertiesFile)
        System.setProperty("micronaut.server.port", ConfigManager.getProperty(ConfigNames.HTTP_PORT, ConfigNames.HTTP_PORT_DEFAULT.toString()))

        if (ConfigManager.getBoolean(ConfigNames.OPEN_API_RESOURCE_ACTIVE)) {
            System.setProperty("micronaut.router.static-resources.swagger.paths", "classpath:META-INF/swagger")
            System.setProperty("micronaut.router.static-resources.swagger.mapping", "/swagger/**")
            System.setProperty("swagger-ui.enabled", "true")
        }

        val customArgs = args.filter { it.contains("=") }.distinctBy { it.split("=")[0] }
            .associate { it.split("=").let { parts -> Pair(parts[0], parts[1]) } }

        val version = customArgs["--version"] ?: customArgs["--version-env"]?.let { System.getenv(it) } ?: ""
        val environment = customArgs["--environment"] ?: customArgs["--environment-env"]?.let { System.getenv(it) } ?: ""

        System.setProperty("application.version", version)
        System.setProperty("application.environment", environment)
        System.setProperty("micronaut.server.cors.enabled", ConfigManager.getProperty("cors.enabled", "true"))

        Micronaut.build()
            .args(*args)
            .classes(DisplayController::class.java, *ConfigManager.getProperties(ConfigNames.CUSTOM_SUBSCRIPTIONS_PACKAGES).map { Reflections(it).getTypesAnnotatedWith(
                Controller::class.java) }.flatten().toTypedArray())
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

        if (ConfigManager.getBoolean(ConfigNames.AMQP_SHOW_LOGS)) {
            LoggingManager.setLoggerLevel("org.apache.activemq.")
        } else {
            LoggingManager.setLoggerLevel("org.apache.activemq", Level.OFF)
        }
    }

}