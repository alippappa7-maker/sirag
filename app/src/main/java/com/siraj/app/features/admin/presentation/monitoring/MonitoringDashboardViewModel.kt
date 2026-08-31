package com.siraj.app.features.admin.presentation.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.monitoring.IncidentSeverity
import com.siraj.app.domain.models.monitoring.IncidentState
import com.siraj.app.domain.models.monitoring.MonitoredService
import com.siraj.app.domain.models.monitoring.MonitoringAlert
import com.siraj.app.domain.models.monitoring.ServiceCategory
import com.siraj.app.domain.models.monitoring.ServiceHealthCheck
import com.siraj.app.domain.models.monitoring.ServiceIncident
import com.siraj.app.domain.models.monitoring.SystemTelemetryOverview
import com.siraj.app.domain.repository.monitoring.MonitoringRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MonitoringDashboardUiState(
    val telemetryOverview: SystemTelemetryOverview = SystemTelemetryOverview(),
    val servicesHealthList: List<ServiceHealthCheck> = emptyList(),
    val filteredServicesList: List<ServiceHealthCheck> = emptyList(),
    val activeIncidents: List<ServiceIncident> = emptyList(),
    val incidentHistory: List<ServiceIncident> = emptyList(),
    val activeAlerts: List<MonitoringAlert> = emptyList(),
    val selectedCategory: ServiceCategory? = null,
    val isProbing: Boolean = false,
    val bannerMessage: String? = null,
    val selectedIncidentForDetail: ServiceIncident? = null,
    val selectedServiceForRunbook: MonitoredService? = null
)

class MonitoringDashboardViewModel(
    private val repository: MonitoringRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitoringDashboardUiState())
    val uiState: StateFlow<MonitoringDashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getTelemetryOverviewStream(),
                repository.getServicesHealthStream(),
                repository.getActiveIncidentsStream(),
                repository.getIncidentHistoryStream(),
                repository.getActiveAlertsStream()
            ) { telemetry, healthList, activeIncidents, incidentHistory, activeAlerts ->
                val category = _uiState.value.selectedCategory
                val filtered = if (category == null) {
                    healthList
                } else {
                    healthList.filter { it.service.category == category }
                }

                _uiState.value.copy(
                    telemetryOverview = telemetry,
                    servicesHealthList = healthList,
                    filteredServicesList = filtered,
                    activeIncidents = activeIncidents,
                    incidentHistory = incidentHistory,
                    activeAlerts = activeAlerts
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun filterByCategory(category: ServiceCategory?) {
        val allServices = _uiState.value.servicesHealthList
        val filtered = if (category == null) {
            allServices
        } else {
            allServices.filter { it.service.category == category }
        }
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredServicesList = filtered
        )
    }

    fun runAllHealthProbes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProbing = true,
                bannerMessage = "جارٍ فحص استجابة ومؤشرات جميع الخدمات والواجهات..."
            )
            val result = repository.runAllHealthProbes()
            _uiState.value = _uiState.value.copy(
                isProbing = false,
                bannerMessage = if (result.isSuccess) {
                    "اكتمل الفحص الآلي لجميع الخدمات بنجاح"
                } else {
                    "تعذر إكمال فحص بعض الخدمات: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun runSingleProbe(service: MonitoredService) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(bannerMessage = "جارٍ فحص ${service.displayNameArabic}...")
            val res = repository.runProbeHealthCheck(service)
            if (res.isSuccess) {
                val check = res.getOrNull() ?: return
                _uiState.value = _uiState.value.copy(
                    bannerMessage = "تم فحص ${service.displayNameArabic}: زمن الاستجابة ${check.latencyMs}ms (${check.status.displayNameArabic})"
                )
            }
        }
    }

    fun toggleCircuitBreaker(service: MonitoredService, disable: Boolean, reasonArabic: String) {
        viewModelScope.launch {
            val res = repository.toggleServiceCircuitBreaker(service, disable, reasonArabic)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    bannerMessage = if (disable) 
                        "تم تعطيل ${service.displayNameArabic} احترازياً وتحويل الطلبات للمسار البديل"
                    else 
                        "تمت إعادة تفعيل ${service.displayNameArabic} وتأكيد الجاهزية"
                )
            }
        }
    }

    fun createIncident(service: MonitoredService, title: String, description: String, severity: IncidentSeverity) {
        viewModelScope.launch {
            val res = repository.createIncident(service, title, description, severity)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    bannerMessage = "تم فتح بلاغ عطل جديد (${res.getOrNull()?.incidentId}) وإرسال التنبيهات اللازمة"
                )
            }
        }
    }

    fun updateIncidentState(
        incidentId: String,
        newState: IncidentState,
        notesArabic: String,
        rootCause: String? = null,
        mitigation: String? = null
    ) {
        viewModelScope.launch {
            val res = repository.updateIncidentState(incidentId, newState, notesArabic, rootCause, mitigation)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    bannerMessage = "تم تحديث حالة البلاغ $incidentId إلى (${newState.displayNameArabic})"
                )
            }
        }
    }

    fun acknowledgeAlert(alertId: String) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
        }
    }

    fun dismissAlert(alertId: String) {
        viewModelScope.launch {
            repository.dismissAlert(alertId)
        }
    }

    fun selectIncidentForDetail(incident: ServiceIncident?) {
        _uiState.value = _uiState.value.copy(selectedIncidentForDetail = incident)
    }

    fun selectServiceForRunbook(service: MonitoredService?) {
        _uiState.value = _uiState.value.copy(selectedServiceForRunbook = service)
    }

    fun clearBanner() {
        _uiState.value = _uiState.value.copy(bannerMessage = null)
    }
}
