package org.nitb.orchestrator.logging.appenders

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.fluentd.logger.FluentLogger
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import java.net.InetAddress

class FluentdAppender(
    loggerName: String
): AppenderBase<ILoggingEvent>() {

    // region STATIC

    companion object {

        // region PRIVATE

        private val fluentdHost = ConfigManager.getProperty(ConfigNames.LOGGING_FLUENTD_HOST, FluentdAppenderPropertyNotFoundException("No mandatory property found: ${ConfigNames.LOGGING_FLUENTD_HOST}"))
        private val fluentdPort = ConfigManager.getInt(ConfigNames.LOGGING_FLUENTD_PORT, ConfigNames.LOGGING_FLUENTD_DEFAULT_PORT)
        private val fluentdTagPrefix = ConfigManager.getProperty(ConfigNames.LOGGING_FLUENTD_TAG_PREFIX, FluentdAppenderPropertyNotFoundException("No mandatory property found: ${ConfigNames.LOGGING_FLUENTD_TAG_PREFIX}"))
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

    private val fluentdTag = ConfigManager.getProperty(ConfigNames.LOGGING_FLUENTD_TAG, FluentdAppenderPropertyNotFoundException("No mandatory property found: ${ConfigNames.LOGGING_FLUENTD_TAG}")) + ".$loggerName"

    // endregion
}