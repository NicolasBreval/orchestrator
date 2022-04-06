package org.nitb.orchestrator.subscriber

import org.nitb.orchestrator.cloud.CloudConsumer
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.DbController
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy
import org.nitb.orchestrator.subscriber.entities.subscribers.RequestResult
import org.nitb.orchestrator.subscriber.entities.subscribers.RequestStatus
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import org.nitb.orchestrator.subscriber.entities.subscriptions.remove.RemoveSubscriptionRequest
import org.nitb.orchestrator.subscriber.entities.subscriptions.remove.RemoveSubscriptionResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionsRequest
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class MainSubscriber: CloudManager<Serializable>, CloudConsumer<Serializable>, CloudSender {

    // region PUBLIC METHODS

    fun start() {
        registerConsumer(client) { message ->
            when (message.message) {
                is SubscriberInfo -> subscribers[message.sender] = message.message
                is UploadSubscriptionsRequest -> {
                    logger.debug("Upload subscriptions request received from ${message.sender}")
                    uploadSubscriptions(message.message.subscriptions, message.message.subscriber, message.message.id)
                }
                is UploadSubscriptionResponse -> {
                    logger.debug("Upload subscriptions response received from ${message.sender}")

                    waitingUploadRequests[message.message.id].let { requestResult ->
                        waitingUploadRequests[message.message.id] = RequestResult(
                            if (message.message.status == ResponseStatus.OK) {
                                RequestStatus.OK
                            } else {
                                RequestStatus.ERROR
                            },
                            if (message.message.status == ResponseStatus.OK) {
                                "Subscriptions has been successfully uploaded"
                            } else {
                                "Error uploading subscriptions"
                            },
                            requestResult?.parent
                        )
                    }
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
        checkCompletedRequestsScheduler.start()
    }

    fun stop() {
        if (checkNodesSchedulerDelegate.isInitialized()) {
            checkNodesScheduler.stop()
        }

        if (checkWaitingSubscriptionsToUploadSchedulerDelegate.isInitialized()) {
            checkWaitingSubscriptionsToUploadScheduler.stop()
        }

        if (removeCompletedRequestsSchedulerDelegate.isInitialized()) {
            checkCompletedRequestsScheduler.stop()
        }

        client.close()
    }

    fun subscriptionUploadResult(id: String): RequestResult {
        return if (!waitingUploadRequests.containsKey(id)) {
            val countPerStatus = RequestStatus.values().associateWith { AtomicInteger(0) }

            waitingUploadRequests.filter { (_, value) -> value.parent == id }.forEach { (_, value) ->
                countPerStatus[value.status]?.incrementAndGet()
            }

            if ((countPerStatus[RequestStatus.WAITING]?.get() ?: 0) > 0) {
                RequestResult(RequestStatus.WAITING)
            } else if ((countPerStatus[RequestStatus.ERROR]?.get() ?: 0) > 0) {
                RequestResult(RequestStatus.ERROR)
            } else if ((countPerStatus[RequestStatus.OK]?.get() ?: 0) > 0) {
                RequestResult(RequestStatus.OK)
            } else {
                RequestResult(RequestStatus.DELETED)
            }
        } else {
            waitingUploadRequests[id] ?: RequestResult(RequestStatus.DELETED)
        }
    }

    

    // endregion

    // region PRIVATE PROPERTIES

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
    private val fallenSubscriptionsBySubscriber = ConcurrentHashMap<String, List<String>>()

    private val fallenSubscriptionsNeedToSend = ConcurrentHashMap<String, Boolean>()

    private val waitingUploadRequests = ConcurrentHashMap<String, RequestResult>()

    private val waitingRemoveRequests = ConcurrentHashMap<String, RequestResult>()

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
            for ((subscriber, subscriptions) in fallenSubscriptionsBySubscriber) {
                if (fallenSubscriptionsNeedToSend[subscriber] == true) {
                    uploadSubscriptions(subscriptions, subscriber, UUID.randomUUID().toString(), true)
                }
                fallenSubscriptionsNeedToSend[subscriber] = false
            }
        }
    } }
    private val checkWaitingSubscriptionsToUploadScheduler by checkWaitingSubscriptionsToUploadSchedulerDelegate

    private val removeCompletedRequestsSchedulerDelegate = lazy { object : PeriodicalScheduler(3600000, 0, 3600000) {
        override fun onCycle() {
            val current = System.currentTimeMillis()

            for ((id, request) in waitingUploadRequests) {
                if (current - request.creation > 3600000) {
                    waitingUploadRequests.remove(id)
                }
            }

            for ((id, request) in waitingRemoveRequests) {
                if (current - request.creation > 3600000) {
                    waitingRemoveRequests.remove(id)
                }
            }
        }
    } }
    private val checkCompletedRequestsScheduler by removeCompletedRequestsSchedulerDelegate

    // endregion

    // region PRIVATE METHODS

    private fun onSubscriberDown(subscribers: List<String>) {
        if (subscribers.isNotEmpty()) {
            logger.info("Nodes fallen detected: $subscribers")
        }

        subscribers.forEach { subscriber ->
            val subscriptionsForSubscriber = DbController.getLastActiveSubscriptionsBySubscriber(subscriber).map { String(it.content) }
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

    private fun uploadSubscriptions(subscriptions: List<String>, subscriber: String? = null, id: String, fromFallenSubscriber: Boolean = false) {
        if (allocationStrategy == AllocationStrategy.FIXED && subscriber != null) {
            if (!subscribers.containsKey(subscriber)) {
                if (!fromFallenSubscriber) {
                    waitingUploadRequests[id] = RequestResult(RequestStatus.ERROR, "Unable to upload subscriptions to a non-exists subscriber when allocation type is FIXED")
                } else {
                    fallenSubscriptionsNeedToSend[subscriber] = true
                }
            } else {
                sendMessage(UploadSubscriptionsRequest(subscriptions, subscriber, id), client, subscriber)

                if (fromFallenSubscriber) {
                    fallenSubscriptionsNeedToSend[subscriber] = false
                }

                waitingUploadRequests[id] = RequestResult(RequestStatus.WAITING)
            }
        } else {
            val ranking = makeRanking()

            subscriptions.withIndex().groupBy { (index, _) ->
                ranking[index % ranking.size]
            }.forEach { (subscriber, subscriptions) ->
                sendMessage(UploadSubscriptionsRequest(subscriptions.map { it.value }, subscriber, id), client, subscriber)
                val childId = UUID.randomUUID().toString()
                waitingUploadRequests[childId] = RequestResult(RequestStatus.WAITING, parent=id)
            }
        }
    }


    // endregion

}