package org.nitb.orchestrator.database.relational

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import java.lang.RuntimeException
import java.sql.Connection

/**
 * Factory object used to create database connections.
 */
object DbFactory {

    // region PUBLIC METHODS

    /**
     * Method used to connect to configured database.
     */
    fun connect() {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        Database.connect(createHikariDataSource())
    }

    // endregion

    // region PRIVATE PROPERTIES

    /**
     * Name of driver used to connect with database.
     */
    private val driverClassname = ConfigManager.getProperty(ConfigNames.DATABASE_DRIVER_CLASSNAME, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_DRIVER_CLASSNAME}"))

    /**
     * Url of database server to connect.
     */
    private val jdbcUrl = ConfigManager.getProperty(ConfigNames.DATABASE_JDBC_URL, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_JDBC_URL}"))

    /**
     * Username used in database connection.
     */
    private val username = ConfigManager.getProperty(ConfigNames.DATABASE_USERNAME, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_USERNAME}"))

    /**
     * Password used in database connection.
     */
    private val password = ConfigManager.getProperty(ConfigNames.DATABASE_PASSWORD, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_PASSWORD}"))

    /**
     * Maximum size of connection pool.
     */
    private val maxPoolSize = ConfigManager.getInt(ConfigNames.DATABASE_MAX_POOL_SIZE, ConfigNames.DATABASE_MAX_POOL_SIZE_DEFAULT)

    /**
     * Maximum connection life time.
     */
    private val maxLifeTime = ConfigManager.getLong(ConfigNames.DATABASE_MAX_LIFE_TIME, ConfigNames.DATABASE_MAX_LIFE_TIME_DEFAULT)

    // endregion

    // region PRIVATE METHODS

    /**
     * Creates a new [HikariDataSource].
     *
     * @return [HikariDataSource] created from properties.
     */
    private fun createHikariDataSource(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = driverClassname
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        config.maximumPoolSize = maxPoolSize
        config.maxLifetime = maxLifeTime
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    // endregion
}