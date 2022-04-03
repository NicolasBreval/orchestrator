package org.nitb.orchestrator.scheduling

import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

abstract class PeriodicalScheduler(
    private val delay: Long,
    private val initialDelay: Long = 0,
    timeout: Long = -1
): Scheduler(timeout) {

    override fun createExecutor(): ScheduledExecutorService {
        return Executors.newSingleThreadScheduledExecutor()
    }

    override fun initializeTask(): ScheduledFuture<*> {
        return executor.scheduleWithFixedDelay({
            try {
                onCycle()
            } catch (e: Exception) {
                if (e !is InterruptedException)
                    logger.error("Fatal error executing task", e)
            }
        }, initialDelay, delay, TimeUnit.MILLISECONDS)
    }
}