package com.siraj.app.core.config

import com.siraj.app.BuildConfig

enum class EnvironmentType(
    val displayName: String,
) {
    DEVELOPMENT("بيئة التطوير (Development)"),
    STAGING("بيئة تجريبية (Staging / Beta)"),
    PRODUCTION("بيئة الإنتاج (Production)"),
}

object EnvironmentConfig {
    val currentEnvironment: EnvironmentType
        get() =
            try {
                when (BuildConfig.ENVIRONMENT.lowercase()) {
                    "staging", "beta" -> EnvironmentType.STAGING
                    "production", "prod" -> EnvironmentType.PRODUCTION
                    else -> EnvironmentType.DEVELOPMENT
                }
            } catch (e: Throwable) {
                EnvironmentType.DEVELOPMENT
            }

    val isBeta: Boolean
        get() =
            try {
                BuildConfig.IS_BETA || currentEnvironment == EnvironmentType.STAGING || currentEnvironment == EnvironmentType.DEVELOPMENT
            } catch (e: Throwable) {
                true
            }

    val versionName: String
        get() =
            try {
                BuildConfig.VERSION_NAME
            } catch (e: Throwable) {
                "1.0.0-beta.1"
            }

    val versionCode: Int
        get() =
            try {
                BuildConfig.VERSION_CODE
            } catch (e: Throwable) {
                1
            }

    val allowMockData: Boolean
        get() =
            try {
                BuildConfig.ALLOW_MOCK_DATA && currentEnvironment != EnvironmentType.PRODUCTION
            } catch (e: Throwable) {
                currentEnvironment != EnvironmentType.PRODUCTION
            }

    val apiBaseUrl: String
        get() =
            when (currentEnvironment) {
                EnvironmentType.DEVELOPMENT -> "https://api.dev.siraj.app"
                EnvironmentType.STAGING -> "https://api.staging.siraj.app"
                EnvironmentType.PRODUCTION -> "https://api.siraj.app"
            }

    val isDebugEnabled: Boolean
        get() = currentEnvironment != EnvironmentType.PRODUCTION

    val buildIdentifier: String
        get() = "v$versionName ($versionCode) - ${currentEnvironment.name}"

    val releaseLabel: String
        get() =
            when (currentEnvironment) {
                EnvironmentType.PRODUCTION -> "سراج"
                EnvironmentType.STAGING -> "سراج (Beta)"
                EnvironmentType.DEVELOPMENT -> "Siraj Dev"
            }
}
