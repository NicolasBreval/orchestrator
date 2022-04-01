package org.nitb.orchestrator.subscriber.entities

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.subscription.Subscription
import java.io.Serializable
import java.net.InetAddress

@NoArgsConstructor
class SubscriptionInfo(
    val name: String,
    val subscriptions: List<Subscription<*, *>>,
    val isMaster: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val hostname: String = InetAddress.getLocalHost().hostName,
    val ipAddress: String = InetAddress.getLocalHost().hostAddress,
    val port: Int = ConfigManager.getInt(ConfigNames.PORT_NUMBER, ConfigNames.PORT_NUMBER_DEFAULT)
): Serializable