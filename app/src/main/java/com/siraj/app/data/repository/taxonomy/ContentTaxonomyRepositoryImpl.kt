package com.siraj.app.data.repository.taxonomy

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.taxonomy.*
import com.siraj.app.domain.repository.taxonomy.ContentTaxonomyFilter
import com.siraj.app.domain.repository.taxonomy.ContentTaxonomyRepository
import com.siraj.app.features.taxonomy.domain.ContentTaxonomyEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ContentTaxonomyRepositoryImpl : ContentTaxonomyRepository {
    private val itemsFlow = MutableStateFlow<List<ClassifiedContentItem>>(emptyList())

    override fun getClassifiedItems(filter: ContentTaxonomyFilter): Flow<List<ClassifiedContentItem>> =
        itemsFlow.map { list ->
            list.filter { item ->
                val meta = item.metadata
                val matchesOrigin = filter.originType == null || meta.originType == filter.originType
                val matchesDiscipline = filter.disciplineType == null || meta.disciplineType == filter.disciplineType
                val matchesMedia = filter.mediaType == null || meta.mediaType == filter.mediaType
                val matchesVerification = filter.verificationStatus == null || meta.verificationStatus == filter.verificationStatus
                val matchesRights = filter.rightsStatus == null || meta.rightsStatus == filter.rightsStatus
                val matchesQuran = !filter.isQuranOnly || meta.isQuranicText
                val matchesAi = !filter.isAiOnly || meta.isAiAssisted
                val matchesQuery =
                    filter.query.isBlank() ||
                        item.title.contains(filter.query, ignoreCase = true) ||
                        item.contentSnippet.contains(filter.query, ignoreCase = true) ||
                        (meta.sourceTitle?.contains(filter.query, ignoreCase = true) == true)

                matchesOrigin &&
                    matchesDiscipline &&
                    matchesMedia &&
                    matchesVerification &&
                    matchesRights &&
                    matchesQuran &&
                    matchesAi &&
                    matchesQuery
            }
        }

    override fun getItemById(id: String): Flow<ClassifiedContentItem?> = itemsFlow.map { list -> list.find { it.id == id } }

    override suspend fun saveClassifiedItem(item: ClassifiedContentItem): Resource<ClassifiedContentItem> {
        val validatedMeta =
            ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                originType = item.metadata.originType,
                disciplineType = item.metadata.disciplineType,
                mediaType = item.metadata.mediaType,
                authorType = item.metadata.authorType,
                generationMethod = item.metadata.generationMethod,
                verificationStatus = item.metadata.verificationStatus,
                rightsStatus = item.metadata.rightsStatus,
                visibility = item.metadata.visibility,
                ownerId = item.metadata.ownerId,
                reviewerId = item.metadata.reviewerId,
                versionId = item.metadata.versionId,
                sourceId = item.metadata.sourceId,
                sourceTitle = item.metadata.sourceTitle,
                sourceUrl = item.metadata.sourceUrl,
                sourceReference = item.metadata.sourceReference,
                authorOrScholarName = item.metadata.authorOrScholarName,
                licenseAttributionText = item.metadata.licenseAttributionText,
                clientReportedCategory = item.metadata.clientReportedCategory,
            )

        val finalized = item.copy(metadata = validatedMeta, updatedAt = System.currentTimeMillis())
        val current = itemsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current[index] = finalized
        } else {
            current.add(0, finalized)
        }
        itemsFlow.value = current
        return Resource.Success(finalized)
    }

    override suspend fun updateTaxonomyMetadata(
        id: String,
        newMetadata: ContentTaxonomyMetadata,
        userRole: String,
        userId: String,
    ): Resource<ClassifiedContentItem> {
        val current = itemsFlow.value.toMutableList()
        val item = current.find { it.id == id } ?: return Resource.Error("المادة غير موجودة")

        val (canEdit, reason) = ContentTaxonomyEngine.canEditContent(item, userRole, userId)
        if (!canEdit) {
            return Resource.Error(reason)
        }

        val validatedMeta =
            ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                originType = newMetadata.originType,
                disciplineType = newMetadata.disciplineType,
                mediaType = newMetadata.mediaType,
                authorType = newMetadata.authorType,
                generationMethod = newMetadata.generationMethod,
                verificationStatus = newMetadata.verificationStatus,
                rightsStatus = newMetadata.rightsStatus,
                visibility = newMetadata.visibility,
                ownerId = newMetadata.ownerId,
                reviewerId = newMetadata.reviewerId,
                versionId = newMetadata.versionId,
                sourceId = newMetadata.sourceId,
                sourceTitle = newMetadata.sourceTitle,
                sourceUrl = newMetadata.sourceUrl,
                sourceReference = newMetadata.sourceReference,
                authorOrScholarName = newMetadata.authorOrScholarName,
                licenseAttributionText = newMetadata.licenseAttributionText,
                clientReportedCategory = newMetadata.clientReportedCategory,
            )

        val updated = item.copy(metadata = validatedMeta, updatedAt = System.currentTimeMillis())
        val index = current.indexOfFirst { it.id == id }
        current[index] = updated
        itemsFlow.value = current

        return Resource.Success(updated)
    }

    override fun getTaxonomyAuditReport(): Flow<TaxonomyAuditReport> =
        itemsFlow.map { list -> ContentTaxonomyEngine.auditContentTaxonomy(list) }

    override suspend fun runLegacyMigration(items: List<LegacyContentItem>): Resource<TaxonomyMigrationResult> {
        val result = ContentTaxonomyEngine.migrateLegacyContent(items)
        val current = itemsFlow.value.toMutableList()
        result.migratedItems.forEach { migrated ->
            val idx = current.indexOfFirst { it.id == migrated.id }
            if (idx >= 0) {
                current[idx] = migrated
            } else {
                current.add(migrated)
            }
        }
        itemsFlow.value = current
        return Resource.Success(result)
    }
}
