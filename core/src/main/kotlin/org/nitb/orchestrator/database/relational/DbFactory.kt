package org.nitb.orchestrator.database.relational

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames

object DbFactory {

    // region PRIVATE PROPERTIES

    private val driverClassname = ConfigManager.getProperty(ConfigNames.DATABASE_DRIVER_CLASSNAME, DbFactoryPropertyNotFoundException("No mandatory property found: ${ConfigNames.DATABASE_DRIVER_CLASSNAME}"))
    private val jdbcUrl = ConfigManager.getProperty(ConfigNames.DATABASE_JDBC_URL, DbFactoryPropertyNotFoundException("No mandatory property found: ${ConfigNames.DATABASE_JDBC_URL}"))
    private val username = ConfigManager.getProperty(ConfigNames.DATABASE_USERNAME, DbFactoryPropertyNotFoundException("No mandatory property found: ${ConfigNames.DATABASE_USERNAME}"))
    private val password = ConfigManager.getProperty(ConfigNames.DATABASE_PASSWORD, DbFactoryPropertyNotFoundException("No mandatory property found: ${ConfigNames.DATABASE_PASSWORD}"))
    private val maxPoolSize = ConfigManager.getInt(ConfigNames.DATABASE_MAX_POOL_SIZE, ConfigNames.DATABASE_MAX_POOL_SIZE_DEFAULT)
    private val maxLifeTime = ConfigManager.getLong(ConfigNames.DATABASE_MAX_LIFE_TIME, ConfigNames.DATABASE_MAX_LIFE_TIME_DEFAULT)
    private val database = Database.connect(createHikariDataSource())

    // endregion

    // region PRIVATE METHODS

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

    // region INIT

    init {
        Database.connect(createHikariDataSource())
    }

    // endregion
}