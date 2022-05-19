package org.nitb.orchestrator.subscriber

import org.nitb.orchestrator.amqp.AmqpConsumer
import org.nitb.orchestrator.amqp.AmqpManager
import org.nitb.orchestrator.amqp.AmqpSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.RequestType
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionOperationResponse
import org.nitb.orchestrator.subscription.Subscription
import org.nitb.orchestrator.subscription.entities.DirectMessage
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Subscriber(
    private val name: String = if (ConfigManager.getEnumProperty(ConfigNames.ALLOCATION_STRATEGY, AllocationStrategy::class.java, ConfigNames.ALLOCATION_STRATEGY_DEFAULT) == AllocationStrategy.FIXED)
        ConfigManager.getProperty(ConfigNames.SECONDARY_NAME, RuntimeException("${ConfigNames.SECONDARY_NAME} property is required when allocation type is ${AllocationStrategy.FIXED}"))
    else UUID.randomUUID().toString()
): AmqpManager<Serializable>, AmqpConsumer<Serializable>, AmqpSender {


    // region PUBLIC PROPERTIES

    /**
     * Flag to check if subscriber has master role
     */
    var isMainNode: Boolean = false

    // endregion

    // region PUBLIC METHODS

    fun stop() {
        subscriptionsPool.forEach { (name, subscription) ->
            logger.info("Deleting subscription $name due to a stop invocation")
            subscription.stop()
        }

        sendInformationScheduler.stop()
        checkMainNodeExistsScheduler.stop()
        mainSubscriber.stop()

        client.close()
    }

    fun start() {
        sendInformationScheduler.start()
        checkMainNodeExistsScheduler.start()
    }

    fun uploadSubscriptions(subscriptions: List<String>, subscriber: String? = null, force: Boolean = false): SubscriptionOperationResponse {
        return try {
            if (isMainNode && !force) {
                mainSubscriber.uploadSubscriptions(subscriptions, subscriber)
            } else {
                subscriptions
                    .map { subscription -> JSONSerializer.deserializeWithClassName(subscription) as Subscription<*, *> }
                    .filter { subscription -> subscriptionsPool[subscription.name]?.info?.content != subscription.info.content }
                    .forEach { subscription ->
                        subscriptionsPool[subscription.name] = subscription
                        subscription.start()
                    }

                val info = SubscriberInfo(name,
                    subscriptionsPool.values.associate { subscription ->
                        Pair(
                            subscription.name,
                            subscription.info
                        )
                    }, name == mainNodeName)
                client.send(mainNodeName, info)

                return SubscriptionOperationResponse(RequestType.UPLOAD, "All subscriptions has been uploading", subscriptions)
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during subscriptions uploading", e)
            return SubscriptionOperationResponse(RequestType.UPLOAD, "Unexpected error during subscriptions uploading", listOf(), subscriptions)
        }
    }

    fun removeSubscriptions(subscriptions: List<String>, force: Boolean = false): SubscriptionOperationResponse {
        return try {
            if (isMainNode && !force) {
                mainSubscriber.removeSubscriptions(subscriptions)
            } else {
                subscriptions.forEach { subscriptionName ->
                    subscriptionsPool[subscriptionName]?.stop()
                    subscriptionsPool.remove(subscriptionName)
                }

                return SubscriptionOperationResponse(RequestType.REMOVE, "All subscriptions has been removed", subscriptions)
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during subscriptions removing", e)
            return SubscriptionOperationResponse(RequestType.REMOVE, "Unexpected error during subscriptions removing", listOf(), subscriptions)
        }
    }

    fun setSubscriptions(subscriptions: List<String>, stop: Boolean, force: Boolean = false): SubscriptionOperationResponse {
        return try {
            if (isMainNode && !force) {
                mainSubscriber.setSubscriptions(subscriptions, stop)
            } else {
                subscriptions.forEach { subscriptionName -> subscriptionsPool[subscriptionName]?.let { if (stop) it.stop() else it.start() } }

                return SubscriptionOperationResponse(RequestType.REMOVE, "All subscriptions has been set", subscriptions)
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during subscriptions set", e)
            return SubscriptionOperationResponse(RequestType.REMOVE, "Unexpected error during subscriptions set", listOf(), subscriptions)
        }
    }

    fun listSubscribers(): Map<String, SubscriberInfo> {
        if (isMainNode) {
            return mainSubscriber.listSubscribers()
        } else {
            throw IllegalAccessException("INVALID REQUEST - This node is not the main node.")
        }
    }

    fun listSubscriptions(): List<SubscriptionInfo> {
        if (isMainNode) {
            return mainSubscriber.listSubscriptions()
        } else {
            throw IllegalAccessException("INVALID REQUEST - This node is not the main node.")
        }
    }

    fun getSubscriptionInfo(name: String): SubscriptionInfo? {
        if (isMainNode) {
            return mainSubscriber.getSubscriptionInfo(name)
        } else {
            throw IllegalAccessException("INVALID REQUEST - This node is not the main node.")
        }
    }

    fun handleSubscriptionMessage(name: String, message: DirectMessage): Any? {
        return if (isMainNode) {

        } else {
            subscriptionsPool[name]?.handleMessage(message)
        }
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Logger object to print logs
     */
    private val logger = LoggingManager.getLogger(name)

    /**
     * Name of master node. This name is used to declare master queue in queues system
     */
    private val mainNodeName = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("Needed property doesn't exists: ${ConfigNames.PRIMARY_NAME}"))

    /**
     * Time between two data transmissions to the head node
     */
    private val subscriberSendInfoPeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SEND_INFO_PERIOD, ConfigNames.SECONDARY_SEND_INFO_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to send information to head node. If this thread is running more than these milliseconds, task is cleared and another task is thrown
     */
    private val subscriberSendInfoTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SEND_INFO_TIMEOUT, ConfigNames.SECONDARY_SEND_INFO_TIMEOUT_DEFAULT)

    /**
     * Time between two verification of the existence of the main node
     */
    private val checkMainNodeExistPeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_PERIOD, ConfigNames.SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to check the existence of the main node. If this thread is running more than these milliseconds, task is cleared and another task is thrown
     */
    private val checkMainNodeExistsTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_TIMEOUT, ConfigNames.SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_TIMEOUT_DEFAULT)

    /**
     * Contains all active subscriptions in this slave
     */
    private val subscriptionsPool: ConcurrentMap<String, Subscription<*, *>> = ConcurrentHashMap()

    /**
     * Client used for communication with another subscribers
     */
    private val client = createClient(name)

    /**
     * Main subscriber instance to use if this subscriber has main node role
     */
    private val mainSubscriber = MainSubscriber(this, name)

    /**
     * This scheduler sends slave information continuously to master
     */
    private val sendInformationScheduler by lazy {
        object : PeriodicalScheduler(subscriberSendInfoPeriod, 0, subscriberSendInfoTimeout, name = name) {
            override fun onCycle() {
                val info = SubscriberInfo(name,
                    subscriptionsPool.values.associate { subscription ->
                        Pair(
                            subscription.name,
                            subscription.info
                        )
                    }, name == mainNodeName)
                client.send(mainNodeName, info)
                logger.debug("Node info sent to master with ${info.subscriptions.count()} subscriptions")
            }
        }
    }

    /**
     * This scheduler checks continuously if master node is up. When main node is down, if [ConfigNames.ALLOCATION_STRATEGY] is not [AllocationStrategy.FIXED]
     * tries to take main node's role
     */
    private val checkMainNodeExistsScheduler by lazy {
        object : PeriodicalScheduler(checkMainNodeExistPeriod, 0, checkMainNodeExistsTimeout, name = name) {
            override fun onCycle() {
                if (!masterConsuming(client)) {
                    logger.info("Master node is fallen, obtaining master role")
                    isMainNode = true
                    mainSubscriber.start()
                }
            }
        }
    }

    // endregion


    // region INIT

    init {
        start()
    }

    // endregion
}