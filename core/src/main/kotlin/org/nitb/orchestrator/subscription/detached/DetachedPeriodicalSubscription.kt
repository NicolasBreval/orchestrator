package org.nitb.orchestrator.subscription.detached

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.scheduling.Scheduler
import org.nitb.orchestrator.subscription.CyclicalSubscription
import java.math.BigInteger

@HeritableSubscription
abstract class DetachedPeriodicalSubscription(
    name: String,
    private val delay: Long,
    private val initialDelay: Long = 0,
    timeout: Long = -1,
    description: String? = null
): CyclicalSubscription<Unit>(name, timeout, description) {

    override fun createScheduler(): Scheduler {
        return object : PeriodicalScheduler(delay, initialDelay, timeout) {
            override fun onCycle() {
                runEvent(BigInteger.ZERO, name, Unit)
            }
        }
    }
}