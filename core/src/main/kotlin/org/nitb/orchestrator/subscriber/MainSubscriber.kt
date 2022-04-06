package org.nitb.orchestrator.subscriber

import org.nitb.orchestrator.cloud.CloudConsumer
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.DbController
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.subscriber.entities.*
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractRequest
import org.nitb.orchestrator.subscriber.entities.subscriptions.AbstractResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import org.nitb.orchestrator.subscriber.entities.subscriptions.remove.RemoveSubscriptionRequest
import org.nitb.orchestrator.subscriber.entities.subscriptions.remove.RemoveSubscriptionResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionsRequest
import org.nitb.orchestrator.subscription.SubscriptionStatus
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class MainSubscriber: CloudManager<Serializable>, CloudConsumer<Serializable>, CloudSender {

    // region PUBLIC METHODS

    fun start() {
        registerConsumer(client) { message ->
            when (message.message) {
                is SubscriberInfo -> subscribers[message.sender] = message.message
                is UploadSubscriptionsRequest -> {
                    logger.debug("Upload subscriptions request received from ${message.sender}")
                    uploadSubscriptions(message.message.subscriptions, message.message.subscriber)
                }
                is UploadSubscriptionResponse -> {
                    logger.debug("Upload subscriptions response received from ${message.sender}")
                    waitingResponses[message.message.id] = message.message

                    if (message.message.id == fallenSubscriptionsOperationId.get()) {
                        fallenSubscriptionsOperationId.set("")
                    }

                    DbController.insertSubscriptions(message.message.subscriptions.map { info ->
                        SubscriptionEntry(info.name, info.content.toByteArray(Charsets.UTF_8), message.sender, info.status == SubscriptionStatus.STOPPED, true)
                    })
                }
                is RemoveSubscriptionRequest -> {
                    logger.debug("Remove subscriptions request received from ${message.sender}")
                }
                is RemoveSubscriptionResponse -> {
                    logger.debug("Remove subscriptions response received from ${message.sender}")

                }
                else -> logger.error("Unrecognized message type has been received. Type: ${message::class.java.name}")
            }
        }

        checkNodesScheduler.start()
        checkWaitingSubscriptionsToUploadScheduler.start()
        checkAutomaticOperations.start()
    }

    fun stop() {
        if (checkNodesSchedulerDelegate.isInitialized()) {
            checkNodesScheduler.stop()
        }

        if (checkWaitingSubscriptionsToUploadSchedulerDelegate.isInitialized()) {
            checkWaitingSubscriptionsToUploadScheduler.stop()
        }

        if (checkAutomaticOperationsDelegate.isInitialized()) {
            checkAutomaticOperations.stop()
        }

        client.close()
    }

    // endregion

    // region PRIVATE PROPERTIES

    private val subscriberlessName = "SUBSCRIBERLESS"

    /**
     * Name of master node, obtaining from properties
     */
    private val name = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("Needed property doesn't exists: ${ConfigNames.PRIMARY_NAME}"))

    /**
     * Logger object used to print logs
     */
    private val logger = LoggingManager.getLogger(name)

    /**
     * Time between two secondary nodes checking
     */
    private val checkSecondaryNodesPeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD, ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to check secondary nodes. If this thread is running more than these milliseconds, task is cleared and another task is thrown
     */
    private val checkSecondaryNodesTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT, ConfigNames.SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT_DEFAULT)

    /**
     * Maximum time during which a node may not send information to the head node
     */
    private val maxSecondaryNodeInactivityTime = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME, ConfigNames.SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME_DEFAULT)

    /**
     * Time between two checks of subscriptions that are waiting to be re-allocated to same or another subscriber
     */
    private val checkSubscriptionsToUploadPeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD, ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to check waiting subscriptions to be uploaded. If this thread is running more than these milliseconds, task is cleared and another task is thrown
     */
    private val checkSubscriptionsToUploadTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT, ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT_DEFAULT)

    private val checkSubscriptionsToRemovePeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_PERIOD, ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_PERIOD_DEFAULT)

    private val checkSubscriptionsToRemoveTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_TIMEOUT, ConfigNames.SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_TIMEOUT_DEFAULT)

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
    private val fallenSubscriptionsBySubscriber = ConcurrentHashMap<String, MutableList<String>>()

    private val fallenSubscriptionsOperationId: AtomicReference<String> = AtomicReference("")

    /**
     * All requests send to main subscriber that are not resolved yet
     */
    private val waitingRequests = ConcurrentHashMap<String, AbstractRequest>()

    private val waitingResponses = ConcurrentHashMap<String, AbstractResponse>()

    private val automaticOperations = Collections.synchronizedList(mutableListOf<String>())

    /**
     * Client used for communication with another subscribers
     */
    private val client = createClient(name)

    /**
     * Scheduler used to check if any secondary node is down
     */
    private val checkNodesSchedulerDelegate = lazy { object : PeriodicalScheduler(checkSecondaryNodesPeriod, 0, checkSecondaryNodesTimeout) {
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

    private val checkWaitingSubscriptionsToUploadSchedulerDelegate = lazy { object : PeriodicalScheduler(checkSubscriptionsToUploadPeriod, 0, checkSubscriptionsToUploadTimeout) {
        override fun onCycle() {
            if (fallenSubscriptionsOperationId.get().isNotEmpty()) {
                fallenSubscriptionsBySubscriber.forEach { (subscriber, subscriptions) ->
                    automaticOperations.add(uploadSubscriptions(subscriptions, subscriber))
                    fallenSubscriptionsBySubscriber.remove(subscriber)
                }
            }
        }
    } }
    private val checkWaitingSubscriptionsToUploadScheduler by checkWaitingSubscriptionsToUploadSchedulerDelegate

    private val checkAutomaticOperationsDelegate = lazy { object : PeriodicalScheduler(checkSubscriptionsToRemovePeriod, 0, checkSubscriptionsToRemoveTimeout) {
        override fun onCycle() {
            for (operation in automaticOperations) {
                if (waitingRequests.containsKey(operation) && waitingResponses.containsKey(operation)) {
                    if (waitingRequests[operation] is UploadSubscriptionsRequest && waitingResponses[operation] is UploadSubscriptionResponse) {
                        if ((waitingResponses[operation] as UploadSubscriptionResponse).status == ResponseStatus.OK) {
                            logger.debug("Success operation received with id $operation")
                        } else {
                            logger.debug("Fail operation received with id $operation")
                        }
                        waitingResponses[operation]?.finished = true
                    } else {
                        logger.error("Unknown response received for a pending request. ${waitingRequests[operation]!!::class.java} vs ${waitingResponses[operation]!!::class.java}")
                        waitingResponses.remove(operation)
                    }
                    waitingRequests.remove(operation)
                } else if (waitingRequests.containsKey(operation) && !waitingResponses.containsKey(operation) && (System.currentTimeMillis() - waitingRequests[operation]!!.creation) > 5000) {
                    waitingRequests.remove(operation)
                } else if (!waitingRequests.containsKey(operation) && waitingResponses.containsKey(operation)) {
                    waitingResponses.remove(operation)
                }
            }
        }
    } }
    private val checkAutomaticOperations by checkAutomaticOperationsDelegate

    // endregion

    // region PRIVATE METHODS

    private fun onSubscriberDown(subscribers: List<String>) {
        if (subscribers.isNotEmpty()) {
            logger.info("Nodes fallen detected: $subscribers")
        }

        subscribers.forEach { subscriber ->
            val subscriptionEntries = DbController.getLastActiveSubscriptionsBySubscriber(subscriber)
            fallenSubscriptionsOperationId.set(UUID.randomUUID().toString())
            automaticOperations.add(uploadSubscriptions(subscriptionEntries.map { String(it.content) }, subscriber, fallenSubscriptionsOperationId.get()))
        }
    }

    private fun makeRanking(): List<String> {
        return when (allocationStrategy) {
            AllocationStrategy.CPU -> subscribers.values.sortedBy { info -> info.cpuUsage }.map { info -> info.name }
            AllocationStrategy.MEMORY -> subscribers.values.sortedByDescending { info -> info.freeMemory }.map { info -> info.name }
            AllocationStrategy.CPU_MEMORY -> subscribers.values.sortedWith(compareBy<SubscriberInfo> { info -> info.cpuUsage }.thenByDescending { info -> info.freeMemory }).map { info -> info.name }
            AllocationStrategy.MEMORY_CPU -> subscribers.values.sortedWith(compareByDescending<SubscriberInfo> { info -> info.freeMemory }.thenBy { info -> info.cpuUsage }).map { info -> info.name }
            AllocationStrategy.OCCUPATION -> subscribers.values.sortedBy { info -> info.subscriptions.size }.map { info -> info.name }
            else -> throw RuntimeException("Impossible case: trying to obtain ranking with invalid allocation strategy")
        }
    }

    private fun reallocateSubscriptions(subscriptions: List<String>, subscriber: String, id: String) {
        val request = UploadSubscriptionsRequest(subscriptions, subscriber, id)
        sendMessage(request, client, subscriber)
        waitingRequests[id] = request
    }

    private fun uploadSubscriptions(subscriptions: List<String>, subscriber: String? = null, id: String? = UUID.randomUUID().toString()): String {
        val finalId = id ?: UUID.randomUUID().toString()

        if (allocationStrategy == AllocationStrategy.FIXED) {
            if (subscriber == null)
                throw IllegalArgumentException("Invalid parameter `subscriber`. When allocation strategy is FIXED, subscriber name must be not null")

            if (subscribers.containsKey(subscriber)) {
                reallocateSubscriptions(subscriptions, subscriber, finalId)
            } else {
                fallenSubscriptionsBySubscriber.computeIfAbsent(subscriber) { mutableListOf() }.addAll(subscriptions)
            }
        } else {
            if (subscribers.isNotEmpty()) {
                val rankedSubscribers = makeRanking()

                subscriptions.withIndex().groupBy { (i, _) ->
                    rankedSubscribers[i % rankedSubscribers.size]
                }.mapValues { (_, value) -> value.map { it.value } }.forEach { (subscriber, subscriptions) ->
                    reallocateSubscriptions(subscriptions, subscriber, finalId)
                }
            } else {
                fallenSubscriptionsBySubscriber.computeIfAbsent(subscriberlessName) { mutableListOf() }.addAll(subscriptions)
            }
        }

        return finalId
    }


    // endregion

}