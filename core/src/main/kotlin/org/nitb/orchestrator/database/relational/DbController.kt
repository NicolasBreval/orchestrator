package org.nitb.orchestrator.database.relational

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.database.relational.entities.Subscriptions
import org.nitb.orchestrator.database.relational.entities.operations.OperationType
import org.nitb.orchestrator.database.relational.entities.operations.SubscriptionDatabaseOperation
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.scheduling.PeriodicalScheduler
import java.lang.RuntimeException
import java.util.concurrent.LinkedBlockingDeque

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
     * @param subscriptions List of subscriptions to update.
     */
    fun insertSubscriptions(subscriptions: List<SubscriptionEntry>) {
        transaction {
            Subscriptions.batchInsert(subscriptions) { entry -> entry.onBatchInsert(this) }
        }
    }

    /**
     * Adds set of operations in queue to be processed later.
     */
    fun addOperationsToWaitingList(subscriber: String, subscriptionOperations: List<SubscriptionDatabaseOperation>) {
        enqueuedOperations.add(Pair(subscriber, subscriptionOperations))
    }

    /**
     * Obtains all active subscriptions registered on database.
     */
    fun getLastActiveSubscriptions(): List<SubscriptionEntry> {
        return transaction {
            val maxId = Subscriptions.id.max().alias("maxId")
            val lastSubscriptions =
                Subscriptions
                    .slice(Subscriptions.name, maxId)
                    .select { Subscriptions.active eq true }
                    .groupBy(Subscriptions.name)
                    .alias("maxIdBySub")

            Join(Subscriptions).join(lastSubscriptions, JoinType.INNER, lastSubscriptions[maxId], Subscriptions.id)
            .selectAll().map { resultRow -> SubscriptionEntry(resultRow) }
        }
    }

    /**
     * Check if database is accessible
     */
    fun checkConnection() {

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
     * Operations waiting to be processed
     */
    private val enqueuedOperations = LinkedBlockingDeque<Pair<String, List<SubscriptionDatabaseOperation>>>()

    /**
     * Scheduler object used to obtain a new update operation from [enqueuedOperations] and process it.
     */
    private val updater by lazy { object : PeriodicalScheduler(100) {
        override fun onCycle() {
            if (enqueuedOperations.size > 0) {
                val last = enqueuedOperations.poll()
                updateSubscriptions(last.first, last.second)
            }
        }
    } }

    // endregion

    // region PRIVATE METHODS

    /**
     * Updates all subscriptions in [subscriptionOperations] for [subscriber] on database. To keep historical for each subscription,
     * when you change it, new register is inserted in table, no register is deleted.
     * @param subscriber Name of subscriber related to subscriptions to update.
     * @param subscriptionOperations Each operation to process in database.
     */
    @Synchronized
    private fun updateSubscriptions(subscriber: String, subscriptionOperations: List<SubscriptionDatabaseOperation>) {
        transaction {
            val maxId = Subscriptions.id.max().alias("maxId")
            val lastSubscriptions =
                Subscriptions
                    .slice(Subscriptions.name, maxId)
                    .select { Subscriptions.subscriber eq subscriber }
                    .groupBy(Subscriptions.name)
                    .alias("maxIdBySub")

            val joined = Join(Subscriptions).join(lastSubscriptions, JoinType.INNER, lastSubscriptions[maxId], Subscriptions.id)
                .selectAll().map { resultRow -> SubscriptionEntry(resultRow) }.toMutableList()

            val operations = subscriptionOperations.groupBy { it.name }

            val changedSubscriptions = mutableListOf<String>()

            for (subscriptionEntry in joined) {
                for (operation in operations[subscriptionEntry.name] ?: listOf()) {
                    var changed = false

                    when (operation.operationType) {
                        OperationType.UPDATE_CONTENT -> {
                            if (!operation.content?.toByteArray().contentEquals(subscriptionEntry.content)) {
                                subscriptionEntry.content = operation.content?.toByteArray() ?: subscriptionEntry.content
                                changed = true
                            }
                        }
                        OperationType.REMOVE -> {
                            if (subscriptionEntry.active) {
                                subscriptionEntry.active = false
                                changed = true
                            }

                        }
                        OperationType.ADD -> {
                            if (!subscriptionEntry.active) {
                                subscriptionEntry.active = true
                                changed = true
                            }
                        }
                        OperationType.START -> {
                            if (subscriptionEntry.stopped) {
                                subscriptionEntry.stopped = false
                                changed = true
                            }
                        }
                        OperationType.STOP -> {
                            if (!subscriptionEntry.stopped) {
                                subscriptionEntry.stopped = true
                                changed = true
                            }
                        }
                    }

                    if (changed) {
                       changedSubscriptions.add(subscriptionEntry.name)
                    }
                }
            }

            operations.flatMap { it.value }.filter { operation -> operation.operationType == OperationType.ADD && joined.none { it.name == operation.name } }.forEach { operation ->
                joined.add(SubscriptionEntry(operation.name, operation.content?.toByteArray() ?: ByteArray(0), subscriber, operation.stopped, true))
                changedSubscriptions.add(operation.name)
            }

            if (joined.isNotEmpty()) {
                Subscriptions.batchInsert(joined.filter { changedSubscriptions.contains(it.name) }) { entry -> entry.onBatchInsert(this) }
            }
        }
    }

    // endregion

    // region INIT

    init {
        try {
            DbFactory.connect()

            updater.start()

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