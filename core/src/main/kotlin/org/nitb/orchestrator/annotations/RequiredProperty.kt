package org.nitb.orchestrator.annotations

/**
 * Annotation used to show developer which properties are imperative for correct execution.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class RequiredProperty(
    val description: String = ""
)
