package org.nitb.orchestrator.config

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

    const val LOGGING_FLUENTD_HOST = "logging.fluentd.host"
    const val LOGGING_FLUENTD_PORT = "logging.fluentd.port"
    const val LOGGING_FLUENTD_TAG = "logging.fluentd.tag"
    const val LOGGING_FLUENTD_TAG_PREFIX = "logging.fluentd.tag.prefix"

    // endregion

    // region FLUENTD DEFAULTS

    const val LOGGING_FLUENTD_DEFAULT_PORT = 24224

    // endregion

    // region DATABASE

    const val DATABASE_DRIVER_CLASSNAME = "database.driver.classname"
    const val DATABASE_JDBC_URL = "database.jdbc.url"
    const val DATABASE_USERNAME = "database.username"
    const val DATABASE_PASSWORD = "database.password"
    const val DATABASE_MAX_POOL_SIZE = "database.max.pool.size"
    const val DATABASE_MAX_LIFE_TIME = "database.max.life.time"

    // endregion

    // region DATABASE DEFAULTS

    const val DATABASE_MAX_POOL_SIZE_DEFAULT = 5
    const val DATABASE_MAX_LIFE_TIME_DEFAULT = 1800000L

    // endregion

    // region CLOUD

    const val CLOUD_TYPE = "cloud.type"

    // region RABBITMQ

    const val RABBITMQ_HOST = "cloud.rabbitmq.host"
    const val RABBITMQ_PORT = "cloud.rabbitmq.port"
    const val RABBITMQ_USERNAME = "cloud.rabbitmq.username"
    const val RABBITMQ_PASSWORD = "cloud.rabbitmq.password"

    // endregion

    // region ACTIVEMQ

    const val ACTIVEMQ_BROKER_URL = "cloud.activemq.broker.url"
    const val ACTIVEMQ_USERNAME = "cloud.activemq.username"
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
}