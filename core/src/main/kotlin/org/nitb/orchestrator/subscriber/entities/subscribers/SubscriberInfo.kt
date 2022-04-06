package org.nitb.orchestrator.subscriber.entities.subscribers

import com.sun.management.OperatingSystemMXBean
import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import java.io.Serializable
import java.lang.management.ManagementFactory
import java.net.InetAddress

@NoArgsConstructor
class SubscriberInfo(
    val name: String,
    val subscriptions: Map<String, SubscriptionInfo>,
    val isMaster: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val hostname: String = InetAddress.getLocalHost().hostName,
    val ipAddress: String = InetAddress.getLocalHost().hostAddress,
    val port: Int? = ConfigManager.getInt(ConfigNames.PORT_NUMBER),
    val totalMemory: Long = Runtime.getRuntime().totalMemory(),
    val freeMemory: Long = Runtime.getRuntime().freeMemory(),
    val cpuUsage: Double = (ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean).processCpuLoad
): Serializable