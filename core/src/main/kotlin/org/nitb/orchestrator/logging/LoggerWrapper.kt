package org.nitb.orchestrator.logging

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.Marker

/**
 * Custom logger object based in SLF4J logger.
 * @param logger Original logger on which it is based.
 * @param level Level related to logger.
 */
class LoggerWrapper(
    private val logger: Logger,
    val level: Level
): Logger {

    // region PUBLIC METHODS

    override fun getName(): String {
        return logger.name
    }

    override fun isTraceEnabled(): Boolean {
        return logger.isTraceEnabled
    }

    override fun isTraceEnabled(p0: Marker?): Boolean {
        return logger.isTraceEnabled(p0)
    }

    override fun trace(p0: String?) {
        logger.trace(p0)
    }

    override fun trace(p0: String?, p1: Any?) {
        logger.trace(p0, p1)
    }

    override fun trace(p0: String?, p1: Any?, p2: Any?) {
        logger.trace(p0, p1, p2)
    }

    override fun trace(p0: String?, vararg p1: Any?) {
        logger.trace(p0, p1)
    }

    override fun trace(p0: String?, p1: Throwable?) {
        logger.trace(p0, p1)
    }

    override fun trace(p0: Marker?, p1: String?) {
        logger.trace(p0, p1)
    }

    override fun trace(p0: Marker?, p1: String?, p2: Any?) {
        logger.trace(p0, p1, p2)
    }

    override fun trace(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        logger.trace(p0, p1, p2)
    }

    override fun trace(p0: Marker?, p1: String?, vararg p2: Any?) {
        logger.trace(p0, p1, p2)
    }

    override fun trace(p0: Marker?, p1: String?, p2: Throwable?) {
        logger.trace(p0, p1, p2)
    }

    override fun isDebugEnabled(): Boolean {
        return logger.isDebugEnabled
    }

    override fun isDebugEnabled(p0: Marker?): Boolean {
        return logger.isDebugEnabled(p0)
    }

    override fun debug(p0: String?) {
        logger.debug(p0)
    }

    override fun debug(p0: String?, p1: Any?) {
        logger.debug(p0, p1)
    }

    override fun debug(p0: String?, p1: Any?, p2: Any?) {
        logger.debug(p0, p1, p2)
    }

    override fun debug(p0: String?, vararg p1: Any?) {
        logger.debug(p0, p1)
    }

    override fun debug(p0: String?, p1: Throwable?) {
        logger.debug(p0, p1)
    }

    override fun debug(p0: Marker?, p1: String?) {
        logger.debug(p0, p1)
    }

    override fun debug(p0: Marker?, p1: String?, p2: Any?) {
        logger.debug(p0, p1)
    }

    override fun debug(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        logger.debug(p0, p1, p2, p3)
    }

    override fun debug(p0: Marker?, p1: String?, vararg p2: Any?) {
        logger.debug(p0, p1, p2)
    }

    override fun debug(p0: Marker?, p1: String?, p2: Throwable?) {
        logger.debug(p0, p1, p2)
    }

    override fun isInfoEnabled(): Boolean {
        return logger.isInfoEnabled
    }

    override fun isInfoEnabled(p0: Marker?): Boolean {
        return logger.isInfoEnabled(p0)
    }

    override fun info(p0: String?) {
        logger.info(p0)
    }

    override fun info(p0: String?, p1: Any?) {
        logger.info(p0, p1)
    }

    override fun info(p0: String?, p1: Any?, p2: Any?) {
        logger.info(p0, p1, p2)
    }

    override fun info(p0: String?, vararg p1: Any?) {
        logger.info(p0, p1)
    }

    override fun info(p0: String?, p1: Throwable?) {
        logger.info(p0, p1)
    }

    override fun info(p0: Marker?, p1: String?) {
        logger.info(p0, p1)
    }

    override fun info(p0: Marker?, p1: String?, p2: Any?) {
        logger.info(p0, p1, p2)
    }

    override fun info(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        logger.info(p0, p1, p2, p3)
    }

    override fun info(p0: Marker?, p1: String?, vararg p2: Any?) {
        logger.info(p0, p1, p2)
    }

    override fun info(p0: Marker?, p1: String?, p2: Throwable?) {
        logger.info(p0, p1, p2)
    }

    override fun isWarnEnabled(): Boolean {
        return logger.isWarnEnabled
    }

    override fun isWarnEnabled(p0: Marker?): Boolean {
        return logger.isWarnEnabled(p0)
    }

    override fun warn(p0: String?) {
        logger.warn(p0)
    }

    override fun warn(p0: String?, p1: Any?) {
        logger.warn(p0, p1)
    }

    override fun warn(p0: String?, vararg p1: Any?) {
        logger.warn(p0, p1)
    }

    override fun warn(p0: String?, p1: Any?, p2: Any?) {
        logger.warn(p0, p1, p2)
    }

    override fun warn(p0: String?, p1: Throwable?) {
        logger.warn(p0, p1)
    }

    override fun warn(p0: Marker?, p1: String?) {
        logger.warn(p0, p1)
    }

    override fun warn(p0: Marker?, p1: String?, p2: Any?) {
        logger.warn(p0, p1, p2)
    }

    override fun warn(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        logger.warn(p0, p1, p2, p3)
    }

    override fun warn(p0: Marker?, p1: String?, vararg p2: Any?) {
        logger.warn(p0, p1, p2)
    }

    override fun warn(p0: Marker?, p1: String?, p2: Throwable?) {
        logger.warn(p0, p1, p2)
    }

    override fun isErrorEnabled(): Boolean {
        return logger.isErrorEnabled
    }

    override fun isErrorEnabled(p0: Marker?): Boolean {
        return logger.isErrorEnabled(p0)
    }

    override fun error(p0: String?) {
        logger.error(p0)
    }

    override fun error(p0: String?, p1: Any?) {
        logger.error(p0, p1)
    }

    override fun error(p0: String?, p1: Any?, p2: Any?) {
        logger.error(p0, p1, p2)
    }

    override fun error(p0: String?, vararg p1: Any?) {
        return logger.error(p0, p1)
    }

    override fun error(p0: String?, p1: Throwable?) {
        logger.error(p0, p1)
    }

    override fun error(p0: Marker?, p1: String?) {
        logger.error(p0, p1)
    }

    override fun error(p0: Marker?, p1: String?, p2: Any?) {
        logger.error(p0, p1, p2)
    }

    override fun error(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        logger.error(p0, p1, p2, p3)
    }

    override fun error(p0: Marker?, p1: String?, vararg p2: Any?) {
        logger.error(p0, p1, p2)
    }

    override fun error(p0: Marker?, p1: String?, p2: Throwable?) {
        logger.error(p0, p1, p2)
    }

    fun log(level: org.slf4j.event.Level, p0: String?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0)
            org.slf4j.event.Level.WARN -> logger.warn(p0)
            org.slf4j.event.Level.INFO -> logger.info(p0)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0)
            org.slf4j.event.Level.TRACE -> logger.trace(p0)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: String?, p1: Any?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: String?, p1: Any?, p2: Any?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1, p2)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1, p2)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1, p2)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1, p2)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1, p2)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: String?, vararg p1: Any?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: String?, p1: Throwable?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: Marker?, p1: String?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: Marker?, p1: String?, p2: Any?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1, p2)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1, p2)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1, p2)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1, p2)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1, p2)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1, p2, p3)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1, p2, p3)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1, p2, p3)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1, p2, p3)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1, p2, p3)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: Marker?, p1: String?, vararg p2: Any?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1, p2)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1, p2)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1, p2)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1, p2)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1, p2)
        }
    }

    fun log(level: org.slf4j.event.Level, p0: Marker?, p1: String?, p2: Throwable?) {
        when(level) {
            org.slf4j.event.Level.ERROR -> logger.error(p0, p1, p2)
            org.slf4j.event.Level.WARN -> logger.warn(p0, p1, p2)
            org.slf4j.event.Level.INFO -> logger.info(p0, p1, p2)
            org.slf4j.event.Level.DEBUG -> logger.debug(p0, p1, p2)
            org.slf4j.event.Level.TRACE -> logger.trace(p0, p1, p2)
        }
    }



    // endregion

}