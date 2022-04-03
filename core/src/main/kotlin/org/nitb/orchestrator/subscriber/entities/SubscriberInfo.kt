package org.nitb.orchestrator.subscriber.entities

import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import java.io.Serializable
import java.net.InetAddress

@NoArgsConstructor
class SubscriberInfo(
    val name: String,
    val subscriptions: Map<String, String>,
    val isMaster: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val hostname: String = InetAddress.getLocalHost().hostName,
    val ipAddress: String = InetAddress.getLocalHost().hostAddress,
    val port: Int? = ConfigManager.getInt(ConfigNames.PORT_NUMBER)
): Serializable