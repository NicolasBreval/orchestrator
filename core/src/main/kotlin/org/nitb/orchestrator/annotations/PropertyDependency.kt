package org.nitb.orchestrator.annotations

/**
 * Annotation used to specify dependencies between properties.
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class PropertyDependency(
    val propertyName: String = "",
    val propertyValue: String = ""
)