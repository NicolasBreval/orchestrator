package org.nitb.orchestrator.scheduling

import org.nitb.orchestrator.logging.LoggingManager
import java.util.concurrent.*

abstract class Scheduler(
    private val timeout: Long = -1
) {
    val running: Boolean get() = !executorTask.isDone && !executorTask.isCancelled

    fun start() {
        executorTask = initializeTask()

        if (timeout > 0) {
            timeoutTask = timeoutExecutor.scheduleAtFixedRate({
                if (running) {
                    executorTask.cancel(true)
                    executorTask = initializeTask()
                }
            }, timeout, timeout, TimeUnit.MILLISECONDS)
        }
    }

    fun pause(wait: Boolean = false) {
        if (timeout > 0) timeoutTask.cancel(true)
        executorTask.cancel(true)

        if (wait) {
            while (running) {
                Thread.sleep(100)
            }
        }
    }

    fun stop(wait: Boolean = false) {
        pause(wait)
        executor.shutdownNow()
        timeoutExecutor.shutdownNow()
    }

    protected abstract fun createExecutor(): ScheduledExecutorService

    protected abstract fun onCycle()

    protected abstract fun initializeTask(): ScheduledFuture<*>

    protected val logger = LoggingManager.getLogger(this::class.java)
    protected val executor by lazy { createExecutor() }
    private val timeoutExecutor = Executors.newSingleThreadScheduledExecutor()
    protected lateinit var executorTask: ScheduledFuture<*>
    private lateinit var timeoutTask: ScheduledFuture<*>

    init {
        Runtime.getRuntime().addShutdownHook(Thread{ executor.shutdownNow(); timeoutExecutor.shutdownNow(); })
    }
}