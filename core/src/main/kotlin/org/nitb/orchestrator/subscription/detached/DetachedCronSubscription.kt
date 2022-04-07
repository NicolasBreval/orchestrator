package org.nitb.orchestrator.subscription.detached

import com.cronutils.model.CronType
import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.scheduling.CronScheduler
import org.nitb.orchestrator.scheduling.Scheduler
import org.nitb.orchestrator.subscription.CyclicalSubscription
import java.math.BigInteger

@HeritableSubscription
abstract class DetachedCronSubscription(
    name: String,
    private val cronExpression: String,
    private val cronType: CronType = CronType.UNIX,
    timeout: Long = -1,
    description: String? = null
): CyclicalSubscription<Unit>(name, timeout, description) {

    override fun createScheduler(): Scheduler {
        return object : CronScheduler(cronExpression, cronType, timeout) {
            override fun onCycle() {
                runEvent(BigInteger.ZERO, name, Unit)
            }
        }
    }
}