package com.siraj.app.features.taxonomy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.taxonomy.*
import com.siraj.app.domain.repository.taxonomy.ContentTaxonomyFilter
import com.siraj.app.domain.repository.taxonomy.ContentTaxonomyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ContentTaxonomyUiState(
    val items: List<ClassifiedContentItem> = emptyList(),
    val filteredItems: List<ClassifiedContentItem> = emptyList(),
    val filter: ContentTaxonomyFilter = ContentTaxonomyFilter(),
    val selectedItem: ClassifiedContentItem? = null,
    val auditReport: TaxonomyAuditReport? = null,
    val migrationResult: TaxonomyMigrationResult? = null,
    val isLoading: Boolean = false,
    val isMigrating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ContentTaxonomyViewModel(
    private val repository: ContentTaxonomyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentTaxonomyUiState())
    val uiState: StateFlow<ContentTaxonomyUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Collect items with filter
            repository.getClassifiedItems(_uiState.value.filter).collect { items ->
                _uiState.update { 
                    it.copy(
                        items = items,
                        filteredItems = items,
                        isLoading = false
                    ) 
                }
            }
        }

        viewModelScope.launch {
            repository.getTaxonomyAuditReport().collect { report ->
                _uiState.update { it.copy(auditReport = report) }
            }
        }
    }

    fun updateFilter(
        originType: ContentOriginType? = null,
        disciplineType: ContentDisciplineType? = null,
        mediaType: ContentMediaType? = null,
        verificationStatus: TaxonomyVerificationStatus? = null,
        rightsStatus: TaxonomyRightsStatus? = null,
        isQuranOnly: Boolean = false,
        isAiOnly: Boolean = false,
        query: String = _uiState.value.filter.query
    ) {
        val newFilter = ContentTaxonomyFilter(
            originType = originType,
            disciplineType = disciplineType,
            mediaType = mediaType,
            verificationStatus = verificationStatus,
            rightsStatus = rightsStatus,
            isQuranOnly = isQuranOnly,
            isAiOnly = isAiOnly,
            query = query
        )
        _uiState.update { it.copy(filter = newFilter) }
        
        viewModelScope.launch {
            repository.getClassifiedItems(newFilter).collect { items ->
                _uiState.update { it.copy(filteredItems = items) }
            }
        }
    }

    fun selectItem(item: ClassifiedContentItem?) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    fun updateTaxonomyMetadata(
        itemId: String,
        newMetadata: ContentTaxonomyMetadata,
        userRole: String = "ADMIN",
        userId: String = "admin_user"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val res = repository.updateTaxonomyMetadata(itemId, newMetadata, userRole, userId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedItem = res.data,
                            successMessage = "تم تحديث التصنيف بنجاح وتطبيق القيود الخادمية"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message ?: "فشل تحديث التصنيف"
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun runSampleLegacyMigration() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMigrating = true, errorMessage = null, successMessage = null) }
            val sampleLegacy = listOf(
                LegacyContentItem(
                    id = "legacy_001",
                    title = "سورة الكهف - قراءة وتدبر",
                    rawCategory = "quran",
                    rawSource = "مصحف المدينة",
                    isQuran = true,
                    isAi = false,
                    ownerId = "system"
                ),
                LegacyContentItem(
                    id = "legacy_002",
                    title = "فوائد الاستغفار في الأثر",
                    rawCategory = "hadith_general",
                    rawSource = "السنن الكبرى",
                    isQuran = false,
                    isAi = false,
                    ownerId = "creator_user_1"
                ),
                LegacyContentItem(
                    id = "legacy_003",
                    title = "خطة محتوى أسبوعي مقترحة للإنتاج",
                    rawCategory = "ai_plan",
                    rawSource = "Gemini API",
                    isQuran = false,
                    isAi = true,
                    ownerId = "creator_user_1"
                )
            )

            when (val res = repository.runLegacyMigration(sampleLegacy)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isMigrating = false,
                            migrationResult = res.data,
                            successMessage = "تم ترحيل ${res.data?.successCount} مادة قديمة إلى التصنيف الموحد بنجاح"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isMigrating = false,
                            errorMessage = res.message ?: "فشل ترحيل البيانات القديمة"
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
