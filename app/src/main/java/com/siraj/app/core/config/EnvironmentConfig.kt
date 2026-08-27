package com.siraj.app.core.config

enum class EnvironmentType {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

object EnvironmentConfig {
    // In a real app, this could be configured via BuildConfig injected from build.gradle.kts
    val currentEnvironment = EnvironmentType.DEVELOPMENT

    val apiBaseUrl: String
        get() = when (currentEnvironment) {
            EnvironmentType.DEVELOPMENT -> "https://api.dev.siraj.app"
            EnvironmentType.STAGING -> "https://api.staging.siraj.app"
            EnvironmentType.PRODUCTION -> "https://api.siraj.app"
        }

    val isDebugEnabled: Boolean
        get() = currentEnvironment != EnvironmentType.PRODUCTION
}
