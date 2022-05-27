package org.nitb.orchestrator.amqp

class AmqpBlockingException(
    message: String,
    parent: Throwable
): Exception(message, parent) {
}