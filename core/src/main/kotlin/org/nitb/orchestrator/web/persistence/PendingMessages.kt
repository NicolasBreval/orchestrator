package org.nitb.orchestrator.web.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object PendingMessages: Table() {
    val id = varchar("ID", 300).entityId()
    val status = varchar("STATUS", 10).nullable()
    val creation = datetime("CREATION")
}