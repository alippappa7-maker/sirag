package com.siraj.app

import com.siraj.app.core.error.GlobalErrorHandler

/**
 * Centralized Build Configuration.
 * Provides a single source of truth for build-time and runtime configuration.
 */
object BuildConfig {
    
    val isBeta: Boolean get() = BuildConfig_IS_BETA
    val allowMockData: Boolean get() = BuildConfig_ALLOW_MOCK_DATA && BuildConfig_IS_BETA
    val environment: String get() = BuildConfig_ENVIRONMENT
    
    /**
     * True when running in production release mode.
     * Mock data is automatically disabled in production.
     */
    val isProduction: Boolean get() = environment.equals("production", ignoreCase = true)
    
    /**
     * Safely determine if mock data should be used.
     * Only allowed in development/beta, never in production.
     */
    val shouldUseMockData: Boolean get() = allowMockData && !isProduction
}
