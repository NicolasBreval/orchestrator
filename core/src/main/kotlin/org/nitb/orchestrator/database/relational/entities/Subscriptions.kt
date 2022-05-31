package org.nitb.orchestrator.database.relational.entities

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.nitb.orchestrator.database.relational.annotations.TableToCreate
import java.time.Instant
import java.time.LocalDateTime

/**
 * Table object to specify ORM table schema.
 */
@TableToCreate
object Subscriptions: LongIdTable("SUBSCRIPTIONS") {
    val name = varchar("NAME", 300)
    val content = blob("CONTENT")
    val subscriber = varchar("SUBSCRIBER", 300)
    val stopped = bool("STOPPED")
    val creationDate = datetime("CREATION_DATE").default(LocalDateTime.now())
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