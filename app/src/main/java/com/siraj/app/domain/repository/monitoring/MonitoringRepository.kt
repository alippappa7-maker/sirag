package com.siraj.app.domain.repository.monitoring

import com.siraj.app.domain.models.monitoring.IncidentSeverity
import com.siraj.app.domain.models.monitoring.IncidentState
import com.siraj.app.domain.models.monitoring.MonitoredService
import com.siraj.app.domain.models.monitoring.MonitoringAlert
import com.siraj.app.domain.models.monitoring.ServiceHealthCheck
import com.siraj.app.domain.models.monitoring.ServiceIncident
import com.siraj.app.domain.models.monitoring.SystemTelemetryOverview
import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    fun getServicesHealthStream(): Flow<List<ServiceHealthCheck>>

    fun getTelemetryOverviewStream(): Flow<SystemTelemetryOverview>

    fun getActiveIncidentsStream(): Flow<List<ServiceIncident>>

    fun getIncidentHistoryStream(): Flow<List<ServiceIncident>>

    fun getActiveAlertsStream(): Flow<List<MonitoringAlert>>

    suspend fun runProbeHealthCheck(service: MonitoredService): Result<ServiceHealthCheck>

    suspend fun runAllHealthProbes(): Result<List<ServiceHealthCheck>>

    suspend fun toggleServiceCircuitBreaker(
        service: MonitoredService,
        disabled: Boolean,
        reasonArabic: String,
    ): Result<Boolean>

    suspend fun createIncident(
        service: MonitoredService,
        titleArabic: String,
        descriptionArabic: String,
        severity: IncidentSeverity,
    ): Result<ServiceIncident>

    suspend fun updateIncidentState(
        incidentId: String,
        newState: IncidentState,
        notesArabic: String,
        rootCauseSummary: String? = null,
        mitigationAction: String? = null,
    ): Result<ServiceIncident>

    suspend fun acknowledgeAlert(alertId: String): Result<Boolean>

    suspend fun dismissAlert(alertId: String): Result<Boolean>

    /**
     * Sanitizes user-facing messages so that internal cloud hostnames, stack traces,
     * or provider-specific internals are completely hidden from the client.
     */
    fun sanitizePublicErrorMessage(
        service: MonitoredService,
        internalError: String?,
    ): String
}
