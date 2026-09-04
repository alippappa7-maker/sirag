package com.siraj.app.features.project.presentation.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.Asset
import com.siraj.app.domain.models.AssetStatus
import com.siraj.app.domain.models.AssetType
import com.siraj.app.domain.models.Project
import com.siraj.app.domain.repository.AssetRepository
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.data.repository.FirebaseAssetRepositoryImpl
import com.siraj.app.data.repository.FirebaseProjectRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AssetLibraryViewModel(
    private val projectId: String,
    private val assetRepository: AssetRepository = FirebaseAssetRepositoryImpl(),
    private val projectRepository: ProjectRepository = FirebaseProjectRepositoryImpl(),
) : ViewModel() {
    private val _assetsState = MutableStateFlow<Resource<List<Asset>>>(Resource.Loading)
    val assetsState: StateFlow<Resource<List<Asset>>> = _assetsState.asStateFlow()

    private val _projectState = MutableStateFlow<Project?>(null)
    val projectState: StateFlow<Project?> = _projectState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        loadProjectAndAssets()
    }

    private fun loadProjectAndAssets() {
        viewModelScope.launch {
            val res = projectRepository.getProject(projectId)
            if (res is Resource.Success) {
                _projectState.value = res.data
            }
        }

        viewModelScope.launch {
            assetRepository.getProjectAssets(projectId).collect {
                _assetsState.value = it
            }
        }
    }

    fun uploadAsset(
        name: String,
        type: AssetType,
        sourceUrl: String,
        license: String,
        attribution: String,
    ) {
        val proj = _projectState.value ?: return

        val sizeBytes = if (type == AssetType.VIDEO) 50_000_000L else 1_000_000L
        if (type == AssetType.VIDEO && sizeBytes > 500_000_000L) {
            viewModelScope.launch { _uiMessage.emit("خطأ: حجم الفيديو يتجاوز 500 ميغابايت") }
            return
        }

        val storagePath = "workspaces/${proj.workspaceId}/projects/${proj.id}/${type.name.lowercase()}_${System.currentTimeMillis()}"
        val newAsset =
            Asset(
                ownerId = proj.ownerId,
                workspaceId = proj.workspaceId,
                projectId = proj.id,
                type = type,
                storagePath = storagePath,
                downloadUrl = sourceUrl.ifEmpty { "https://storage.siraj.app/$storagePath" },
                mimeType =
                    when (type) {
                        AssetType.IMAGE -> "image/jpeg"
                        AssetType.VIDEO -> "video/mp4"
                        AssetType.AUDIO -> "audio/mp3"
                        else -> "application/octet-stream"
                    },
                sizeBytes = sizeBytes,
                sourceUrl = sourceUrl,
                license = license,
                attribution = attribution,
                status = AssetStatus.READY,
            )

        viewModelScope.launch {
            val res = assetRepository.addAsset(newAsset)
            if (res is Resource.Success) {
                _uiMessage.emit("تم رفع الأصل بنجاح")
            } else if (res is Resource.Error) {
                _uiMessage.emit(res.message)
            }
        }
    }

    fun updateAsset(asset: Asset) {
        viewModelScope.launch {
            assetRepository.updateAsset(asset)
            _uiMessage.emit("تم التحديث")
        }
    }

    fun deleteAsset(asset: Asset) {
        // Check if used in project (we will just warn, or prevent if strictly required)
        val proj = _projectState.value
        val isUsed = proj?.scenes?.any { it.assetIds.contains(asset.id) } == true

        if (isUsed) {
            viewModelScope.launch {
                _uiMessage.emit("تحذير: هذا الأصل مستخدم في أحد المشاهد! أزله من المشاهد أولاً.")
            }
            return
        }

        viewModelScope.launch {
            val res = assetRepository.deleteAsset(asset)
            if (res is Resource.Success) {
                _uiMessage.emit("تم الحذف بنجاح")
            } else if (res is Resource.Error) {
                _uiMessage.emit(res.message)
            }
        }
    }
}

class AssetLibraryViewModelFactory(
    private val projectId: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetLibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssetLibraryViewModel(projectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
