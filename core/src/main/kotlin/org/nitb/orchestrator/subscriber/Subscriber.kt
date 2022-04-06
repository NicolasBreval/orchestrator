package org.nitb.orchestrator.subscriber

import org.nitb.orchestrator.cloud.CloudConsumer
import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.cloud.CloudSender
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.serialization.json.JSONSerializer
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy
import org.nitb.orchestrator.subscriber.entities.subscribers.SubscriberInfo
import org.nitb.orchestrator.subscriber.entities.subscriptions.ResponseStatus
import org.nitb.orchestrator.subscriber.entities.subscriptions.remove.RemoveSubscriptionRequest
import org.nitb.orchestrator.subscriber.entities.subscriptions.remove.RemoveSubscriptionResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionResponse
import org.nitb.orchestrator.subscriber.entities.subscriptions.upload.UploadSubscriptionsRequest
import org.nitb.orchestrator.subscription.Subscription
import java.io.Serializable
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Subscriber(
    private val name: String = if (ConfigManager.getEnumProperty(ConfigNames.ALLOCATION_STRATEGY, AllocationStrategy::class.java, ConfigNames.ALLOCATION_STRATEGY_DEFAULT) == AllocationStrategy.FIXED)
        ConfigManager.getProperty(ConfigNames.SECONDARY_NAME, RuntimeException("${ConfigNames.SECONDARY_NAME} property is required when allocation type is ${AllocationStrategy.FIXED}"))
    else UUID.randomUUID().toString()
): CloudManager<Serializable>, CloudConsumer<Serializable>, CloudSender {

    /**
     * Flag to check if subscriber has master role
     */
    var isMaster: Boolean = false

    fun stop() {
        subscriptionsPool.forEach { (_, subscription) ->
            subscription.stop()
        }

        sendInformationScheduler.stop()
        checkMainNodeExistsScheduler.stop()
        mainSubscriber.stop()

        cancelConsumer(client)
        client.close()
    }

    fun start() {
        registerConsumer(client) { message ->
            when (message.message) {
                is UploadSubscriptionsRequest -> uploadSubscriptions(message.message)
                is RemoveSubscriptionRequest -> removeSubscriptions(message.message)
                else -> logger.error("Unrecognized message type has been received. Type: ${message::class.java.name}")
            }
        }

        sendInformationScheduler.start()
        checkMainNodeExistsScheduler.start()
    }

    /**
     * Logger object to print logs
     */
    private val logger = LoggingManager.getLogger(name)

    /**
     * Name of master node. This name is used to declare master queue in queues system
     */
    private val masterName = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("Needed property doesn't exists: ${ConfigNames.PRIMARY_NAME}"))

    /**
     * Time between two data transmissions to the head node
     */
    private val slaveSendInfoPeriod = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SEND_INFO_PERIOD, ConfigNames.SECONDARY_SEND_INFO_PERIOD_DEFAULT)

    /**
     * Maximum time when subscriber can take thread used to send information to head node. If this thread is running more than these milliseconds, task is cleared and another task is thrown
     */
    private val slaveSendInfoTimeout = ConfigManager.getLong(ConfigNames.SUBSCRIBER_SEND_INFO_TIMEOUT, ConfigNames.SECONDARY_SEND_INFO_TIMEOUT_DEFAULT)

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
    private val mainSubscriber = MainSubscriber()

    /**
     * This scheduler sends slave information continuously to master
     */
    private val sendInformationScheduler by lazy {
        object : PeriodicalScheduler(slaveSendInfoPeriod, 0, slaveSendInfoTimeout) {
            override fun onCycle() {
                client.send(masterName, SubscriberInfo(name,
                    subscriptionsPool.values.associate { subscription ->
                        Pair(
                            subscription.name,
                            subscription.info
                        )
                    }, name == masterName)
                )
            }
        }
    }

    /**
     * This scheduler checks continuously if master node is up. When main node is down, if [ConfigNames.ALLOCATION_STRATEGY] is not [AllocationStrategy.FIXED]
     * tries to take main node's role
     */
    private val checkMainNodeExistsScheduler by lazy {
        object : PeriodicalScheduler(checkMainNodeExistPeriod, 0, checkMainNodeExistsTimeout) {
            override fun onCycle() {
                if (!masterConsuming(client)) {
                    isMaster = true
                    mainSubscriber.start()
                }
            }
        }
    }

    private fun uploadSubscriptions(request: UploadSubscriptionsRequest) {
        try {
            val subscriptionInfos = request.subscriptions
                .map { subscription ->
                    JSONSerializer.deserializeWithClassName(subscription) as Subscription<*, *>
                }
                .filter { subscription -> !subscriptionsPool.containsKey(subscription.name)
                        || subscriptionsPool[subscription.name]?.info?.content == subscription.info.content }
                .onEach { subscription ->
                    subscriptionsPool[subscription.name]?.stop()
                    subscriptionsPool[subscription.name] = subscription
                    subscription.start()
                }.map { it.info }

            sendMessage(UploadSubscriptionResponse(ResponseStatus.OK, subscriptionInfos, request.id), client, masterName)
        } catch (e: Exception) {
            logger.error("Query ${request.id} fails. Unable to upload subscriptions", e)
            sendMessage(UploadSubscriptionResponse(ResponseStatus.ERROR, listOf(), request.id), client, masterName)
        }
    }

    private fun removeSubscriptions(request: RemoveSubscriptionRequest) {
        var responseStatus = ResponseStatus.OK

        try {
            request.subscriptions.forEach { subscriptionsName ->
                subscriptionsPool[subscriptionsName]?.stop()
                subscriptionsPool.remove(subscriptionsName)
            }

        } catch (e: Exception) {
            logger.error("Query ${request.id} fails. Unable to remove subscriptions", e)
            responseStatus = ResponseStatus.ERROR
        }

        sendMessage(RemoveSubscriptionResponse(responseStatus, request.id), client, masterName)
    }

    init {
        start()
    }
}