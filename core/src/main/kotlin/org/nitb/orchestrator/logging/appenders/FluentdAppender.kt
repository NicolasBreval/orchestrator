package org.nitb.orchestrator.logging.appenders

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.fluentd.logger.FluentLogger
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import java.lang.RuntimeException
import java.net.InetAddress

/**
 * Appender object to send automatically logs to fluentd server.
 * @param loggerName Name of logger who sends log.
 */
class FluentdAppender(
    loggerName: String
): AppenderBase<ILoggingEvent>() {

    // region STATIC

    companion object {

        // region PRIVATE

        /**
         * Hostname used to connect with Fluentd server.
         */
        private val fluentdHost = ConfigManager.getProperty(ConfigNames.LOGGING_FLUENTD_HOST, RuntimeException("Mandatory property not found: ${ConfigNames.LOGGING_FLUENTD_HOST}"))

        /**
         * Port used to connect with Fluentd server.
         */
        private val fluentdPort = ConfigManager.getInt(ConfigNames.LOGGING_FLUENTD_PORT, ConfigNames.LOGGING_FLUENTD_DEFAULT_PORT)

        /**
         * Tag prefix used to send logs to Fluentd server.
         */
        private val fluentdTagPrefix = ConfigManager.getProperty(ConfigNames.LOGGING_FLUENTD_TAG_PREFIX, RuntimeException("Mandatory property not found: ${ConfigNames.LOGGING_FLUENTD_TAG_PREFIX}"))

        /**
         * FluentLogger object used to connect with Fluentd server.
         */
        private val fluentLogger = FluentLogger.getLogger(fluentdTagPrefix, fluentdHost, fluentdPort)

        // endregion
    }

    // endregion

    // region PUBLIC METHODS

    override fun append(event: ILoggingEvent?) {
        val values = mutableMapOf<String, Any?>(
            "level" to event?.level,
            "loggerName" to event?.loggerName,
            "threadName" to event?.threadName,
            "timestamp" to event?.timeStamp,
            "message" to event?.message,
            "hostname" to InetAddress.getLocalHost().hostName,
            "stacktrace" to event?.callerData
        )

        fluentLogger.log(fluentdTag, values)
    }

    // endregion

    // region PRIVATE

    /**
     * Tag used to send logs to Fluentd server.
     */
    private val fluentdTag = ConfigManager.getProperty(ConfigNames.LOGGING_FLUENTD_TAG, RuntimeException("Mandatory property not found: ${ConfigNames.LOGGING_FLUENTD_TAG}")) + ".$loggerName"

    // endregion
}