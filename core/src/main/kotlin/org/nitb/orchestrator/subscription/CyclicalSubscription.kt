package org.nitb.orchestrator.subscription

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import com.fasterxml.jackson.annotation.JsonIgnore
import org.nitb.orchestrator.scheduling.CronScheduler
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.scheduling.Scheduler
import org.nitb.orchestrator.subscription.entities.PeriodType
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.math.BigInteger

abstract class CyclicalSubscription<O>(
    name: String,
    timeout: Long = -1,
    description: String? = null,
    private val periodExpression: String,
    private val type: PeriodType
): Subscription<Unit, O>(name, timeout, description) {

    @JsonIgnore
    @Transient
    private val fixedRegex = "\\d+(@)?(\\d+)?"

    @delegate:JsonIgnore
    @delegate:Transient
    private val scheduler: Scheduler by lazy {
        if (type == PeriodType.FIXED) {
            if (!fixedRegex.toRegex().matches(periodExpression)) {
                throw RuntimeException("Invalid expression for $type.")
            }

            val (delay, initialDelay) = if (periodExpression.contains("@")) {
                val parts = periodExpression.split("@")
                Pair(parts[0].toLong(), parts[1].toLong())
            } else {
                Pair(periodExpression.toLong(), 0L)
            }

            object : PeriodicalScheduler(delay, initialDelay, timeout) {
                override fun onCycle() {
                    runEvent(BigInteger.ZERO, name, Unit)
                }
            }
        } else {

            val cron = try {
                CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.valueOf(type.name)))
                    .parse(periodExpression).validate()
            } catch (e: IllegalArgumentException) {
                throw RuntimeException("Invalid expression for $type.")
            }

            object : CronScheduler(cron, timeout) {
                override fun onCycle() {
                    runEvent(BigInteger.ZERO, name, Unit)
                }
            }
        }
    }

    override fun initialize() {
        scheduler.start()
    }

    override fun deactivate() {
        scheduler.pause(true)
    }

    override fun onDelete() {
        scheduler.stop(true)
    }
}