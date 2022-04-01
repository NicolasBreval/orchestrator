package org.nitb.orchestrator.database.relational.entities

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Subscriptions: Table("SUBSCRIPTIONS") {
    val id = long("ID").autoIncrement()
    val name = varchar("NAME", 300)
    val content = blob("CONTENT")
    val slave = varchar("SLAVE", 300)
    val stopped = bool("STOPPED")
    val creationDate = datetime("CREATION_DATE")
    val active = bool("ACTIVE")
    override val primaryKey = PrimaryKey(id, name = "PK_SUBSCRIPTIONS_PK")
}