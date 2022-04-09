package org.nitb.orchestrator

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.DbController
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
import org.nitb.orchestrator.database.relational.entities.operations.OperationType
import org.nitb.orchestrator.database.relational.entities.operations.SubscriptionDatabaseOperation
import org.nitb.orchestrator.subscriber.entities.subscriptions.SubscriptionInfo
import org.nitb.orchestrator.subscription.SubscriptionStatus
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DatabaseTests {

    companion object {
        init {
            ConfigManager.setProperties(mapOf(
                ConfigNames.DATABASE_JDBC_URL to "jdbc:sqlite:database.db",
                ConfigNames.DATABASE_DRIVER_CLASSNAME to "org.sqlite.JDBC",
                ConfigNames.DATABASE_PASSWORD to "",
                ConfigNames.DATABASE_USERNAME to "",
                ConfigNames.DATABASE_CREATE_SCHEMAS_ON_STARTUP to "true",
                ConfigNames.DATABASE_SHOW_SQL_QUERIES to "true"
            ))
        }
    }

    @Test
    fun checkDatabaseCreated() {
        assertTrue(DbController.checkTablesAreCreated())
    }

    @Test
    fun checkDatabaseInserts() {
        DbController.clearSubscriptions()

        DbController.insertSubscriptions(listOf(
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber2", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber3", true, true),
            SubscriptionEntry("subscription 4", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 5", ByteArray(0), "subscriber2", true, true),
            SubscriptionEntry("subscription 6", ByteArray(0), "subscriber3", true, true),
            SubscriptionEntry("subscription 7", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 8", ByteArray(0), "subscriber2", true, true),
            SubscriptionEntry("subscription 9", ByteArray(0), "subscriber3", true, true),
            SubscriptionEntry("subscription 10", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 11", ByteArray(0), "subscriber2", true, true),
            SubscriptionEntry("subscription 12", ByteArray(0), "subscriber3", true, true)
        ))

        val subscriptionPack1 = DbController.getLastActiveSubscriptionsBySubscriber("subscriber1").size
        val subscriptionPack2 = DbController.getLastActiveSubscriptionsBySubscriber("subscriber2").size
        val subscriptionPack3 = DbController.getLastActiveSubscriptionsBySubscriber("subscriber3").size

        assertEquals(subscriptionPack1, 4)
        assertEquals(subscriptionPack2, 4)
        assertEquals(subscriptionPack3, 4)
    }

    @Test
    fun checkDatabaseHistorical() {
        DbController.clearSubscriptions()

        DbController.insertSubscriptions(listOf(
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", true, true),
        ))

        val subscriptionPack = DbController.getLastActiveSubscriptionsBySubscriber("subscriber1").size

        assertEquals(subscriptionPack, 3)

        val subscriptions1 = DbController.getSubscriptionsByName("subscription 1").size
        val subscriptions2 = DbController.getSubscriptionsByName("subscription 2").size
        val subscriptions3 = DbController.getSubscriptionsByName("subscription 3").size

        assertEquals(subscriptions1, 4)
        assertEquals(subscriptions2, 3)
        assertEquals(subscriptions3, 4)
    }

    @Test
    fun checkSubscriptionsUpdate() {
        DbController.clearSubscriptions()

        DbController.insertSubscriptions(listOf(
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber1", true, false),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 3", ByteArray(0), "subscriber1", false, true),
        ))

        val operations = listOf(
            SubscriptionDatabaseOperation(OperationType.REMOVE, "subscription 1", "subscriber1"),
            SubscriptionDatabaseOperation(OperationType.ADD, "subscription 2", "subscriber1"),
            SubscriptionDatabaseOperation(OperationType.UPDATE_CONTENT, "subscription 3", "subscriber1", "New content"),
            SubscriptionDatabaseOperation(OperationType.ADD, "subscription 4", "subscriber1", "New content"),
            SubscriptionDatabaseOperation(OperationType.START, "subscription 1", "subscriber1"),
            SubscriptionDatabaseOperation(OperationType.STOP, "subscription 3", "subscriber1")
        )

        DbController.addOperationsToWaitingList("subscriber1", operations)

        Thread.sleep(200)

        val lastSubscriptions = DbController.getLastActiveSubscriptionsBySubscriber("subscriber1").associateBy { it.name }

        assertEquals(lastSubscriptions["subscription 1"]?.active, false)
        assertEquals(lastSubscriptions["subscription 2"]?.active, true)
        assertEquals(lastSubscriptions["subscription 3"]?.content?.let { String(it) }, "New content")
        assertEquals(lastSubscriptions["subscription 4"]?.content?.let { String(it) }, "New content")
        assertEquals(lastSubscriptions["subscription 1"]?.stopped, false)
        assertEquals(lastSubscriptions["subscription 2"]?.stopped, true)
    }

    @Test
    fun checkLastActiveSubscriptions() {
        DbController.clearSubscriptions()

        DbController.insertSubscriptions(listOf(
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber1", true, true),
            SubscriptionEntry("subscription 1", ByteArray(0), "subscriber2", true, false),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber3", true, true),
            SubscriptionEntry("subscription 2", ByteArray(0), "subscriber1", true, true)
        ))

        val last = DbController.getLastActiveSubscriptions()

        for (subscription in last) {
            if (subscription.name == "subscription 1") {
                assertEquals(subscription.subscriber, "subscriber1")
            } else if (subscription.name == "subscription 2") {
                assertEquals(subscription.subscriber, "subscriber1")
            }
        }
    }

}