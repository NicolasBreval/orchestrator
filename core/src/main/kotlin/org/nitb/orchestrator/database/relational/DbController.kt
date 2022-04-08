package org.nitb.orchestrator.database.relational

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.database.relational.entities.Subscriptions
import org.nitb.orchestrator.logging.LoggingManager
import java.lang.RuntimeException

object DbController {

    // region PUBLIC METHODS

    /**
     * Obtains last subscriptions registered to a subscriber.
     * @param subscriber Name of subscriber to search subscriptions.
     */
    fun getLastActiveSubscriptionsBySubscriber(subscriber: String): List<SubscriptionEntry> {
        return transaction {
            Subscriptions
                .select { Subscriptions.id inSubQuery Subscriptions.slice(Subscriptions.id.max()).select { Subscriptions.subscriber eq subscriber }.groupBy(Subscriptions.name) }
                .map { resultRow ->
                    SubscriptionEntry(resultRow)
                }
        }
    }

    /**
     * Obtains all subscriptions with specified name.
     * @param subscriptionName Name of subscription to search.
     * @param onlyActive If is true, obtains only active subscriptions
     */
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

    /**
     * Inserts new subscriptions to database.
     * @param subscriptions List of subscriptions to update
     */
    fun insertSubscriptions(subscriptions: List<SubscriptionEntry>) {
        transaction {
            Subscriptions.batchInsert(subscriptions) { entry ->
                this[Subscriptions.name] = entry.name
                this[Subscriptions.content] = ExposedBlob(entry.content)
                this[Subscriptions.subscriber] = entry.subscriber
                this[Subscriptions.active] = entry.active
                this[Subscriptions.stopped] = entry.stopped
                this[Subscriptions.creationDate] = entry.creationDate
            }
        }
    }

    // endregion

    // region INTERNAL METHODS

    /**
     * Checks if tables are created or not. This method is used only in tests.
     */
    internal fun checkTablesAreCreated(): Boolean {
        return transaction {
            Subscriptions.exists()
        }
    }

    /**
     * Remove all subscriptions from table. This method is used only in tests.
     */
    internal fun clearSubscriptions() {
        transaction {
            Subscriptions.deleteAll()
        }
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Logger object used to show logs to developer.
     */
    val logger = LoggingManager.getLogger(this::class.java)

    // endregion

    // region INIT

    init {
        try {
            DbFactory.connect()

            if (ConfigManager.getBoolean(ConfigNames.DATABASE_CREATE_SCHEMAS_ON_STARTUP)) {
                transaction {
                    SchemaUtils.createMissingTablesAndColumns(Subscriptions)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Impossible to connect to database at first time, shutting down", e)
        }
    }

    // endregion
}