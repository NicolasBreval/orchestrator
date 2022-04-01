package org.nitb.orchestrator.config

import java.lang.Exception

class NotFoundPropertyException: Exception {

    constructor(msg: String): super(msg)

    constructor(cause: Throwable): super(cause)

    constructor(msg: String, cause: Throwable): super(msg, cause)
}