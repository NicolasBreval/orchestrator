package org.nitb.orchestrator.amqp

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AmqpType(
    val type: String
)
