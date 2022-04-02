package org.nitb.orchestrator.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiredProperty(
    val value: String = "",
    val depends: Boolean = false,
    val dependency: String = "",
    val dependencyValue: String = ""
)
