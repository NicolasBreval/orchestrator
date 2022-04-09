package org.nitb.orchestrator.scheduling

import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Scheduler object used to run a task periodically, based on period time, in milliseconds.
 *
 * @property delay Time, in milliseconds, to wait between two executions of task.
 * @property initialDelay Time, in milliseconds, to wait before first execution of task.
 * @param timeout Maximum time that scheduler can execute task.
 */
abstract class PeriodicalScheduler(
    private val delay: Long,
    private val initialDelay: Long = 0,
    timeout: Long = -1,
    name: String? = null
): Scheduler(timeout, name) {

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