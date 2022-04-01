package org.nitb.orchestrator.subscriber

import org.nitb.orchestrator.cloud.CloudManager
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import org.nitb.orchestrator.subscriber.entities.SubscriptionInfo
import org.nitb.orchestrator.subscription.Subscription
import java.io.Serializable
import java.lang.RuntimeException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class SecondarySubscriber(
    private val name: String
): CloudManager<Serializable> {

    fun addSubscriptions(subscriptions: List<Subscription<*, *>>) {
        subscriptions.forEach { subscription ->
            subscriptionsPool[subscription.name] = subscription
        }
    }

    fun removeSubscription(subscriptions: List<Subscription<*, *>>) {
        subscriptions.forEach { subscription ->
            subscriptionsPool.remove(subscription.name)
        }
    }

    private val masterName = ConfigManager.getProperty(ConfigNames.PRIMARY_NAME, RuntimeException("Needed property doesn't exists: ${ConfigNames.PRIMARY_NAME}"))
    private val slaveSendInfoPeriod = ConfigManager.getLong(ConfigNames.SECONDARY_SEND_INFO_PERIOD, ConfigNames.SECONDARY_SEND_INFO_PERIOD_DEFAULT)
    private val slaveSendInfoTimeout = ConfigManager.getLong(ConfigNames.SECONDARY_SEND_INFO_TIMEOUT, ConfigNames.SECONDARY_SEND_INFO_TIMEOUT_DEFAULT)

    /**
     * Contains all active subscriptions in this slave
     */
    private val subscriptionsPool: ConcurrentMap<String, Subscription<*, *>> = ConcurrentHashMap()

    /**
     * Client used for communication with another subscribers
     */
    private val client = createClient(name)

    /**
     * This scheduler sends slave information continuously to master
     */
    private val sendInformationScheduler = object : PeriodicalScheduler(slaveSendInfoPeriod, 0, slaveSendInfoTimeout) {
        override fun onCycle() {
            client.send(masterName, SubscriptionInfo(name, subscriptionsPool.values.toList(), name == masterName))
        }
    }

    init {
        sendInformationScheduler.start()

        Runtime.getRuntime().addShutdownHook(Thread() {
            sendInformationScheduler.stop()
        })
    }

}