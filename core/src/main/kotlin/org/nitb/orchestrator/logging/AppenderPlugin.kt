package org.nitb.orchestrator.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.nitb.orchestrator.annotations.NoArgsConstructor

@NoArgsConstructor
abstract class AppenderPlugin(
    protected val loggerName: String
): AppenderBase<ILoggingEvent>() {

    abstract fun receive(event: LogEvent?)

    override fun append(event: ILoggingEvent?) {
        receive(LogEvent.fromOriginal(event))
    }

}