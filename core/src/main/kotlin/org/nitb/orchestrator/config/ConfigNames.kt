package org.nitb.orchestrator.config

import org.nitb.orchestrator.annotations.RequiredProperty
import org.nitb.orchestrator.subscriber.entities.subscribers.AllocationStrategy

object ConfigNames {

    // region LOGGING

    /**
     * Allows specifying log level for all elements in program.
     */
    const val LOGGING_LEVEL = "logging.level"

    /**
     * Allows specifying folder path where log files are located.
     */
    const val LOGGING_FOLDER = "logging.folder"

    /**
     * Allows specifying log pattern for log lines.
     */
    const val LOGGING_PATTERN = "logging.pattern"

    /**
     * Allows specifying logging date pattern used in log file names.
     */
    const val LOGGING_DATE_PATTERN = "logging.date.pattern"

    /**
     * Allows specifying max size for log files.
     */
    const val LOGGING_MAX_FILE_SIZE = "logging.max.file.size"

    // endregion

    // region LOGGING DEFAULTS

    /**
     * Default value used for log level if is not set in [LOGGING_LEVEL] property.
     */
    const val LOGGING_LEVEL_DEFAULT = "INFO"

    /**
     * Default value used for log folder path if is not set in [LOGGING_FOLDER] property.
     */
    const val LOGGING_FOLDER_DEFAULT = "./logs"

    /**
     * Default value used for log pattern if is not set in [LOGGING_PATTERN] property.
     */
    const val LOGGING_PATTERN_DEFAULT = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

    /**
     * Default value used for date pattern if is not set in [LOGGING_DATE_PATTERN] property.
     */
    const val LOGGING_DATE_PATTERN_DEFAULT = "yyyy-MM-dd"

    /**
     * Default value used for log files' max size if is not set in [LOGGING_MAX_FILE_SIZE] property.
     */
    const val LOGGING_MAX_FILE_SIZE_DEFAULT = "512MB"

    // endregion

    // region DATABASE

    /**
     * Name of driver used in database connection.
     */
    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_DRIVER_CLASSNAME = "database.driver.classname"

    /**
     * JDBC url used to connect to database.
     *
     * @see <a href="https://www.baeldung.com/java-jdbc-url-format">JDBC URL Format For Different Databases - By Baeldung</a>
     */
    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_JDBC_URL = "database.jdbc.url"

    /**
     * Username used to connect with database.
     */
    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_USERNAME = "database.username"

    /**
     * Password used to connect with database
     */
    @RequiredProperty("Master node needs to connect to database, also slaves can connect to database when changes to master mode")
    const val DATABASE_PASSWORD = "database.password"

    /**
     * Pool size used to configure Hikari pool
     */
    const val DATABASE_MAX_POOL_SIZE = "database.max.pool.size"

    /**
     * Max lifetime of connection inside Hikari pool.
     */
    const val DATABASE_MAX_LIFE_TIME = "database.max.life.time"

    /**
     * If is true, creates all needed tables by orchestrator core on startup.
     */
    const val DATABASE_CREATE_SCHEMAS_ON_STARTUP = "database.create.schemas.on.startup"

    /**
     * If is true, all queries made from database controller are shown in logs.
     */
    const val DATABASE_SHOW_SQL_QUERIES = "database.show.sql.queries"

    /**
     * If is true, show all Hikari logs.
     */
    const val DATABASE_SHOW_LOGS = "database.show.logs"

    const val DATABASE_CREATE_ON_STARTUP_SCHEMAS_PACKAGES = "database.create.on.startup.schemas.packages"

    const val DATABASE_ORCHESTRATOR_SCHEMA = "database.orchestrator.schema"

    // endregion

    // region DATABASE DEFAULTS

    /**
     * Default property for Hikari pool size.
     */
    const val DATABASE_MAX_POOL_SIZE_DEFAULT = 5

    /**
     * Default property for Hikari lifetime.
     */
    const val DATABASE_MAX_LIFE_TIME_DEFAULT = 1800000L

    // endregion

    // region AMQP

    /**
     * Selects type of technology used for queues management
     */
    @RequiredProperty("Needed to select queue connection type")
    const val AMQP_TYPE = "amqp.type"

    /**
     * If is true, shows logs related to queue management.
     */
    const val AMQP_SHOW_LOGS = "amqp.show.logs"

    /**
     * Number of retries to process failed input message from queue
     */
    const val AMQP_RETRIES = "amqp.retries"

    // region RABBITMQ

