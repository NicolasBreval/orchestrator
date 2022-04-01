package org.nitb.orchestrator.logging

import org.slf4j.Logger
import org.slf4j.Marker

class LoggerWrapper(
    private val logger: Logger
): Logger {


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
        return logger.trace(p0)
    }

    override fun trace(p0: String?, p1: Any?) {
        return logger.trace(p0, p1)
    }

    override fun trace(p0: String?, p1: Any?, p2: Any?) {
        return logger.trace(p0, p1, p2)
    }

    override fun trace(p0: String?, vararg p1: Any?) {
        return logger.trace(p0, p1)
    }

    override fun trace(p0: String?, p1: Throwable?) {
        return logger.trace(p0, p1)
    }

    override fun trace(p0: Marker?, p1: String?) {
        return logger.trace(p0, p1)
    }

    override fun trace(p0: Marker?, p1: String?, p2: Any?) {
        return logger.trace(p0, p1, p2)
    }

    override fun trace(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        return logger.trace(p0, p1, p2)
    }

    override fun trace(p0: Marker?, p1: String?, vararg p2: Any?) {
        return logger.trace(p0, p1, p2)
    }

    override fun trace(p0: Marker?, p1: String?, p2: Throwable?) {
        return logger.trace(p0, p1, p2)
    }

    override fun isDebugEnabled(): Boolean {
        return logger.isDebugEnabled
    }

    override fun isDebugEnabled(p0: Marker?): Boolean {
        return logger.isDebugEnabled(p0)
    }

    override fun debug(p0: String?) {
        return logger.debug(p0)
    }

    override fun debug(p0: String?, p1: Any?) {
        return logger.debug(p0, p1)
    }

    override fun debug(p0: String?, p1: Any?, p2: Any?) {
        return logger.debug(p0, p1, p2)
    }

    override fun debug(p0: String?, vararg p1: Any?) {
        return logger.debug(p0, p1)
    }

    override fun debug(p0: String?, p1: Throwable?) {
        return logger.debug(p0, p1)
    }

    override fun debug(p0: Marker?, p1: String?) {
        return logger.debug(p0, p1)
    }

    override fun debug(p0: Marker?, p1: String?, p2: Any?) {
        return logger.debug(p0, p1)
    }

    override fun debug(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        return logger.debug(p0, p1, p2, p3)
    }

    override fun debug(p0: Marker?, p1: String?, vararg p2: Any?) {
        return logger.debug(p0, p1, p2)
    }

    override fun debug(p0: Marker?, p1: String?, p2: Throwable?) {
        return logger.debug(p0, p1, p2)
    }

    override fun isInfoEnabled(): Boolean {
        return logger.isInfoEnabled
    }

    override fun isInfoEnabled(p0: Marker?): Boolean {
        return logger.isInfoEnabled(p0)
    }

    override fun info(p0: String?) {
        return logger.info(p0)
    }

    override fun info(p0: String?, p1: Any?) {
        return logger.info(p0, p1)
    }

    override fun info(p0: String?, p1: Any?, p2: Any?) {
        return logger.info(p0, p1, p2)
    }

    override fun info(p0: String?, vararg p1: Any?) {
        return logger.info(p0, p1)
    }

    override fun info(p0: String?, p1: Throwable?) {
        return logger.info(p0, p1)
    }

    override fun info(p0: Marker?, p1: String?) {
        return logger.info(p0, p1)
    }

    override fun info(p0: Marker?, p1: String?, p2: Any?) {
        return logger.info(p0, p1, p2)
    }

    override fun info(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        return logger.info(p0, p1, p2, p3)
    }

    override fun info(p0: Marker?, p1: String?, vararg p2: Any?) {
        return logger.info(p0, p1, p2)
    }

    override fun info(p0: Marker?, p1: String?, p2: Throwable?) {
        return logger.info(p0, p1, p2)
    }

    override fun isWarnEnabled(): Boolean {
        return logger.isWarnEnabled
    }

    override fun isWarnEnabled(p0: Marker?): Boolean {
        return logger.isWarnEnabled(p0)
    }

    override fun warn(p0: String?) {
        return logger.warn(p0)
    }

    override fun warn(p0: String?, p1: Any?) {
        return logger.warn(p0, p1)
    }

    override fun warn(p0: String?, vararg p1: Any?) {
        return logger.warn(p0, p1)
    }

    override fun warn(p0: String?, p1: Any?, p2: Any?) {
        return logger.warn(p0, p1, p2)
    }

    override fun warn(p0: String?, p1: Throwable?) {
        return logger.warn(p0, p1)
    }

    override fun warn(p0: Marker?, p1: String?) {
        return logger.warn(p0, p1)
    }

    override fun warn(p0: Marker?, p1: String?, p2: Any?) {
        return logger.warn(p0, p1, p2)
    }

    override fun warn(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        return logger.warn(p0, p1, p2, p3)
    }

    override fun warn(p0: Marker?, p1: String?, vararg p2: Any?) {
        return logger.warn(p0, p1, p2)
    }

    override fun warn(p0: Marker?, p1: String?, p2: Throwable?) {
        return logger.warn(p0, p1, p2)
    }

    override fun isErrorEnabled(): Boolean {
        return logger.isErrorEnabled
    }

    override fun isErrorEnabled(p0: Marker?): Boolean {
        return logger.isErrorEnabled(p0)
    }

    override fun error(p0: String?) {
        return logger.error(p0)
    }

    override fun error(p0: String?, p1: Any?) {
        return logger.error(p0, p1)
    }

    override fun error(p0: String?, p1: Any?, p2: Any?) {
        return logger.error(p0, p1, p2)
    }

    override fun error(p0: String?, vararg p1: Any?) {
        return logger.error(p0, p1)
    }

    override fun error(p0: String?, p1: Throwable?) {
        return logger.error(p0, p1)
    }

    override fun error(p0: Marker?, p1: String?) {
        return logger.error(p0, p1)
    }

    override fun error(p0: Marker?, p1: String?, p2: Any?) {
        return logger.error(p0, p1, p2)
    }

    override fun error(p0: Marker?, p1: String?, p2: Any?, p3: Any?) {
        return logger.error(p0, p1, p2, p3)
    }

    override fun error(p0: Marker?, p1: String?, vararg p2: Any?) {
        return logger.error(p0, p1, p2)
    }

    override fun error(p0: Marker?, p1: String?, p2: Throwable?) {
        return logger.error(p0, p1, p2)
    }

}