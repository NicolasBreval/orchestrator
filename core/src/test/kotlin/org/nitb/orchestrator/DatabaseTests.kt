package org.nitb.orchestrator

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.database.relational.DbController
import org.nitb.orchestrator.database.relational.entities.SubscriptionEntry
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
                ConfigNames.DATABASE_CREATE_SCHEMAS_ON_STARTUP to "true"
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

}