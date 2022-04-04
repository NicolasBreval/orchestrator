package org.nitb.orchestrator.web

import io.micronaut.http.annotation.Get

abstract class BaseController {

    @Get("/hello")
    fun hello(): String {
        return "hello"
    }
}