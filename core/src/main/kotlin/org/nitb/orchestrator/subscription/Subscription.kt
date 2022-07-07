package org.nitb.orchestrator.subscription

import com.fasterxml.jackson.annotation.JsonIgnore
import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.logging.LoggerWrapper
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.binary.BinarySerializer
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscription.entities.DirectMessage
import org.nitb.orchestrator.subscription.entities.MessageHandlerInfo
import java.io.Serializable
import java.lang.Exception
import java.math.BigInteger

@NoArgsConstructor
abstract class Subscription<I, O>(
    val name: String,
    protected val timeout: Long = -1,
    private val description: String? = null
): Serializable {

    // region PUBLIC METHODS

    fun start() {
        status = SubscriptionStatus.IDLE
        starts = starts.add(BigInteger.ONE)
        initialize()
        onStart()
    }

    fun stop() {
        onStop()
        deactivate()
        status = SubscriptionStatus.STOPPED
        stops = stops.add(BigInteger.ONE)
    }

    // endregion

    // region PRIVATE PROPERTIES

    @JsonIgnore
    private val creation: Long = System.currentTimeMillis()

    @JsonIgnore
    private var status: SubscriptionStatus = SubscriptionStatus.STOPPED

    @JsonIgnore
    private var inputVolume: BigInteger = BigInteger.ZERO

    @JsonIgnore
    private var outputVolume: BigInteger = BigInteger.ZERO

    @JsonIgnore
    private var starts: BigInteger = BigInteger.ZERO

    @JsonIgnore
    private var stops: BigInteger = BigInteger.ZERO

    @JsonIgnore
    private var success: BigInteger = BigInteger.ZERO

    @JsonIgnore
    private var error: BigInteger = BigInteger.ZERO

    @JsonIgnore
    private var lastExecution: Long = -1L

    @JsonIgnore
    private var schema: String? = JSONSerializer.getSchema(this::class.java)

    @JsonIgnore
    @Transient
    protected val logger: LoggerWrapper = LoggingManager.getLogger(name)

    @JsonIgnore
    @Transient
    protected val messageHandlers: MutableMap<String, (DirectMessage<*>) -> Any?> = mutableMapOf()

    @JsonIgnore
    @Transient
    protected val messageHandlerInfo: MutableMap<String, MessageHandlerInfo> = mutableMapOf()

    // endregion

    // region PRIVATE METHODS

    protected abstract fun onEvent(sender: String, input: I): O?

    protected open fun preRun(input: I) {}

    protected abstract fun initialize()

    protected abstract fun deactivate()

    protected fun runEvent(messageSize: BigInteger, sender: String, input: I): O? {
        status = SubscriptionStatus.RUNNING
        lastExecution = System.currentTimeMillis()
        inputVolume = inputVolume.add(messageSize)

        return try {
            preRun(input)
            val output = onEvent(sender, input)
            onSuccess(input, output)
            success = success.add(BigInteger.ONE)

            try {
                outputVolume = outputVolume.add(if (output is Unit) BigInteger.ZERO else output?.let { BinarySerializer.serialize(output).size.toBigInteger() } ?: BigInteger.ZERO)
            } catch (e: Exception) {
                logger.error("Fatal error obtaining output volume", e)
            } catch (e: StackOverflowError) {
                logger.error("Fatal error obtaining output volume", e)
            }
            output
        } catch (e: Exception) {
            if (e !is InterruptedException) {
                logger.error("Fatal error during execution", e)
                error = error.add(BigInteger.ONE)
                onError(input)
            } else {
                logger.warn("Interruption detected")
            }
            null
        } finally {
            status = SubscriptionStatus.IDLE
        }
    }

    protected open fun onStart() {}

    protected open fun onStop() {}

    protected open fun onSuccess(input: I, output: O?) {}

    protected open fun onError(input: I) {}

    protected open fun onDelete() {}

    protected fun <T> parseHandlerMessage(message: DirectMessage<*>, clazz: Class<T>): T {
        return JSONSerializer.deserialize(JSONSerializer.serialize(message.info), clazz)
    }

    // endregion

    // region PUBLIC PROPERTIES

    @delegate:Transient
    private val content: String by lazy { JSONSerializer.serializeWithClassName(this) }

    @get:JsonIgnore
    val info: SubscriptionInfo get() = SubscriptionInfo(name, status, creation, inputVolume, outputVolume, starts, stops,
        success, error, lastExecution, schema, content, messageHandlers.keys.associateWith { messageHandlerInfo[it] })

    // endregion

    // region PUBLIC METHODS

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Subscription<*, *>

        if (schema != other.schema) return false

        return true
    }

    override fun hashCode(): Int {
        return schema?.hashCode() ?: 0
    }

    fun handleMessage(message: DirectMessage<*>): Any? {
        return this.messageHandlers[message.handler]?.invoke(message)
    }

    // endregion
}