    /**
     * Hostname of RabbitMQ server. This property is only required if [AMQP_TYPE] property is RABBITMQ.
     */
    @RequiredProperty("Only if amqp.type is RABBITMQ", depends = true, dependency = "amqp.type", dependencyValue = "RABBITMQ")
    const val RABBITMQ_HOST = "amqp.rabbitmq.host"

    /**
     * Port used to connect with RabbitMQ server
     */
    const val RABBITMQ_PORT = "amqp.rabbitmq.port"

    /**
     * Username used to connect with RabbitMQ server. This property is only required if [AMQP_TYPE] property is RABBITMQ.
     */
    @RequiredProperty("Only if amqp.type is RABBITMQ", depends = true, dependency = "amqp.type", dependencyValue = "RABBITMQ")
    const val RABBITMQ_USERNAME = "amqp.rabbitmq.username"

    /**
     * Password used to connect with RabbitMQ server. This property is only required if [AMQP_TYPE] property is RABBITMQ.
     */
    @RequiredProperty("Only if amqp.type is RABBITMQ", depends = true, dependency = "amqp.type", dependencyValue = "RABBITMQ")
    const val RABBITMQ_PASSWORD = "amqp.rabbitmq.password"

    // endregion

    // region ACTIVEMQ

    /**
     * URL used to connect with ActiveMQ server(s). It's mandatory that broker URL must have this pattern:
     * *failover:{protocol}://{host}:{port}*, because if you don't put *failover* before URL, if server falls, client couldn't reconnect.
     * This property is only required if [AMQP_TYPE] property is ACTIVEMQ.
     */
    @RequiredProperty("Only if amqp.type is ACTIVEMQ", depends = true, dependency = "amqp.type", dependencyValue = "ACTIVEMQ")
    const val ACTIVEMQ_BROKER_URL = "amqp.activemq.broker.url"

    /**
     * Username used to connect with ActiveMQ server(s).
     * This property is only required if [AMQP_TYPE] property is ACTIVEMQ.
     */
    @RequiredProperty("Only if amqp.type is ACTIVEMQ", depends = true, dependency = "amqp.type", dependencyValue = "ACTIVEMQ")
    const val ACTIVEMQ_USERNAME = "amqp.activemq.username"

    /**
     * Password used to connect with ActiveMQ server(s).
     * This property is only required if [AMQP_TYPE] property is ACTIVEMQ.
     */
    @RequiredProperty("Only if amqp.type is ACTIVEMQ", depends = true, dependency = "amqp.type", dependencyValue = "ACTIVEMQ")
    const val ACTIVEMQ_PASSWORD = "amqp.activemq.password"

    // endregion

    // endregion

    // region AMQP DEFAULTS

    /**
     * Amqp clients don't retry to process message by default
     */
    const val AMQP_RETRIES_DEFAULT = 0

    const val AMQP_WORKERS_DEFAULT = 1

    // region RABBITMQ DEFAULTS

    /**
     * Default port used to connect with RabbitMQ server, if [RABBITMQ_PORT] is not set.
     */
    const val RABBITMQ_DEFAULT_PORT = 5672

    /**
     * Default username used to connect with RabbitMQ server, if [RABBITMQ_USERNAME] is not set.
     */
    const val RABBITMQ_DEFAULT_USERNAME = "guest"

    /**
     * Default password used to connect with RabbitMQ server, if [RABBITMQ_PASSWORD] is not set.
     */
    const val RABBITMQ_DEFAULT_PASSWORD = "guest"

    // endregion

    // region ACTIVEMQ DEFAULTS

    /**
     * Default username used to connect with ActiveMQ server, if [ACTIVEMQ_USERNAME] is not set.
     */
    const val ACTIVEMQ_DEFAULT_USERNAME = "admin"

    /**
     * Default password used to connect with ActiveMQ server, if [ACTIVEMQ_PASSWORD] is not set.
     */
    const val ACTIVEMQ_DEFAULT_PASSWORD = "admin"

    // endregion

    // endregion

    // region SUBSCRIBER

    /**
     * Name used to detect a secondary node. This property is required only if [ALLOCATION_STRATEGY] property is FIXED.
     */
    @RequiredProperty("Needed only if allocation strategy is FIXED", depends = true, dependency = "subscriber.master.allocation.strategy", dependencyValue = "FIXED")
    const val SECONDARY_NAME = "subscriber.secondary.name"

    /**
     * Period used to send information from the secondary node to the primary node.
     */
    const val SUBSCRIBER_SEND_INFO_PERIOD = "subscriber.secondary.send.info.period"

