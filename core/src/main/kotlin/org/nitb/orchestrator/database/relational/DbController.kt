package org.nitb.orchestrator.database.relational

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.annotations.TableToCreate
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.database.relational.entities.SubscriptionSerializableEntry
import org.nitb.orchestrator.database.relational.entities.Subscriptions
import org.nitb.orchestrator.logging.LoggingManager
import org.reflections.Reflections
import kotlin.system.exitProcess

/**
 * Class used to configure and operate with database.
 */
object DbController {

    // region PUBLIC METHODS

    /**
     * Obtains last subscriptions registered to a subscriber.
     *
     * @param subscriber Name of subscriber to search subscriptions.
     *
     * @return List of subscriptions related to a subscriber.
     */
    fun getLastActiveSubscriptionsBySubscriber(subscriber: String): List<SubscriptionEntry> {
        return transaction {
            SubscriptionEntry.find { Subscriptions.id inSubQuery Subscriptions.slice(Subscriptions.id.max()).select { Subscriptions.subscriber eq subscriber }.groupBy(Subscriptions.name) }.toList()
        }
    }

    /**
     * Obtains all active subscriptions registered on database.
     */
    fun getLastSubscriptions(): List<SubscriptionEntry> {
        return transaction {
            val maxId = Subscriptions.id.max().alias("maxId")
            val lastSubscriptions =
                Subscriptions
                    .slice(Subscriptions.name, maxId)
                    .selectAll()
                    .groupBy(Subscriptions.name)
                    .alias("maxIdBySub")

            Join(Subscriptions).join(lastSubscriptions, JoinType.INNER, lastSubscriptions[maxId], Subscriptions.id)
            .select { Subscriptions.active eq true }.map { resultRow -> SubscriptionEntry.wrapRow(resultRow) }
        }
    }

    /**
     * Inserts a list of subscriptions concurrently inside an ExecutorService.
     *
     * @param subscriptions List of subscriptions to upload.
     * @param subscriber Subscriber where subscriptions are uploaded.
     * @param stopped Indicates if subscriptions are stopped.
     * @param active Indicates if subscriptions are removed or not.
     */
    fun uploadSubscriptionsConcurrently(subscriptions: Map<String, String>, subscriber: String, stopped: Map<String, Boolean> = mapOf()) {
        Thread {
            val lastSubscriptions = getLastActiveSubscriptionsBySubscriber(subscriber).associateBy { it.name }

            transaction {
                subscriptions.forEach { (name, content) ->
                    SubscriptionEntry.new {
                        this.name = name
                        this.content = ExposedBlob(content.toByteArray())
                        this.subscriber = subscriber
                        this.stopped = stopped[name] ?: (lastSubscriptions[name]?.stopped ?: false)
                        this.active = lastSubscriptions[name]?.active ?: true
                    }
                }
            }
        }.start()
    }

    /**
     * Updates all subscriptions passed as parameter
     *
     * @param subscriptions List of subscription names to update.
     * @param stopped Indicates if subscriptions are stopped.
     * @param active Indicates if subscriptions are removed.
     */
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

    /**
     * Returns all changes of a subscription from database.
     *
     * @param name Name of subscription to retrieve their historical.
     *
     * @return List of all subscription contents registered on database.
     */
    fun getSubscriptionHistorical(name: String): List<SubscriptionSerializableEntry> {
        val historical = mutableListOf<SubscriptionSerializableEntry>()

        transaction {
            historical.addAll(SubscriptionEntry.find { Subscriptions.name eq name }.map { SubscriptionSerializableEntry(it) }.toList())
        }

        return historical
    }

    // endregion

    // region INTERNAL METHODS

    /**
     * Initializes database connection pool.
     */
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
                                    logger.error("Error with database table ${it.name}", e)
                                }
                            }

                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Impossible to connect to database at first time, shutting down", e)
            exitProcess(1)
        }
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Logger object to show message on application log.
     */
    private val logger = LoggingManager.getLogger("db.controller")

    // endregion

    // region INIT

    init {
        initialize()
    }

    // endregion
}