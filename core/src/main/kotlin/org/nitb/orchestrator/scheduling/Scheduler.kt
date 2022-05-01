package org.nitb.orchestrator.scheduling

import org.nitb.orchestrator.logging.LoggingManager
import java.util.concurrent.*

/**
 * Class used to run code periodically.
 *
 * @param timeout Maximum time the scheduler can be running its task. If this property is less than zero, task hasn't timeout.
 */
abstract class Scheduler(
    private val timeout: Long = -1,
    name: String? = null,
    protected val params: Array<out Any> = arrayOf()
) {

    // region PUBLIC PROPERTIES

    val running: Boolean get() = !executorTask.isDone && !executorTask.isCancelled

    // endregion

    // region PUBLIC METHODS

    /**
     * Starts running the task periodically
     */
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

    /**
     * Pauses task execution. Scheduler can be started again after call this method.
     */
    fun pause(wait: Boolean = false) {
        if (timeout > 0) timeoutTask.cancel(true)
        executorTask.cancel(true)

        if (wait) {
            while (running) {
                Thread.sleep(100)
            }
        }
    }

    /**
     * Stops the executor forever.
     */
    fun stop(wait: Boolean = false) {
        pause(wait)
        executor.shutdownNow()
        timeoutExecutor.shutdownNow()
    }

    /**
     * Creates executor to run task periodically
     */
    protected abstract fun createExecutor(): ScheduledExecutorService

    /**
     * Method to define task to execute.
     */
    protected abstract fun onCycle()

    /**
     * Method to initialize task related to periodically execution.
     */
    protected abstract fun initializeTask(): ScheduledFuture<*>

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Logger object to show logs to developer.
     */
    protected val logger = if (name == null) LoggingManager.getLogger(this::class.java) else LoggingManager.getLogger(name)

    /**
     * [ExecutorService] object used to run task periodically.
     */
    protected val executor by lazy { createExecutor() }

    /**
     * [ExecutorService] used to check if main executor exceeds timeout.
     */
    private val timeoutExecutor = Executors.newSingleThreadScheduledExecutor()

    /**
     * Task object related to [executor].
     */
    protected lateinit var executorTask: ScheduledFuture<*>

    /**
     * Task object related to [timeoutExecutor].
     */
    private lateinit var timeoutTask: ScheduledFuture<*>

    // endregion

    // region INIT

    init {
        Runtime.getRuntime().addShutdownHook(Thread{ executor.shutdownNow(); timeoutExecutor.shutdownNow(); })
    }

    // endregion
}