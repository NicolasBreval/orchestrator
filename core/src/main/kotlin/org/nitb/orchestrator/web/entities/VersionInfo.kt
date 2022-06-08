package org.nitb.orchestrator.web.entities

data class VersionInfo(
    val version: String = System.getProperty("application.version"),
    val environment: String = System.getProperty("application.environment")
)