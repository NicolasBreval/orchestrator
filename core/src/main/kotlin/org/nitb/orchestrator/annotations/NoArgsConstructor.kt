package org.nitb.orchestrator.annotations

/**
 * Annotation used by Kotlin no-args compiler plugin to create no-arguments constructor automatically for objects annotated with it.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoArgsConstructor
