package org.nitb.orchestrator.database.relational.entities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.annotations.TableToCreate
import java.time.Instant

/**
 * Table object to specify ORM table schema.
 */
@TableToCreate
object Subscriptions: LongIdTable("${ConfigManager.getProperty(ConfigNames.DATABASE_ORCHESTRATOR_SCHEMA)?.let { "$it." } ?: ""}SUBSCRIPTIONS") {
    val name = varchar("NAME", 300)
    val content = blob("CONTENT")
    val subscriber = varchar("SUBSCRIBER", 300)
    val stopped = bool("STOPPED")
    val creationDate = timestamp("CREATION_DATE").clientDefault { Instant.now() }
    val active = bool("ACTIVE")
}

/**
 * Class related to SUBSCRIPTION database table to operate using DSL.
 */
class SubscriptionEntry(id: EntityID<Long>): Entity<Long>(id) {
    companion object: EntityClass<Long, SubscriptionEntry>(Subscriptions)

    var name by Subscriptions.name
    var content by Subscriptions.content
    var subscriber by Subscriptions.subscriber
    var stopped by Subscriptions.stopped
    var creationDate by Subscriptions.creationDate
    var active by Subscriptions.active
}

/**
 * Serializable version of [SubscriptionEntry] class, used to return on web controllers. This class it's needed because
 * original types of table class are Column<T>, and in serializable entities, application must return JSON-compatible types.
 */
data class SubscriptionSerializableEntry(
    val id: Long,
    val name: String,
    val content: ByteArray,
    val subscriber: String,
    val stopped: Boolean,
    val creationDate: Instant,
    val active: Boolean
) {
    constructor(subscriptionEntry: SubscriptionEntry): this(subscriptionEntry.id.value, subscriptionEntry.name,
        subscriptionEntry.content.bytes, subscriptionEntry.subscriber, subscriptionEntry.stopped,
        subscriptionEntry.creationDate, subscriptionEntry.active)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubscriptionSerializableEntry) return false

        if (name != other.name) return false
        if (!content.contentEquals(other.content)) return false
        if (subscriber != other.subscriber) return false
        if (stopped != other.stopped) return false
        if (creationDate != other.creationDate) return false
        if (active != other.active) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + subscriber.hashCode()
        result = 31 * result + stopped.hashCode()
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + active.hashCode()
        return result
    }
}