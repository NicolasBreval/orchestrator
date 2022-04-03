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
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.entities.*
import org.nitb.orchestrator.subscription.Subscription
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MainSubscriber: CloudManager<Serializable>, CloudConsumer<Serializable>, CloudSender {

    // region PUBLIC METHODS

    fun start() {
        registerConsumer(client) { message ->
            when (message.message) {
                is SubscriberInfo -> {
                    secondaryNodes[message.sender] = message.message

                }
                is UploadSubscriptionResponse -> {
                    if (message.message.status == UploadSubscriptionStatus.ERROR) {
                        waitingSubscriptionsToReceiveAck[message.message.id]?.forEach { (name, subscription) ->
                            waitingSubscriptionsToUpload.computeIfAbsent(subscription.subscriber) { ConcurrentHashMap() }[name] = subscription
                        }
                    }
                    waitingSubscriptionsToReceiveAck[message.message.id]?.forEach { (_, subscription) ->
                        subscription.subscriber = message.sender
                    }

                    DbController.insertSubscriptions(waitingSubscriptionsToReceiveAck[message.message.id]?.values?.toList() ?: listOf())

                    waitingSubscriptionsToReceiveAck.remove(message.message.id)
                }
                is UploadSubscriptionsReq -> {
                    message.message.subscriptions.forEach { subscription ->
                        val deserialized = JSONSerializer.deserializeWithClassName(subscription) as Subscription<*, *>
                        waitingSubscriptionsToUpload.computeIfAbsent(message.message.subscriber ?: "") { ConcurrentHashMap() }[deserialized.name] =
                            SubscriptionEntry(deserialized.name, subscription.toByteArray(), message.message.subscriber ?: "", stopped = false, active = true)
                    }
                    uploadSubscriptions()
                }
                else -> logger.error("Unrecognized message type has been received. Type: ${message::class.java.name}")
            }
        }

        checkNodesScheduler.start()
        checkWaitingSubscriptionsToUploadScheduler.start()
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
    private val secondaryNodes = ConcurrentHashMap<String, SubscriberInfo>()

    /**
     * List of subscriptions, grouped by unique id, which their subscribers have been fallen
     */
    private val waitingSubscriptionsToUpload = ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionEntry>>()

    /**
     * List of subscriptions, grouped by subscriber, that are waiting to check if has been uploaded to subscriber
     */
    private val waitingSubscriptionsToReceiveAck = ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionEntry>>()

    /**
     * Client used for communication with another subscribers
     */
    private val client = createClient(name)

    /**
     * Scheduler used to check if any secondary node is down
     */
    private val checkNodesScheduler by lazy { object : PeriodicalScheduler(checkSecondaryNodesPeriod, 0, checkSecondaryNodesTimeout) {
        override fun onCycle() {
            val currentTime = System.currentTimeMillis()

            for ((name, info) in secondaryNodes) {
                if (currentTime - info.timestamp > maxSecondaryNodeInactivityTime) {
                    secondaryNodes.remove(name)
                }
            }
        }
    } }

    private val checkWaitingSubscriptionsToUploadScheduler by lazy { object : PeriodicalScheduler(checkSubscriptionsToUploadPeriod, 0, checkSubscriptionsToUploadTimeout) {
        override fun onCycle() {
            uploadSubscriptions()
        }
    }
    }

    // endregion

    // region PRIVATE METHODS

    private fun onSubscriberDown(subscribers: List<String>) {
        val subscriptionsBySubscriber = subscribers.associateWith { subscriber -> DbController.getLastActiveSubscriptionsBySlave(subscriber) }

        for ((subscriber, subscriptions) in subscriptionsBySubscriber) {
            val currentSubscriptions = waitingSubscriptionsToUpload.computeIfAbsent(subscriber) { ConcurrentHashMap() }

            for (subscription in subscriptions) {
                currentSubscriptions[subscription.name] = subscription
            }
        }

        uploadSubscriptions()
    }

    private fun makeRanking(): List<String> {
        return when (allocationStrategy) {
            AllocationStrategy.CPU -> secondaryNodes.values.sortedBy { info -> info.cpuUsage }.map { info -> info.name }
            AllocationStrategy.MEMORY -> secondaryNodes.values.sortedByDescending { info -> info.freeMemory }.map { info -> info.name }
            AllocationStrategy.CPU_MEMORY -> secondaryNodes.values.sortedWith(compareBy<SubscriberInfo> { info -> info.cpuUsage }.thenByDescending { info -> info.freeMemory }).map { info -> info.name }
            AllocationStrategy.MEMORY_CPU -> secondaryNodes.values.sortedWith(compareByDescending<SubscriberInfo> { info -> info.freeMemory }.thenBy { info -> info.cpuUsage }).map { info -> info.name }
            AllocationStrategy.OCCUPATION -> secondaryNodes.values.sortedBy { info -> info.subscriptions.size }.map { info -> info.name }
            else -> throw RuntimeException("Impossible case: trying to obtain ranking with invalid allocation strategy")
        }
    }

    private fun sendRequestToSubscriber(subscriptions: ConcurrentHashMap<String, SubscriptionEntry>, subscriber: String) {
        val id = UUID.randomUUID().toString()
        sendMessage(UploadSubscriptionsReq(subscriptions.map { subscription -> String(subscription.value.content) }, subscriber, id), client, subscriber)
        waitingSubscriptionsToReceiveAck[id] = subscriptions
        subscriptions.forEach { (name, _) ->
            waitingSubscriptionsToUpload[subscriber]?.remove(name)
        }
    }

    private fun uploadSubscriptions() {
        if (allocationStrategy == AllocationStrategy.FIXED) {
            for ((subscriber, subscriptions) in waitingSubscriptionsToUpload) {
                if (secondaryNodes.containsKey(subscriber)) {
                    sendRequestToSubscriber(subscriptions, subscriber)
                }
            }
        } else {
            if (secondaryNodes.isNotEmpty()) {
                val rankedSubscribers = makeRanking()

                var i = 0
                for ((_, subscriptions) in waitingSubscriptionsToUpload) {
                    val subscriber = rankedSubscribers[i % rankedSubscribers.size]
                    sendRequestToSubscriber(subscriptions, subscriber)
                    i++
                }
            }
        }
    }

    // endregion

}