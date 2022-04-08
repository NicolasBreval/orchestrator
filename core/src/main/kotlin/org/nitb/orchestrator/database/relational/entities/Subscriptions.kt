package org.nitb.orchestrator.database.relational.entities

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * Class related to SUBSCRIPTION database table to operate using DSL.
 */
class SubscriptionEntry(
    val name: String,
    val content: ByteArray,
    var subscriber: String,
    val stopped: Boolean,
    val active: Boolean,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val id: Long? = null
) {
    constructor(resultRow: ResultRow): this(
        resultRow[Subscriptions.name],
        resultRow[Subscriptions.content].bytes,
        resultRow[Subscriptions.subscriber],
        resultRow[Subscriptions.stopped],
        resultRow[Subscriptions.active],
        resultRow[Subscriptions.creationDate],
        resultRow[Subscriptions.id]
    )
}

/**
 * Table object to specify ORM table schema.
 */
object Subscriptions: Table("SUBSCRIPTIONS") {
    val id = long("ID").autoIncrement()
    val name = varchar("NAME", 300)
    val content = blob("CONTENT")
    val subscriber = varchar("SUBSCRIBER", 300)
    val stopped = bool("STOPPED")
    val creationDate = datetime("CREATION_DATE")
    val active = bool("ACTIVE")
    override val primaryKey = PrimaryKey(id, name = "PK_SUBSCRIPTIONS_PK")
}