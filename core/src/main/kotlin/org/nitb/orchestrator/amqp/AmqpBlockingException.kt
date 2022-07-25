package org.nitb.orchestrator.amqp

/**
 * Special exception used to don't send ack to AMQP server if subscription throws it.
 */
class AmqpBlockingException(
    message: String,
    parent: Throwable
): Exception(message, parent)