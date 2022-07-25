package org.nitb.orchestrator.database.relational.annotations

/**
 * All table classes annotated with this annotation must be created on database at application startup.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TableToCreate
