package org.nitb.orchestrator.config

import org.nitb.orchestrator.annotations.RequiredProperty
import org.nitb.orchestrator.subscriber.entities.AllocationStrategy

object ConfigNames {

    // region LOGGING

    const val LOGGING_FOLDER = "logging.folder"
    const val LOGGING_PATTERN = "logging.pattern"
    const val LOGGING_DATE_PATTERN = "logging.date.pattern"
    const val LOGGING_MAX_FILE_SIZE = "logging.max.file.size"
    const val LOGGING_FLUENTD_ENABLED = "logging.fluentd.enabled"

    // endregion

    // region LOGGING DEFAULTS

    const val LOGGING_FOLDER_DEFAULT = "./logs"
    const val LOGGING_PATTERN_DEFAULT = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    const val LOGGING_DATE_PATTERN_DEFAULT = "yyyy-MM-dd"
    const val LOGGING_MAX_FILE_SIZE_DEFAULT = "512MB"

    // endregion

    // region FLUENTD

    @RequiredProperty("Only if logging.fluentd.enabled is true", depends = true, dependency = "logging.fluentd.enabled", "true")
    const val LOGGING_FLUENTD_HOST = "logging.fluentd.host"
    const val LOGGING_FLUENTD_PORT = "logging.fluentd.port"
    @RequiredProperty("Only if logging.fluentd.enabled is true", depends = true, dependency = "logging.fluentd.enabled", "true")
    const val LOGGING_FLUENTD_TAG = "logging.fluentd.tag"
    @RequiredProperty("Only if logging.fluentd.enabled is true", depends = true, dependency = "logging.fluentd.enabled", "true")
    const val LOGGING_FLUENTD_TAG_PREFIX = "logging.fluentd.tag.prefix"

    // endregion

    // region FLUENTD DEFAULTS

    const val LOGGING_FLUENTD_DEFAULT_PORT = 24224

    // endregion

    // region DATABASE

    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_DRIVER_CLASSNAME = "database.driver.classname"
    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_JDBC_URL = "database.jdbc.url"
    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_USERNAME = "database.username"
    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_PASSWORD = "database.password"
    const val DATABASE_MAX_POOL_SIZE = "database.max.pool.size"
    const val DATABASE_MAX_LIFE_TIME = "database.max.life.time"

    // endregion

    // region DATABASE DEFAULTS

    const val DATABASE_MAX_POOL_SIZE_DEFAULT = 5
    const val DATABASE_MAX_LIFE_TIME_DEFAULT = 1800000L

    // endregion

    // region CLOUD

    @RequiredProperty("Needed to select queue connection type")
    const val CLOUD_TYPE = "cloud.type"

    // region RABBITMQ

    @RequiredProperty("Only if cloud.type is RABBITMQ", depends = true, dependency = "cloud.type", dependencyValue = "RABBITMQ")
    const val RABBITMQ_HOST = "cloud.rabbitmq.host"
    const val RABBITMQ_PORT = "cloud.rabbitmq.port"
    @RequiredProperty("Only if cloud.type is RABBITMQ", depends = true, dependency = "cloud.type", dependencyValue = "RABBITMQ")
    const val RABBITMQ_USERNAME = "cloud.rabbitmq.username"
    @RequiredProperty("Only if cloud.type is RABBITMQ", depends = true, dependency = "cloud.type", dependencyValue = "RABBITMQ")
    const val RABBITMQ_PASSWORD = "cloud.rabbitmq.password"

    // endregion

    // region ACTIVEMQ

    @RequiredProperty("Only if cloud.type is ACTIVEMQ", depends = true, dependency = "cloud.type", dependencyValue = "ACTIVEMQ")
    const val ACTIVEMQ_BROKER_URL = "cloud.activemq.broker.url"
    @RequiredProperty("Only if cloud.type is ACTIVEMQ", depends = true, dependency = "cloud.type", dependencyValue = "ACTIVEMQ")
    const val ACTIVEMQ_USERNAME = "cloud.activemq.username"
    @RequiredProperty("Only if cloud.type is ACTIVEMQ", depends = true, dependency = "cloud.type", dependencyValue = "ACTIVEMQ")
    const val ACTIVEMQ_PASSWORD = "cloud.activemq.password"

    // endregion

    // endregion

    // region CLOUD DEFAULTS

    // region RABBITMQ DEFAULTS

    const val RABBITMQ_DEFAULT_PORT = 5672
    const val RABBITMQ_DEFAULT_USERNAME = "guest"
    const val RABBITMQ_DEFAULT_PASSWORD = "guest"

    // endregion

    // region ACTIVEMQ DEFAULTS

    const val ACTIVEMQ_DEFAULT_USERNAME = "admin"
    const val ACTIVEMQ_DEFAULT_PASSWORD = "admin"

    // endregion

    // endregion

    // region NETWORK

    const val PORT_NUMBER = "server.port.number"

    // endregion

    // region SECONDARY SUBSCRIBER

    @RequiredProperty("Needed only if allocation strategy is FIXED", depends = true, dependency = "subscriber.master.allocation.strategy", dependencyValue = "FIXED")
    const val SECONDARY_NAME = "subscriber.slave.name"
    const val SECONDARY_SEND_INFO_PERIOD = "subscriber.slave.send.info.period"
    const val SECONDARY_SEND_INFO_TIMEOUT = "subscriber.slave.send.ingo.timeout"

    // endregion

    // region PRIMARY SUBSCRIBER

    @RequiredProperty("All nodes need to know the name of the primary node in order to know to which queue to send availability notification messages and to consume the queue in case they get the primary role")
    const val PRIMARY_NAME = "subscriber.master.name"
    const val ALLOCATION_STRATEGY = "subscriber.master.allocation.strategy"

    // endregion

    // region SECONDARY SUBSCRIBER DEFAULTS

    const val SECONDARY_SEND_INFO_PERIOD_DEFAULT = 5000L
    const val SECONDARY_SEND_INFO_TIMEOUT_DEFAULT = 4000L

    // endregion

    // region PRIMARY SUBSCRIBER DEFAULTS

    val ALLOCATION_STRATEGY_DEFAULT = AllocationStrategy.FIXED

    // endregion
}