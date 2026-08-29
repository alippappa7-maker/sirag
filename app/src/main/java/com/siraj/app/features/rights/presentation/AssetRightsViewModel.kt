package com.siraj.app.features.rights.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Asset
import com.siraj.app.domain.models.RightsStatus
import com.siraj.app.domain.repository.AssetRepository
import com.siraj.app.features.rights.domain.repository.RightsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RightsUiState(
    val asset: Asset? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdated: Boolean = false
)

class AssetRightsViewModel(
    private val rightsRepository: RightsRepository,
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RightsUiState())
    val uiState: StateFlow<RightsUiState> = _uiState.asStateFlow()

    fun loadAsset(assetId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = assetRepository.getAsset(assetId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(asset = result.data, isLoading = false)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateRights(
        assetId: String,
        reviewerId: String,
        sourceUrl: String,
        creatorName: String,
        provider: String,
        licenseType: String,
        commercialUseAllowed: Boolean,
        modificationAllowed: Boolean,
        attributionRequired: Boolean,
        attributionText: String,
        proofUrl: String,
        expiresAt: Long?,
        newStatus: RightsStatus,
        reason: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isUpdated = false)
            val currentAsset = _uiState.value.asset
            if (currentAsset == null) {
                _uiState.value = _uiState.value.copy(error = "Asset not loaded", isLoading = false)
                return@launch
            }

            val updates = mapOf(
                "sourceUrl" to sourceUrl,
                "creatorName" to creatorName,
                "provider" to provider,
                "license" to licenseType,
                "commercialUseAllowed" to commercialUseAllowed,
                "modificationAllowed" to modificationAllowed,
                "attributionRequired" to attributionRequired,
                "attribution" to attributionText,
                "proofUrl" to proofUrl,
                "expiresAt" to expiresAt,
                "rightsStatus" to newStatus.name,
                "acquiredAt" to (currentAsset.acquiredAt ?: System.currentTimeMillis())
            )

            val updateResult = rightsRepository.updateAssetRights(assetId, updates)
            if (updateResult is Resource.Success) {
                // Log decision
                rightsRepository.logRightsDecision(
                    assetId = assetId,
                    reviewerId = reviewerId,
                    previousStatus = currentAsset.rightsStatus,
                    newStatus = newStatus,
                    reason = reason
                )
                
                _uiState.value = _uiState.value.copy(isLoading = false, isUpdated = true)
                loadAsset(assetId) // reload
            } else if (updateResult is Resource.Error) {
                _uiState.value = _uiState.value.copy(error = updateResult.message, isLoading = false)
            }
        }
    }
}
