package com.siraj.app.domain.repository.taxonomy

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.taxonomy.*
import kotlinx.coroutines.flow.Flow

data class ContentTaxonomyFilter(
    val originType: ContentOriginType? = null,
    val disciplineType: ContentDisciplineType? = null,
    val mediaType: ContentMediaType? = null,
    val verificationStatus: TaxonomyVerificationStatus? = null,
    val rightsStatus: TaxonomyRightsStatus? = null,
    val isQuranOnly: Boolean = false,
    val isAiOnly: Boolean = false,
    val query: String = ""
)

interface ContentTaxonomyRepository {
    fun getClassifiedItems(filter: ContentTaxonomyFilter): Flow<List<ClassifiedContentItem>>
    fun getItemById(id: String): Flow<ClassifiedContentItem?>
    suspend fun saveClassifiedItem(item: ClassifiedContentItem): Resource<ClassifiedContentItem>
    suspend fun updateTaxonomyMetadata(
        id: String,
        newMetadata: ContentTaxonomyMetadata,
        userRole: String,
        userId: String
    ): Resource<ClassifiedContentItem>
    fun getTaxonomyAuditReport(): Flow<TaxonomyAuditReport>
    suspend fun runLegacyMigration(items: List<LegacyContentItem>): Resource<TaxonomyMigrationResult>
}
