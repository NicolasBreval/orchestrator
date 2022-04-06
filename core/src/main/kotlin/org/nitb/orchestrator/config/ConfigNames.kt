package org.nitb.orchestrator.config

import org.nitb.orchestrator.annotations.RequiredProperty
import org.nitb.orchestrator.subscriber.entities.AllocationStrategy

object ConfigNames {

    // region LOGGING

    const val LOGGING_LEVEL = "logging.level"
    const val LOGGING_FOLDER = "logging.folder"
    const val LOGGING_PATTERN = "logging.pattern"
    const val LOGGING_DATE_PATTERN = "logging.date.pattern"
    const val LOGGING_MAX_FILE_SIZE = "logging.max.file.size"
    const val LOGGING_FLUENTD_ENABLED = "logging.fluentd.enabled"

    // endregion

    // region LOGGING DEFAULTS

    const val LOGGING_LEVEL_DEFAULT = "INFO"
    const val LOGGING_FOLDER_DEFAULT = "./logs"
    const val LOGGING_PATTERN_DEFAULT = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
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
    const val DATABASE_CREATE_SCHEMAS_ON_STARTUP = "database.create.schemas.on.startup"
    const val DATABASE_SHOW_SQL_QUERIES = "database.show.sql.queries"
    const val DATABASE_SHOW_LOGS = "database.show.logs"

    // endregion

    // region DATABASE DEFAULTS

    const val DATABASE_MAX_POOL_SIZE_DEFAULT = 5
    const val DATABASE_MAX_LIFE_TIME_DEFAULT = 1800000L

    // endregion

    // region CLOUD

    @RequiredProperty("Needed to select queue connection type")
    const val CLOUD_TYPE = "cloud.type"
    const val CLOUD_SHOW_LOGS = "cloud.show.logs"

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

    // region SUBSCRIBER

    @RequiredProperty("Needed only if allocation strategy is FIXED", depends = true, dependency = "subscriber.master.allocation.strategy", dependencyValue = "FIXED")
    const val SECONDARY_NAME = "subscriber.secondary.name"
    const val SUBSCRIBER_SEND_INFO_PERIOD = "subscriber.secondary.send.info.period"
    const val SUBSCRIBER_SEND_INFO_TIMEOUT = "subscriber.secondary.send.info.timeout"
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_PERIOD = "subscriber.secondary.check.main.node.exists.period"
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_TIMEOUT = "subscriber.secondary.check.main.node.exists.timeout"
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD = "subscriber.main.check.secondary.nodes.up.period"
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT = "subscriber.main.check.secondary.nodes.up.timeout"
    const val SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME = "subscriber.secondary.node.max.inactivity.time"
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD = "subscriber.check.waiting.subscriptions.to.upload.period"
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT = "subscriber.check.waiting.subscriptions.to.upload.timeout"
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_PERIOD = "subscriber.check.waiting.subscriptions.to.remove.period"
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_TIMEOUT = "subscriber.check.waiting.subscriptions.to.remove.timeout"

    @RequiredProperty("All nodes need to know the name of the primary node in order to know to which queue to send availability notification messages and to consume the queue in case they get the primary role")
    const val PRIMARY_NAME = "subscriber.primary.name"
    const val ALLOCATION_STRATEGY = "subscriber.primary.allocation.strategy"

    // endregion

    // region SECONDARY SUBSCRIBER DEFAULTS

    const val SECONDARY_SEND_INFO_PERIOD_DEFAULT = 5000L
    const val SECONDARY_SEND_INFO_TIMEOUT_DEFAULT = 4000L
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_PERIOD_DEFAULT = 5000L
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_TIMEOUT_DEFAULT = 4000L
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD_DEFAULT = 5000L
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT_DEFAULT = 4000L
    const val SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME_DEFAULT = 10000L
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD_DEFAULT = 5000L
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT_DEFAULT = 4000L
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_PERIOD_DEFAULT = 5000L
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_REMOVE_TIMEOUT_DEFAULT = 4000L


    // endregion

    // region SUBSCRIBER DEFAULTS

    val ALLOCATION_STRATEGY_DEFAULT = AllocationStrategy.FIXED

    // endregion

    // region CONTROLLER

    const val CONTROLLER_NAME = "subscriber.controller.name"

    // endregion
}