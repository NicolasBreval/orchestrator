package org.nitb.orchestrator.test.scheduling

import com.cronutils.model.CronType
import org.junit.jupiter.api.Test
import org.nitb.orchestrator.scheduling.CronScheduler
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.junit.jupiter.api.Assertions.*

class SchedulingTests {

    class SimplePeriodicalScheduler(
        delay: Long,
        initialDelay: Long = 0,
        timeout: Long = -1
    ): PeriodicalScheduler(delay, initialDelay, timeout) {
        var count: Int = 0

        override fun onCycle() {
            count++
        }
    }

    class OverTimePeriodicalScheduler(
        delay: Long,
        initialDelay: Long = 0,
        timeout: Long = -1
    ): PeriodicalScheduler(delay, initialDelay, timeout) {

        var count: Int = 0

        override fun onCycle() {
            count++
            Thread.sleep(2000)
        }
    }

    class OverTimeCronScheduler(
        cronExpression: String,
        cronType: CronType = CronType.UNIX,
        timeout: Long = -1
    ): CronScheduler(cronExpression, cronType, timeout) {
        var count: Int = 0

        override fun onCycle() {
            count++
            Thread.sleep(5000)
        }
    }

    class SimpleCronScheduler(
        cronExpression: String,
        cronType: CronType = CronType.UNIX,
        timeout: Long = -1
    ): CronScheduler(cronExpression, cronType, timeout) {
        var count: Int = 0

        override fun onCycle() {
            count++
            Thread.sleep(5000)
        }
    }

    @Test
    fun periodicalTest() {
        val simplePeriodicalScheduler = SimplePeriodicalScheduler(100, 0, -1)
        simplePeriodicalScheduler.start()

        Thread.sleep(2000)

        simplePeriodicalScheduler.stop()

        assertEquals(simplePeriodicalScheduler.count, 19)
    }

    @Test
    fun periodicalOverTimedTest() {
        val overTimePeriodicalScheduler = OverTimePeriodicalScheduler(100, 0, 100)
        overTimePeriodicalScheduler.start()

        Thread.sleep(200)

        overTimePeriodicalScheduler.stop()

        assertEquals(overTimePeriodicalScheduler.count, 2)

    }

    @Test
    fun cronTest() {
        val simpleCronScheduler = SimpleCronScheduler("0/2 * * * * ?", CronType.QUARTZ, 1000)
        simpleCronScheduler.start()

        Thread.sleep(3000)

        simpleCronScheduler.stop()

        assertEquals(simpleCronScheduler.count, 1)
    }

    @Test
    fun cronOverTimedTest() {
        val overTimeCronScheduler = OverTimeCronScheduler("* * * * * ?", CronType.QUARTZ, 1000)
        overTimeCronScheduler.start()

        Thread.sleep(2500)

        overTimeCronScheduler.stop()

        assertEquals(overTimeCronScheduler.count, 2)
    }

}