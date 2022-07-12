package org.nitb.orchestrator.subscriber.entities.subscribers

import com.sun.management.OperatingSystemMXBean
import io.swagger.v3.oas.annotations.media.Schema
import org.nitb.orchestrator.annotations.NoArgsConstructor
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import java.io.Serializable
import java.lang.management.ManagementFactory
import java.net.InetAddress

@Schema(description = "Information about a subscriber registered in cluster.")
@NoArgsConstructor
class SubscriberInfo(
    @Schema(description = "Unique name of subscriber.")
    val name: String,
    @Schema(description = "List of subscriptions registered in this subscriber.")
    val subscriptions: Map<String, SubscriptionInfo> = mapOf(),
    @Schema(description = "If is true, this information is related to main subscriber")
    val isMainNode: Boolean = false,
    @Schema(description = "Timestamp when this information was created.")
    val timestamp: Long = System.currentTimeMillis(),
    @Schema(description = "Hostname of this subscriber. It's used to make easy HTTP communication between subscribers.")
    val hostname: String = InetAddress.getLocalHost().hostName,
    @Schema(description = "IP address of this subscriber. It's used to make easy HTTP communication between subscribers.")
    val ipAddress: String = InetAddress.getLocalHost().hostAddress,
    @Schema(description = "HTTP configured in this subscription. It's used to make easy HTTP communication between subscribers.")
    val httpPort: Int? = ConfigManager.getInt(ConfigNames.HTTP_PORT),
    @Schema(description = "Total memory available in JVM for this subscriber. This information allows main subscriber to decide where send a new subscription if allocation strategy type uses memory in their ranking.")
    val totalMemory: Long = Runtime.getRuntime().totalMemory(),
    @Schema(description = "Free memory available in JVM for this subscriber. This information allows main subscriber to decide where send a new subscription if allocation strategy type uses memory in their ranking.")
    val freeMemory: Long = Runtime.getRuntime().freeMemory(),
    @Schema(description = "CPU usage at moment by this subscriber. This information allows main subscriber to decide where send a new subscription if allocation strategy type uses CPU in their ranking.")
    val cpuUsage: Double = (ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean).processCpuLoad
): Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubscriberInfo

        if (name != other.name) return false
        if (isMainNode != other.isMainNode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isMainNode.hashCode()
        return result
    }

    val fixedHost: String get() {
        return if (ConfigManager.getBoolean(ConfigNames.SUBSCRIBER_COMMUNICATION_USE_HOSTNAME)) {
            hostname
        } else {
            ipAddress
        }
    }
}