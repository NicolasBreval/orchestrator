package org.nitb.orchestrator.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.appenders.FluentdAppender
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.RuntimeException

object LoggingManager {

    // region PUBLIC METHODS

    fun getLogger(loggerName: String): LoggerWrapper {
        val logger = context.getLogger(loggerName)

        initializeLogger(logger)

        return LoggerWrapper(logger)
    }

    fun getLogger(loggerClass: Class<*>): LoggerWrapper {
        val logger = context.getLogger(loggerClass)

        initializeLogger(logger)

        return LoggerWrapper(logger)
    }

    fun useCommonLoggerLevel(loggerName: String, level: Level = commonLoggerLevel) {
        val logger = context.getLogger(loggerName)
        logger.level = level
    }

    // endregion

    // region PRIVATE PROPERTIES

    private val commonLoggerLevel = levelPropertyToLevel()
    private const val rollingAppenderName = "FILE_ROLLING_APPENDER"
    private const val fluentdAppenderName = "FLUENTD_APPENDER"
    private val context = LoggerFactory.getILoggerFactory() as LoggerContext
    private val loggingFolder = ConfigManager.getProperty(ConfigNames.LOGGING_FOLDER, ConfigNames.LOGGING_FOLDER_DEFAULT)
    private val loggingPattern = ConfigManager.getProperty(ConfigNames.LOGGING_PATTERN, ConfigNames.LOGGING_PATTERN_DEFAULT)
    private val loggingDatePattern = ConfigManager.getProperty(ConfigNames.LOGGING_DATE_PATTERN, ConfigNames.LOGGING_DATE_PATTERN_DEFAULT)
    private val loggingMaxSize = ConfigManager.getProperty(ConfigNames.LOGGING_MAX_FILE_SIZE, ConfigNames.LOGGING_MAX_FILE_SIZE_DEFAULT)
    private val fluentdEnabled = ConfigManager.getBoolean(ConfigNames.LOGGING_FLUENTD_ENABLED)

    // endregion

    // region PRIVATE METHODS

    private fun levelPropertyToLevel(): Level {
        val strLevel = ConfigManager.getProperty(ConfigNames.LOGGING_LEVEL, ConfigNames.LOGGING_LEVEL_DEFAULT)

        return when (strLevel) {
            "OFF" -> Level.OFF
            "ERROR" -> Level.ERROR
            "WARN" -> Level.WARN
            "INFO" -> Level.INFO
            "DEBUG" -> Level.DEBUG
            "TRACE" -> Level.TRACE
            "ALL" -> Level.ALL
            else -> throw RuntimeException("Invalid level name: $strLevel")
        }
    }

    private fun initializeLogger(logger: Logger) {
        if (logger.getAppender(rollingAppenderName) == null) {
            logger.detachAndStopAllAppenders()

            val rollingFileAppender = RollingFileAppender<ILoggingEvent>()
            rollingFileAppender.name = rollingAppenderName
            rollingFileAppender.context = context
            rollingFileAppender.file = File(loggingFolder, "${logger.name}.log").absolutePath

            val timeBasedRollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>()
            timeBasedRollingPolicy.context = context
            timeBasedRollingPolicy.maxHistory = 5
            timeBasedRollingPolicy.fileNamePattern = "-%d{$loggingDatePattern}.log"
            timeBasedRollingPolicy.setParent(rollingFileAppender)
            timeBasedRollingPolicy.start()

            val sizeAndTimeBasedFNATP = SizeAndTimeBasedFNATP<ILoggingEvent>()
            sizeAndTimeBasedFNATP.context = context
            sizeAndTimeBasedFNATP.setMaxFileSize(FileSize.valueOf(loggingMaxSize))
            sizeAndTimeBasedFNATP.setTimeBasedRollingPolicy(timeBasedRollingPolicy)
            sizeAndTimeBasedFNATP.start()

            rollingFileAppender.rollingPolicy = timeBasedRollingPolicy

            val patternLayoutEncoder = PatternLayoutEncoder()
            patternLayoutEncoder.context = context
            patternLayoutEncoder.pattern = loggingPattern
            patternLayoutEncoder.start()

            rollingFileAppender.encoder = patternLayoutEncoder
            rollingFileAppender.start()

            logger.addAppender(rollingFileAppender)
        }

        if (fluentdEnabled && logger.getAppender(fluentdAppenderName) == null) {
            val fluentdAppender = FluentdAppender(logger.name)
            fluentdAppender.start()

            logger.addAppender(fluentdAppender)
        }

        logger.level = commonLoggerLevel
    }

    // endregion

    // region INIT

    init {
        val loggingFolderFile = File(loggingFolder)

        if (!loggingFolderFile.exists()) {
            loggingFolderFile.mkdir()
        }
    }

    // endregion
}