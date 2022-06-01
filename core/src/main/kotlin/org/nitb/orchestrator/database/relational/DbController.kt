package org.nitb.orchestrator.database.relational

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.annotations.TableToCreate
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.database.relational.entities.Subscriptions
import org.nitb.orchestrator.logging.LoggingManager
import org.reflections.Reflections
import java.lang.RuntimeException

object DbController {

    // region PUBLIC METHODS

    /**
     * Obtains last subscriptions registered to a subscriber.
     * @param subscriber Name of subscriber to search subscriptions.
     */
    fun getLastActiveSubscriptionsBySubscriber(subscriber: String): List<SubscriptionEntry> {
        return transaction {
            SubscriptionEntry.find { Subscriptions.id inSubQuery Subscriptions.slice(Subscriptions.id.max()).select { Subscriptions.subscriber eq subscriber }.groupBy(Subscriptions.name) }.toList()
        }
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
            .selectAll().map { resultRow -> SubscriptionEntry.wrapRow(resultRow) }
        }
    }

    /**
     * Inserts a list of subscriptions concurrently inside an ExecutorService
     */
    fun uploadSubscriptionsConcurrently(subscriptions: Map<String, String>, subscriber: String, stopped: Boolean? = null, active: Boolean? = null) {
        Thread {
            val lastSubscriptions = getLastActiveSubscriptionsBySubscriber(subscriber).associateBy { it.name }

            transaction {
                subscriptions.forEach { (name, content) ->
                    SubscriptionEntry.new {
                        this.name = name
                        this.content = ExposedBlob(content.toByteArray())
                        this.subscriber = subscriber
                        this.stopped = stopped ?: (lastSubscriptions[name]?.stopped ?: false)
                        this.active = active ?: (lastSubscriptions[name]?.active ?: true)
                    }
                }
            }
        }.start()
    }

    fun setSubscriptionsConcurrentlyByName(subscriptions: List<String>, stopped: Boolean? = null, active: Boolean? = null) {
        Thread {
            transaction {
                SubscriptionEntry.find { Subscriptions.id inSubQuery Subscriptions.slice(Subscriptions.id.max())
                    .select { Subscriptions.name inList subscriptions }
                    .groupBy(Subscriptions.name) }.forEach { entry ->
                    SubscriptionEntry.new {
                        this.name = entry.name
                        this.content = entry.content
                        this.subscriber = entry.subscriber
                        this.stopped = stopped ?: entry.stopped
                        this.active = active ?: entry.active
                    }
                }
            }
        }.start()
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

    internal fun initialize() {
        try {
            DbFactory.connect()

            if (ConfigManager.getBoolean(ConfigNames.DATABASE_CREATE_SCHEMAS_ON_STARTUP)) {

                transaction {
                    val packages = listOf("org.nitb.orchestrator.database.relational.entities",
                        *ConfigManager.getProperties(ConfigNames.DATABASE_CREATE_ON_STARTUP_SCHEMAS_PACKAGES).toTypedArray())


                    for (packageName in packages) {
                        Reflections(packageName).getTypesAnnotatedWith(TableToCreate::class.java).forEach {
                            if (Table::class.java.isAssignableFrom(it)) {
                                try {
                                    SchemaUtils.createMissingTablesAndColumns(it.kotlin.objectInstance as Table)
                                } catch (e: Exception) {
                                    logger.error("Error with database table ${it.name}")
                                }
                            }

                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Impossible to connect to database at first time, shutting down", e)
        }
    }

    // endregion

    // region PRIVATE PROPERTIES

    private val logger = LoggingManager.getLogger("db.controller")

    // endregion

    // region INIT

    init {
        initialize()
    }

    // endregion
}