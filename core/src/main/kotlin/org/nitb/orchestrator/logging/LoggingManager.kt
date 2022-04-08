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

/**
 * Util class used to create logger objects.
 */
object LoggingManager {

    // region PUBLIC METHODS

    /**
     * Creates a new logger object from a name.
     * @param loggerName Name of logger to create.
     * @return New logger object.
     */
    fun getLogger(loggerName: String): LoggerWrapper {
        val logger = context.getLogger(loggerName)

        initializeLogger(logger)

        return LoggerWrapper(logger, logger.level)
    }

    /**
     * Creates a new logger object from a class.
     * @param loggerClass Class used as name of logger to create.
     * @return New logger object.
     */
    fun getLogger(loggerClass: Class<*>): LoggerWrapper {
        val logger = context.getLogger(loggerClass)

        initializeLogger(logger)

        return LoggerWrapper(logger, logger.level)
    }

    /**
     * Sets level of a logger with specified name.
     * @param loggerName Name of logger to set level.
     * @param level Level to set in logger.
     */
    fun setLoggerLevel(loggerName: String, level: Level = commonLoggerLevel) {
        val logger = context.getLogger(loggerName)
        logger.level = level
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Logger level obtained from properties file.
     */
    private val commonLoggerLevel = levelPropertyToLevel()

    /**
     * Name of rolling file appender
     */
    private const val rollingAppenderName = "FILE_ROLLING_APPENDER"

    /**
     * Name of fluentd appender
     */
    private const val fluentdAppenderName = "FLUENTD_APPENDER"

    /**
     * Context to create logger objects.
     */
    private val context = LoggerFactory.getILoggerFactory() as LoggerContext

    /**
     * Folder used to store log files.
     */
    private val loggingFolder = ConfigManager.getProperty(ConfigNames.LOGGING_FOLDER, ConfigNames.LOGGING_FOLDER_DEFAULT)

    /**
     * Pattern used to write logs.
     */
    private val loggingPattern = ConfigManager.getProperty(ConfigNames.LOGGING_PATTERN, ConfigNames.LOGGING_PATTERN_DEFAULT)

    /**
     * Date pattern used to name logger files with rolling date appender.
     */
    private val loggingDatePattern = ConfigManager.getProperty(ConfigNames.LOGGING_DATE_PATTERN, ConfigNames.LOGGING_DATE_PATTERN_DEFAULT)

    /**
     * Max allowed size of a logging file.
     */
    private val loggingMaxSize = ConfigManager.getProperty(ConfigNames.LOGGING_MAX_FILE_SIZE, ConfigNames.LOGGING_MAX_FILE_SIZE_DEFAULT)

    /**
     * If is true, creates Fluentd appender to logs.
     */
    private val fluentdEnabled = ConfigManager.getBoolean(ConfigNames.LOGGING_FLUENTD_ENABLED)

    // endregion

    // region PRIVATE METHODS

    /**
     * Transforms string level from property to a valid Level object. If string property is not a valid level name, throws
     * RuntimeException.
     * @return [Level] parsed from string property.
     */
    private fun levelPropertyToLevel(): Level {
        return when (val strLevel = ConfigManager.getProperty(ConfigNames.LOGGING_LEVEL, ConfigNames.LOGGING_LEVEL_DEFAULT)) {
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

    /**
     * Initializes a new [Logger] object, if it was not created before.
     * @param logger Logger object to initialize
     */
    private fun initializeLogger(logger: Logger) {
        if (logger.getAppender(rollingAppenderName) == null) {
            logger.detachAndStopAllAppenders()

            val rollingFileAppender = RollingFileAppender<ILoggingEvent>()
            rollingFileAppender.name = rollingAppenderName
            rollingFileAppender.context = context

            val timeBasedRollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>()
            timeBasedRollingPolicy.context = context
            timeBasedRollingPolicy.maxHistory = 5
            timeBasedRollingPolicy.fileNamePattern = File(loggingFolder, "${logger.name}-%d{$loggingDatePattern}.log").absolutePath
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