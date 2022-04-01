package org.nitb.orchestrator.scheduling

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

abstract class CronScheduler(
    private val cronExpression: String,
    timeout: Long = -1
): Scheduler(timeout) {

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

    private val cron by lazy { CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(cronExpression) }

    init {
        cron.validate()
    }
}