package org.nitb.logging.appenders

import org.fluentd.logger.FluentLogger
import org.nitb.orchestrator.annotations.AppenderName
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.AppenderPlugin
import org.nitb.orchestrator.logging.LogEvent
import java.lang.RuntimeException
import java.net.InetAddress

@AppenderName("FLUENTD_APPENDER")
class FluentdAppenderPlugin(
    loggerName: String
): AppenderPlugin(loggerName) {

    companion object {
        // region PRIVATE

        /**
         * Hostname used to connect with Fluentd server.
         */
        private val fluentdHost = ConfigManager.getProperty(FluentdConfigNames.LOGGING_FLUENTD_HOST, RuntimeException("Mandatory property not found: ${FluentdConfigNames.LOGGING_FLUENTD_HOST}"))

        /**
         * Port used to connect with Fluentd server.
         */
        private val fluentdPort = ConfigManager.getInt(
            FluentdConfigNames.LOGGING_FLUENTD_PORT,
            FluentdConfigNames.LOGGING_FLUENTD_PORT_DEFAULT
        )

        /**
         * Tag prefix used to send logs to Fluentd server.
         */
        private val fluentdTagPrefix = ConfigManager.getProperty(FluentdConfigNames.LOGGING_FLUENTD_TAG_PREFIX, RuntimeException("Mandatory property not found: ${FluentdConfigNames.LOGGING_FLUENTD_TAG_PREFIX}"))

        /**
         * FluentLogger object used to connect with Fluentd server.
         */
        private val fluentLogger = FluentLogger.getLogger(fluentdTagPrefix, fluentdHost, fluentdPort)

        // endregion
    }

    override fun receive(event: LogEvent?) {
        val values = mutableMapOf(
            "level" to event?.level,
            "loggerName" to event?.logger,
            "threadName" to event?.thread,
            "timestamp" to event?.timestamp,
            "message" to event?.message,
            "hostname" to InetAddress.getLocalHost().hostName,
            "stacktrace" to event?.stacktrace
        )

        fluentLogger.log(fluentdTag, values)
    }

    /**
     * Tag used to send logs to Fluentd server.
     */
    private val fluentdTag = ConfigManager.getProperty(FluentdConfigNames.LOGGING_FLUENTD_TAG, RuntimeException("Mandatory property not found: ${FluentdConfigNames.LOGGING_FLUENTD_TAG}")) + ".$loggerName"
}