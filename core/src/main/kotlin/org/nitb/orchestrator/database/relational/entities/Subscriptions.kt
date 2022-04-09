package org.nitb.orchestrator.database.relational.entities

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.LocalDateTime

/**
 * Class related to SUBSCRIPTION database table to operate using DSL.
 */
class SubscriptionEntry(
    val name: String,
    var content: ByteArray,
    var subscriber: String,
    var stopped: Boolean,
    var active: Boolean,
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

    fun onBatchInsert(batchInsertStatement: BatchInsertStatement) {
        batchInsertStatement[Subscriptions.name] = name
        batchInsertStatement[Subscriptions.content] = ExposedBlob(content)
        batchInsertStatement[Subscriptions.subscriber] = subscriber
        batchInsertStatement[Subscriptions.active] = active
        batchInsertStatement[Subscriptions.stopped] = stopped
        batchInsertStatement[Subscriptions.creationDate] = creationDate
    }
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