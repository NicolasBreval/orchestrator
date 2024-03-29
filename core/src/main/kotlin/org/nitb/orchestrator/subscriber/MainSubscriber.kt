package org.nitb.orchestrator.subscriber

import com.fasterxml.jackson.core.type.TypeReference
import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.DbController
import org.nitb.orchestrator.database.relational.entities.SubscriptionSerializableEntry
import org.nitb.orchestrator.http.HttpClient
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.RequestType
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionOperationResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionOperationResult
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.SubscriptionStatus
import org.nitb.orchestrator.subscription.entities.DirectMessage
import org.nitb.orchestrator.web.entities.UploadSubscriptionsRequest
import org.reflections.Reflections
import java.io.InputStream
import java.io.Serializable
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.asSequence

class MainSubscriber(
    private val parentSubscriber: Subscriber,
    private val subscriberName: String
): AmqpManager<Serializable>, AmqpConsumer<Serializable>, AmqpSender {

    // region PUBLIC PROPERTIES

    var isStarted = false

    // endregion

    // region PUBLIC METHODS

    fun start() {
        client.start()
        client.purge()

        registerConsumer(client) { message ->
            when (message.message) {
                is SubscriberInfo -> {
                    subscribers[message.sender] = message.message
                    lastSubscriberInfoReceptionTime = System.currentTimeMillis()
                    logger.debug("Received message from ${message.sender} with ${message.message.subscriptions.count()} subscriptions")
                }
                else -> logger.error("Unrecognized message type has been received. Type: ${message::class.java.name}")
            }
        }

        logger.info("Obtaining subscriptions from database...")
        val lastSubscriptions = DbController.getLastSubscriptions()

        logger.info("${lastSubscriptions.size} subscriptions obtained from database")
        if (allocationStrategy == AllocationStrategy.FIXED) {
            lastSubscriptions.groupBy { it.subscriber }.forEach { (subscriber, subscriptions) ->
                fallenSubscriptionsBySubscriber[subscriber] = subscriptions.associate { Pair(it.name, Pair(String(it.content.bytes), it.stopped)) }
                fallenSubscriptionsNeedToSend[subscriber] = true
            }
        } else {
            fallenSubscriptionsBySubscriber[""] = lastSubscriptions.associate { Pair(it.name, Pair(String(it.content.bytes), it.stopped)) }
            fallenSubscriptionsNeedToSend[""] = true
        }

        checkMasterRoleScheduler.start()
        checkNodesScheduler.start()
        checkWaitingSubscriptionsToUploadScheduler.start()

        sendInfoToDisplayNodeScheduler.start()

        isStarted = true
    }

    fun stop() {
        if (checkMasterRoleSchedulerDelegate.isInitialized()) {
            checkMasterRoleScheduler.stop()
        }

        if (checkNodesSchedulerDelegate.isInitialized()) {
            checkNodesScheduler.stop()
        }

        if (checkWaitingSubscriptionsToUploadSchedulerDelegate.isInitialized()) {
            checkWaitingSubscriptionsToUploadScheduler.stop()
        }

        if (sendInfoToDisplayNodeSchedulerDelegate.isInitialized()) {
            sendInfoToDisplayNodeScheduler.stop()
        }

        client.close()

        isStarted = false
    }

    fun uploadSubscriptions(subscriptions: List<String>, subscriber: String? = null, stopped: Map<String, Boolean> = mapOf()): SubscriptionOperationResponse {
        if (allocationStrategy == AllocationStrategy.FIXED && (subscriber == null || !subscribers.containsKey(subscriber))) {
            return SubscriptionOperationResponse(RequestType.UPLOAD, "Unable to upload subscriptions for a FIXED strategy without a valid subscriber name", notModified = subscriptions)
        }

        if (allocationStrategy != AllocationStrategy.FIXED && subscribers.isEmpty()) {
            return SubscriptionOperationResponse(RequestType.UPLOAD, "There are no subscribers available at this moment", notModified = subscriptions)
        }

        val uploaded = mutableListOf<String>()
        val notUploaded = mutableListOf<String>()

        if (allocationStrategy == AllocationStrategy.FIXED) {
            mapOf(subscriber!! to subscriptions)
        } else {
            val ranking = makeRanking()
            subscriptions.withIndex().groupBy({ (i, _) -> ranking[i % ranking.size] }, { (_, subscription) -> subscription })
        }.entries.parallelStream().forEach { (subscriber, subscriptions) ->
            if (subscriber == subscriberName) {
                parentSubscriber.uploadSubscriptions(subscriptions, subscriber, true, stopped)
            } else {
                val info = subscribers[subscriber]
                val url = "http://${info?.fixedHost}:${info?.httpPort}/subscriptions/upload"

                try {
                    logger.debug("Making request to $url")
                    val response = HttpClient(url, params = mapOf("stoppedSubscriptions" to listOf(stopped.keys.joinToString(",")))).jsonRequest("PUT", UploadSubscriptionsRequest(subscriptions), SubscriptionOperationResponse::class.java)
                    uploaded.addAll(response.modified)
                    notUploaded.addAll(response.notModified)
                } catch (e: IllegalStateException) {
                    notUploaded.addAll(subscriptions)
                }
            }
            DbController.uploadSubscriptionsConcurrently(subscriptions.associateBy { (JSONSerializer.deserializeWithClassName(it) as Subscription<*, *>).name }, subscriber, stopped)
        }

        val message = if (notUploaded.isEmpty()) {
            "All subscriptions has been successfully uploaded"
        } else if (uploaded.isNotEmpty()) {
            "Some subscriptions could not be uploaded"
        } else {
            "Operation revoked"
        }

        return SubscriptionOperationResponse(RequestType.UPLOAD, message, uploaded, notUploaded)
    }

    fun removeSubscriptions(subscriptions: List<String>): SubscriptionOperationResponse {
        val removed = mutableListOf<String>()
        val notRemoved = mutableListOf<String>()

        val groupedSubscriptions = subscriptionsBySubscriber(subscriptions)
        notRemoved.addAll(groupedSubscriptions[Optional.empty()] ?: listOf())

        groupedSubscriptions.entries.parallelStream().forEach { (subscriber, subscriptions) ->
            if (subscriber.isPresent) {
                if (subscriber.get() == subscriberName) {
                    parentSubscriber.removeSubscriptions(subscriptions, true)
                } else {
                    val info = subscribers[subscriber.get()]
                    val url = "http://${info?.fixedHost}:${info?.httpPort}/subscriptions/remove" // TODO: Check for HTTPS option

                    try {
                        logger.debug("Making request to $url")
                        val response = HttpClient(url).jsonRequest("PUT", subscriptions, SubscriptionOperationResponse::class.java)
                        removed.addAll(response.modified)
                        notRemoved.addAll(response.notModified)
                    } catch (e: IllegalStateException) {
                        notRemoved.addAll(subscriptions)
                    }
                }
                DbController.setSubscriptionsConcurrentlyByName(subscriptions, active = false)
            }
        }

        val message = if (notRemoved.isEmpty()) {
            "All subscriptions has been successfully uploaded"
        } else if (removed.isNotEmpty()) {
            "Some subscriptions could not be uploaded"
        } else {
            "Operation revoked"
        }

        return SubscriptionOperationResponse(RequestType.REMOVE, message, removed, notRemoved)
    }

    fun setSubscriptions(subscriptions: List<String>, stop: Boolean): SubscriptionOperationResponse {
        val set = mutableListOf<String>()
        val notSet = mutableListOf<String>()

        val groupedSubscriptions = subscriptionsBySubscriber(subscriptions)
        notSet.addAll(groupedSubscriptions[Optional.empty()] ?: listOf())

        groupedSubscriptions.entries.parallelStream().forEach { (subscriber, subscriptions) ->
            if (subscriber.isPresent) {
                if (subscriber.get() == subscriberName) {
                    parentSubscriber.setSubscriptions(subscriptions, stop, true)
                } else {
                    val info = subscribers[subscriber.get()]
                    val url = "http://${info?.fixedHost}:${info?.httpPort}/subscriptions/${if (stop) "stop" else "start"}" // TODO: Check for HTTPS option

                    try {
                        logger.debug("Making request to $url")
                        val response = HttpClient(url).jsonRequest("PUT", subscriptions, SubscriptionOperationResponse::class.java)
                        set.addAll(response.modified)
                        notSet.addAll(response.notModified)
                    } catch (e: IllegalStateException) {
                        notSet.addAll(subscriptions)
                    }
                }
                DbController.setSubscriptionsConcurrentlyByName(subscriptions, stopped = stop)
            }
        }

        val message = if (notSet.isEmpty()) {
            "All subscriptions has been successfully ${if (stop) "stopped" else "started"}"
        } else if (set.isNotEmpty()) {
            "Some subscriptions could not be ${if (stop) "stopped" else "started"}"
        } else {
            "Operation revoked"
        }

        return SubscriptionOperationResponse(RequestType.SET, message, set, notSet)
    }

    fun getLogs(name: String, count: Int): List<String> {
        val subscriber = subscribers.entries.firstOrNull { entry -> entry.value.subscriptions.containsKey(name) }?.key ?: throw IllegalArgumentException("Subscription doesn't exists")

        return if (subscriber == subscriberName) {
            parentSubscriber.getLogs(name, count, true)
        } else {
            val info = subscribers[subscriber]
            val url = "http://${info?.fixedHost}:${info?.httpPort}/subscription/logs" // TODO: Check for HTTPS option
            logger.debug("Making request to $url")
            HttpClient(url, params = mapOf("name" to listOf(name), "count" to listOf("$count")))
                .jsonRequest("GET", object: TypeReference<List<String>>() {})
        }
    }

    fun getLogFiles(name: String): InputStream? {
        val subscriber = subscribers.entries.firstOrNull { entry -> entry.value.subscriptions.containsKey(name) }?.key ?: throw IllegalArgumentException("Subscription doesn't exists")

        return if (subscriber == subscriberName) {
            parentSubscriber.getLogFiles(name, true)
        } else {
            val info = subscribers[subscriber]
            val url = "http://${info?.fixedHost}:${info?.httpPort}/subscription/logs/download" // TODO: Check for HTTPS option
            logger.debug("Making request to $url")
            HttpClient(url, params = mapOf("name" to listOf(name))).basicRequest("GET").use { res ->
                res.body?.byteStream()
            }
        }
    }

    fun listSubscribers(): Map<String, SubscriberInfo> {
        return subscribers
    }

    fun listSubscriptions(): List<SubscriptionInfo> {
        return subscribers.flatMap { it.value.subscriptions.values }
    }
    fun getSubscriptionInfo(name: String): SubscriptionInfo? {
        return subscribers.flatMap { it.value.subscriptions.values }.firstOrNull { it.name == name }
    }

    fun handleSubscriptionMessage(name: String, message: DirectMessage<*>): Any? {
        subscribers.filter { it.value.subscriptions.containsKey(name) }.keys.first().let {
            return if (it == subscriberName) {
                parentSubscriber.handleSubscriptionMessage(name, message, true)
            } else {
                val info = subscribers[it]
                val url = "http://${info?.fixedHost}:${info?.httpPort}/subscriptions/remove" // TODO: Check for HTTPS option
                logger.debug("Making request to $url")
                HttpClient(url).jsonRequest("POST", message, Any::class.java)
            }
        }
    }

    fun getSubscriptionsSchemas(): Map<String, String?> {
        return subscriptionSchemas
    }

    fun getSubscriptionHistorical(name: String): List<SubscriptionSerializableEntry> {
        return DbController.getSubscriptionHistorical(name)
    }

    fun getSubscriptionStatus(names: List<String>): Map<String, SubscriptionStatus> {
        return subscribers.flatMap { it.value.subscriptions.values }.filter { names.contains(it.name) }.associate { Pair(it.name, it.status) }
    }

    fun getSubscriptionsClasses(): Map<String, String> {
        return subscribers.flatMap { it.value.subscriptions.values }
            .map { JSONSerializer.deserialize(it.content, object : TypeReference<Map<String, Any>>() {}) }
            .filter { it.containsKey("className") }.associate {
                Pair(it["name"] as String, it["className"] as String)
            }
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Name of master node, obtaining from properties.
     */
    private val name = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("Required property doesn't exists: ${ConfigNames.PRIMARY_NAME}"))

    /**
     * Name of display node used to show information to users
     */
    private val displayNodeName = ConfigManager.getProperty(ConfigNames.DISPLAY_NODE_NAME, RuntimeException("Required property doesn't exists: ${ConfigNames.DISPLAY_NODE_NAME}"))

    /**
     * Logger object used to print logs.
     */
    private val logger = LoggingManager.getLogger(name)

    /**
     * Time between two secondary nodes checking.
     */
    private val checkSecondaryNodesPeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD, ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to check secondary nodes. If this thread is running more than these milliseconds, task is cleared and another task is thrown.
     */
    private val checkSecondaryNodesTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT, ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT_DEFAULT)

    /**
     * Maximum time during which a node may not send information to the head node.
     */
    private val maxSecondaryNodeInactivityTime = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME, ConfigNames.SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME_DEFAULT)

    /**
     * Time between two checks of subscriptions that are waiting to be re-allocated to same or another subscriber.
     */
    private val checkSubscriptionsToUploadPeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD, ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to check waiting subscriptions to be uploaded. If this thread is running more than these milliseconds, task is cleared and another task is thrown.
     */
    private val checkSubscriptionsToUploadTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT, ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT_DEFAULT)

    /**
     * Time between two data transmissions to the display node
     */
    private val sendInfoToDisplayNodePeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SEND_INFO_PERIOD, ConfigNames.SECONDARY_SEND_INFO_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to send information to display node. If this thread is running more than these milliseconds, task is cleared and another task is thrown
     */
    private val sendInfoToDisplayNodeTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SEND_INFO_TIMEOUT, ConfigNames.SECONDARY_SEND_INFO_TIMEOUT_DEFAULT)

    /**
     * Type of allocation strategy to send subscriptions to secondary subscribers
     */
    private val allocationStrategy = ConfigManager.getEnumProperty(ConfigNames.ALLOCATION_STRATEGY, AllocationStrategy::class.java, ConfigNames.ALLOCATION_STRATEGY_DEFAULT)

    /**
     * All secondary nodes with their last timestamp when primary node receives an information message from it
     */
    private val subscribers = ConcurrentHashMap<String, SubscriberInfo>()

    /**
     * List of subscriptions to reallocate due to node crash
     */
    private val fallenSubscriptionsBySubscriber = ConcurrentHashMap<String, Map<String, Pair<String, Boolean>>>()

    private val fallenSubscriptionsNeedToSend = ConcurrentHashMap<String, Boolean>()

    /**
     * Client used for communication with another subscribers
     */
    private val client = createClient(name)

    /**
     * Scheduler used to check if any secondary node is down
     */
    private val checkNodesSchedulerDelegate = lazy { object : PeriodicalScheduler(checkSecondaryNodesPeriod, 0, checkSecondaryNodesTimeout, name = subscriberName) {
        override fun onCycle() {
            val currentTime = System.currentTimeMillis()
            val downSubscribers = mutableListOf<String>()

            for ((name, info) in subscribers) {
                if (currentTime - info.timestamp > maxSecondaryNodeInactivityTime) {
                    subscribers.remove(name)
                    downSubscribers.add(name)
                }
            }

            onSubscriberDown(downSubscribers)
        }
    } }
    private val checkNodesScheduler by checkNodesSchedulerDelegate

    private val checkWaitingSubscriptionsToUploadSchedulerDelegate = lazy { object : PeriodicalScheduler(checkSubscriptionsToUploadPeriod, 0, checkSubscriptionsToUploadTimeout, name = subscriberName) {
        override fun onCycle() {
            for ((subscriber, subscriptions) in fallenSubscriptionsBySubscriber) {
                if (fallenSubscriptionsNeedToSend[subscriber] == true) {
                    val response = if (allocationStrategy == AllocationStrategy.FIXED) {
                        uploadSubscriptions(subscriptions.values.map { it.first }.toList(), subscriber, stopped = subscriptions.entries.associate { Pair(it.key, it.value.second) })
                    } else {
                        uploadSubscriptions(subscriptions.values.map { it.first }.toList(), stopped = subscriptions.entries.associate { Pair(it.key, it.value.second) })
                    }

                    if (response.result == SubscriptionOperationResult.PARTIAL) {
                        fallenSubscriptionsBySubscriber[subscriber] = subscriptions.filter { subscription -> subscription.key in response.notModified }
                    } else if (response.result == SubscriptionOperationResult.TOTAL) {
                        fallenSubscriptionsBySubscriber.remove(subscriber)
                        fallenSubscriptionsNeedToSend.remove(subscriber)
                    }
                }
            }
        }
    } }
    private val checkWaitingSubscriptionsToUploadScheduler by checkWaitingSubscriptionsToUploadSchedulerDelegate

    private val sendInfoToDisplayNodeSchedulerDelegate = lazy { object : PeriodicalScheduler(sendInfoToDisplayNodePeriod, 0, sendInfoToDisplayNodeTimeout, name = subscriberName) {
        override fun onCycle() {
            sendMessage(SubscriberInfo(name, mapOf(), true), client, displayNodeName)
        }
    } }
    private val sendInfoToDisplayNodeScheduler by sendInfoToDisplayNodeSchedulerDelegate

    private var lastSubscriberInfoReceptionTime = System.currentTimeMillis()
    private val checkMasterRoleSchedulerDelegate = lazy { object: PeriodicalScheduler(1000, 0, 1000, name = subscriberName, this) {
        override fun onCycle() {
            if (subscribers.isNotEmpty() && (System.currentTimeMillis() - lastSubscriberInfoReceptionTime) > maxSecondaryNodeInactivityTime) {
                logger.info("A main node already exists, stopping this...")
                (this.params[0] as MainSubscriber).stop()
            }
        }
    }}
    private val checkMasterRoleScheduler by checkMasterRoleSchedulerDelegate

    private val subscriptionSchemas = loadSubscriptionSchemas()

    // endregion

    // region PRIVATE METHODS

    private fun onSubscriberDown(subscribers: List<String>) {
        if (subscribers.isNotEmpty()) {
            logger.info("Nodes fallen detected: $subscribers")
        }

        subscribers.forEach { subscriber ->
            val subscriptionsForSubscriber = DbController.getLastActiveSubscriptionsBySubscriber(subscriber).associate { Pair(it.name, Pair(String(it.content.bytes), it.stopped)) }
            fallenSubscriptionsBySubscriber[subscriber] = subscriptionsForSubscriber
            fallenSubscriptionsNeedToSend[subscriber] = true
        }
    }

    private fun makeRanking(): List<String> {
        return when (allocationStrategy) {
            AllocationStrategy.FIXED -> subscribers.values.shuffled().map { info -> info.name }
            AllocationStrategy.CPU -> subscribers.values.sortedBy { info -> info.cpuUsage }.map { info -> info.name }
            AllocationStrategy.MEMORY -> subscribers.values.sortedByDescending { info -> info.freeMemory }.map { info -> info.name }
            AllocationStrategy.CPU_MEMORY -> subscribers.values.sortedWith(compareBy<SubscriberInfo> { info -> info.cpuUsage }.thenByDescending { info -> info.freeMemory }).map { info -> info.name }
            AllocationStrategy.MEMORY_CPU -> subscribers.values.sortedWith(compareByDescending<SubscriberInfo> { info -> info.freeMemory }.thenBy { info -> info.cpuUsage }).map { info -> info.name }
            AllocationStrategy.OCCUPATION -> subscribers.values.sortedBy { info -> info.subscriptions.size }.map { info -> info.name }
        }
    }

    private fun subscriptionsBySubscriber(subscriptions: List<String>): Map<Optional<String>, List<String>> {
        return subscriptions.groupBy { subscription -> subscribers.filter { (_, info) -> info.subscriptions.any { (name, _) -> name == subscription } }.map { Optional.of(it.key) }.firstOrNull() ?: Optional.empty<String>() }
    }

    private fun loadSubscriptionSchemas(): Map<String, String?> {
        val packageSet = ConfigManager.getProperties(ConfigNames.CUSTOM_SUBSCRIPTIONS_PACKAGES).toSet()

        val result = packageSet.parallelStream().flatMap { packageName ->
            Reflections(packageName).getSubTypesOf(Subscription::class.java).filter { !it.kotlin.isAbstract }.stream()
        }.asSequence().associate { Pair(it.name, JSONSerializer.getSchema(it, false)) }

        return result
    }

    // endregion

    init {
        DbController.initialize()
    }
}