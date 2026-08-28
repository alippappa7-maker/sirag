package com.siraj.app.core.analytics

import com.siraj.app.data.repository.analytics.FirebaseAnalyticsRepositoryImpl
import com.siraj.app.domain.models.analytics.AnalyticsEvent
import com.siraj.app.domain.repository.analytics.AnalyticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AnalyticsManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var repository: AnalyticsRepository = FirebaseAnalyticsRepositoryImpl()

    fun initialize(repository: AnalyticsRepository) {
        this.repository = repository
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        scope.launch {
            repository.setAnalyticsEnabled(enabled)
        }
    }

    fun logEvent(event: AnalyticsEvent, properties: Map<String, String> = emptyMap()) {
        scope.launch {
            repository.logEvent(event, properties)
        }
    }
    
    fun clearUserData() {
        scope.launch {
            repository.clearUserData()
        }
    }
    
    fun getRepository(): AnalyticsRepository = repository
}
