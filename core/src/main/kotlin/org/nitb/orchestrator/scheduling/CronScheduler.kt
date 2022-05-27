package org.nitb.orchestrator.scheduling

import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import java.lang.Exception
import java.lang.IllegalStateException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Scheduler object used to run a task periodically, based on cron expression.
 *
 * @property cronExpression String with cron expression to define period of task execution.
 * @property cronType Cron type for cron expression.
 * @param timeout Maximum time that scheduler can execute task.
 */
abstract class CronScheduler(
    private val cron: Cron,
    timeout: Long = -1,
    name: String? = null,
    vararg params: Any = arrayOf()
): Scheduler(timeout, name, params) {

    override fun createExecutor(): ScheduledExecutorService {
        return Executors.newSingleThreadScheduledExecutor()
    }

    override fun initializeTask(): ScheduledFuture<*> {

        val optionalNextExecutor = ExecutionTime.forCron(cron).nextExecution(ZonedDateTime.now())

        if (!optionalNextExecutor.isPresent)
            throw IllegalStateException("Cannot calculate next date with cron expression")

        val delay = ChronoUnit.MILLIS.between(ZonedDateTime.now(), optionalNextExecutor.get())

        return executor.schedule({
            try {
                onCycle()
            } catch (e: Exception) {
                if (e !is InterruptedException)
                    logger.error("Fatal error executing task", e)
            } finally {
                executorTask = initializeTask()
            }
        }, delay, TimeUnit.MILLISECONDS)
    }
}