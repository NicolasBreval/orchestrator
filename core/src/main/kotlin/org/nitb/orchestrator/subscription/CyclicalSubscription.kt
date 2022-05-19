package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.scheduling.Scheduler

abstract class CyclicalSubscription<O>(
    name: String,
    timeout: Long = -1,
    description: String? = null
): Subscription<Unit, O>(name, timeout, description) {

    // region PRIVATE PROPERTIES

    @delegate:Transient
    private val scheduler: Scheduler by lazy { createScheduler() }

    // endregion

    // region PRIVATE METHODS

    override fun initialize() {
        scheduler.start()
    }

    override fun deactivate() {
        scheduler.pause(true)
    }

    override fun onDelete() {
        scheduler.stop(true)
    }

    abstract fun createScheduler(): Scheduler

    // endregion

}