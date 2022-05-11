package org.nitb.orchestrator.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import org.nitb.orchestrator.annotations.NoArgsConstructor

@NoArgsConstructor
class LogEvent(
    val level: String,
    val logger: String,
    val thread: String,
    val timestamp: Long,
    val message: String,
    val stacktrace: List<LogEventStackTrace>
) {
    companion object {
        internal fun fromOriginal(event: ILoggingEvent?): LogEvent? {
            return event?.let { e -> LogEvent(e.level.levelStr, e.loggerName, e.threadName, e.timeStamp, e.message, e.callerData.map { LogEventStackTrace.fromOriginal(it) }) }
        }
    }
}