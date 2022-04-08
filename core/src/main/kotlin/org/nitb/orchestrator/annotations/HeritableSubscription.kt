package org.nitb.orchestrator.annotations

/**
 * Annotation to mark subscriptions that can be inherited outside the module.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HeritableSubscription
