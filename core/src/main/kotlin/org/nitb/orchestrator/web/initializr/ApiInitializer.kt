package org.nitb.orchestrator.web.initializr

import io.micronaut.runtime.Micronaut.*

object ApiInitializer {

    fun initialize(vararg args: String) {

        build()
            .args(*args)
            .packages("org.nitb.orchestrator.web")
            .eagerInitSingletons(true)
            .start()
    }
}