    /**
     * Max time when a secondary node is sending information to main node.
     */
    const val SUBSCRIBER_SEND_INFO_TIMEOUT = "subscriber.secondary.send.info.timeout"

    /**
     * Period used in a secondary node to check if main node exists.
     */
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_PERIOD = "subscriber.secondary.check.main.node.exists.period"

    /**
     * Max time when a secondary node is checking if main node exists.
     */
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_TIMEOUT = "subscriber.secondary.check.main.node.exists.timeout"

    /**
     * Period used in main node to check if previously registered secondary node is fallen.
     */
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD = "subscriber.main.check.secondary.nodes.up.period"

    /**
     * Max time that main node is checking for fallen secondary nodes.
     */
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT = "subscriber.main.check.secondary.nodes.up.timeout"

    /**
     * Max time when a secondary node is not sending information to main node without be marked as fallen.
     */
    const val SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME = "subscriber.secondary.node.max.inactivity.time"

    /**
     * Period used to check subscriptions waiting to be sent to a secondary node to be executed.
     */
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD = "subscriber.check.waiting.subscriptions.to.upload.period"

    /**
     * Max time that main subscriber is checking for waiting subscriptions.
     */
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT = "subscriber.check.waiting.subscriptions.to.upload.timeout"

    /**
     * Name related to main node. All nodes must contain same value for this property.
     */
    @RequiredProperty("All nodes need to know the name of the primary node in order to know to which queue to send availability notification messages and to consume the queue in case they get the primary role")
    const val PRIMARY_NAME = "subscriber.primary.name"

    /**
     * Strategy used to reallocate subscriptions when a node is fallen.
     */
    const val ALLOCATION_STRATEGY = "subscriber.primary.allocation.strategy"

    /**
     * Port used for Subscriber API
     */
    const val HTTP_PORT = "server.http.port"

    // endregion

    // region SECONDARY SUBSCRIBER DEFAULTS

    /**
     * Default value for [SUBSCRIBER_SEND_INFO_PERIOD], if is not set.
     */
    const val SECONDARY_SEND_INFO_PERIOD_DEFAULT = 500L

    /**
     * Default value for [SUBSCRIBER_SEND_INFO_TIMEOUT], if is not set.
     */
    const val SECONDARY_SEND_INFO_TIMEOUT_DEFAULT = 4000L

    /**
     * Default value for [SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_PERIOD], if is not set.
     */
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_PERIOD_DEFAULT = 5000L

    /**
     * Default value for [SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_TIMEOUT], if is not set.
     */
    const val SUBSCRIBER_CHECK_MAIN_NODE_EXISTS_TIMEOUT_DEFAULT = 4000L

    /**
     * Default value for [SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD], if is not set.
     */
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_PERIOD_DEFAULT = 5000L

    /**
     * Default value for [SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT], if is not set.
     */
    const val SUBSCRIBER_CHECK_SECONDARY_NODES_UP_TIMEOUT_DEFAULT = 4000L

    /**
     * Default value for [SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME], if is not set.
     */
    const val SUBSCRIBER_SECONDARY_NODE_MAX_INACTIVITY_TIME_DEFAULT = 7000L

    /**
     * Default value for [SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD], if is not set.
     */
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_PERIOD_DEFAULT = 5000L

    /**
     * Default value for [SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT], if is not set.
     */
    const val SUBSCRIBER_CHECK_WAITING_SUBSCRIPTIONS_TO_UPLOAD_TIMEOUT_DEFAULT = 4000L

    /**
     * Default value for [HTTP_PORT], if is not set
     */
    const val HTTP_PORT_DEFAULT = 80

    // endregion

    // region SUBSCRIBER DEFAULTS

    /**
     * Default allocation strategy used
     */
    val ALLOCATION_STRATEGY_DEFAULT = AllocationStrategy.FIXED

    // endregion

    // region CONTROLLER

    /**
     * Name used for controller node. Display node is the node to communicate user with cluster.
     */
    const val DISPLAY_NODE_NAME = "display.node.name"

    // endregion

    // region OPEN API

    const val OPEN_API_RESOURCE_ACTIVE = "open.api.resource.active"

    // endregion

    // region HTTP CLIENT

    const val HTTP_CLIENT_RETRIES = "http.client.retries"

    const val HTTP_CLIENT_TIME_BETWEEN_RETRIES = "http.time.between.retries"

    // endregion

    // region HTTP_CLIENT_DEFAULTS

    const val HTTP_CLIENT_RETRIES_DEFAULT = 3

    const val HTTP_CLIENT_TIME_BETWEEN_RETRIES_DEFAULT = 500L

    // endregion

}