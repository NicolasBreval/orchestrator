package org.nitb.orchestrator.database.relational

import ch.qos.logback.classic.Level
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import java.lang.RuntimeException
import java.sql.Connection

/**
 * Factory object used to create database connections.
 */
object DbFactory {

    // region PUBLIC METHODS

    /**
     * Method used to connect
     */
    fun connect() {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        while (!firstConnection) {
            try {
                Database.connect(createHikariDataSource())
                firstConnection = true
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
            Thread.sleep(1000)
        }
    }

    // endregion

    // region PRIVATE PROPERTIES

    private val driverClassname = ConfigManager.getProperty(ConfigNames.DATABASE_DRIVER_CLASSNAME, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_DRIVER_CLASSNAME}"))
    private val jdbcUrl = ConfigManager.getProperty(ConfigNames.DATABASE_JDBC_URL, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_JDBC_URL}"))
    private val username = ConfigManager.getProperty(ConfigNames.DATABASE_USERNAME, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_USERNAME}"))
    private val password = ConfigManager.getProperty(ConfigNames.DATABASE_PASSWORD, RuntimeException("No mandatory property found: ${ConfigNames.DATABASE_PASSWORD}"))
    private val maxPoolSize = ConfigManager.getInt(ConfigNames.DATABASE_MAX_POOL_SIZE, ConfigNames.DATABASE_MAX_POOL_SIZE_DEFAULT)
    private val maxLifeTime = ConfigManager.getLong(ConfigNames.DATABASE_MAX_LIFE_TIME, ConfigNames.DATABASE_MAX_LIFE_TIME_DEFAULT)
    private var firstConnection = false

    // endregion

    // region PRIVATE METHODS

    private fun createHikariDataSource(): HikariDataSource {
        if (ConfigManager.getBoolean(ConfigNames.DATABASE_SHOW_LOGS)) {
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.pool.PoolBase")
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.pool.HikariPool")
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.HikariDataSource")
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.HikariConfig")
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.util.DriverDataSource")
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.pool.ProxyConnection")
        } else {
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.pool.PoolBase", Level.OFF)
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.pool.HikariPool", Level.OFF)
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.HikariDataSource", Level.OFF)
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.HikariConfig", Level.OFF)
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.util.DriverDataSource", Level.OFF)
            LoggingManager.setLoggerLevel("com.zaxxer.hikari.pool.ProxyConnection", Level.OFF)
        }

        if (!ConfigManager.getBoolean(ConfigNames.DATABASE_SHOW_SQL_QUERIES)) {
            LoggingManager.setLoggerLevel("Exposed", Level.OFF)
        }

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