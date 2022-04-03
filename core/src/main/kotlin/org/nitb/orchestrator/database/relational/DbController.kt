package org.nitb.orchestrator.database.relational

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.database.relational.entities.Subscriptions
import java.lang.RuntimeException

object DbController {

    // region PUBLIC METHODS

    fun getActiveSubscriptionsBySlave(slaveName: String): List<SubscriptionEntry> {
        return transaction {
            Subscriptions.select { Subscriptions.slave eq slaveName and Subscriptions.active }.map { resultRow ->
                SubscriptionEntry(resultRow)
            }
        }
    }

    fun getSubscriptionsByName(subscriptionName: String, onlyActive: Boolean = false): List<SubscriptionEntry> {
        return transaction {
            val filters: Op<Boolean> = when {
                onlyActive -> Op.build { Subscriptions.name eq subscriptionName and Subscriptions.active }
                else -> Op.build { Subscriptions.name eq subscriptionName }
            }

            Subscriptions.select(filters).map { resultRow ->
                SubscriptionEntry(resultRow)
            }
        }
    }

    fun insertSubscriptions(subscriptions: List<SubscriptionEntry>) {
        transaction {
            Subscriptions.batchInsert(subscriptions) { entry ->
                this[Subscriptions.name] = entry.name
                this[Subscriptions.content] = ExposedBlob(entry.content)
                this[Subscriptions.slave] = entry.slave
                this[Subscriptions.active] = entry.active
                this[Subscriptions.stopped] = entry.stopped
                this[Subscriptions.creationDate] = entry.creationDate
            }
        }
    }

    // endregion

    init {
        try {
            DbFactory.connect()

            if (ConfigManager.getBoolean(ConfigNames.DATABASE_CREATE_SCHEMAS_ON_STARTUP)) {
                transaction {
                    SchemaUtils.create(Subscriptions)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Impossible to connect to database at first time, shutting down", e)
        }
    }

}