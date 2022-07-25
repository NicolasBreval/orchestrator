package org.nitb.orchestrator.amqp

/**
 * Annotation used to identify an AMQP client class with their related amqp type property.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AmqpType(
    val type: String
)
