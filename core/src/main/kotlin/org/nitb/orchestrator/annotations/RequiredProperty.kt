package org.nitb.orchestrator.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiredProperty(
    val demandCause: String = ""
)
