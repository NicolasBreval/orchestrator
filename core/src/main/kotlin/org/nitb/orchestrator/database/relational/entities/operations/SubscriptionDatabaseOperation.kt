package org.nitb.orchestrator.database.relational.entities.operations

class SubscriptionDatabaseOperation(
    val operationType: OperationType,
    val name: String,
    val subscriber: String,
    val content: String? = null,
    val stopped: Boolean = false
)