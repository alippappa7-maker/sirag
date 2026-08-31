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
    private val itemsFlow = MutableStateFlow<List<ClassifiedContentItem>>(generateInitialDataset())

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

    private fun generateInitialDataset(): List<ClassifiedContentItem> =
        listOf(
            ClassifiedContentItem(
                id = "item_quran_1",
                title = "سورة الفاتحة - الرسم العثماني",
                contentSnippet = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ ...",
                metadata =
                    ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                        originType = ContentOriginType.SYSTEM_CONTENT,
                        disciplineType = ContentDisciplineType.QURAN_TEXT,
                        mediaType = ContentMediaType.TEXT,
                        authorType = AuthorType.SYSTEM,
                        generationMethod = GenerationMethod.IMPORTED_DATASET,
                        verificationStatus = TaxonomyVerificationStatus.SHARIA_VERIFIED,
                        rightsStatus = TaxonomyRightsStatus.PUBLIC_DOMAIN,
                        visibility = TaxonomyVisibility.PUBLIC_APPROVED,
                        ownerId = "system_quran",
                        sourceTitle = "مصحف مجمع الملك فهد لطباعة المصحف الشريف",
                        sourceUrl = "https://qurancomplex.gov.sa",
                        sourceReference = "سورة رقم 1، مصحف المدينة",
                        authorOrScholarName = "مجمع الملك فهد",
                    ),
            ),
            ClassifiedContentItem(
                id = "item_tafsir_1",
                title = "تفسير آية: {وَقُل رَّبِّ زِدْنِي عِلْمًا}",
                contentSnippet = "أي زدني فهماً وعلماً بكتابك وسنة نبيك صلى الله عليه وسلم، كما بين الإمام ابن كثير رحمه الله...",
                metadata =
                    ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                        originType = ContentOriginType.EDITORIAL_CONTENT,
                        disciplineType = ContentDisciplineType.TAFSIR,
                        mediaType = ContentMediaType.TEXT,
                        authorType = AuthorType.SCHOLAR_EDITOR,
                        generationMethod = GenerationMethod.MANUAL_HUMAN,
                        verificationStatus = TaxonomyVerificationStatus.SHARIA_VERIFIED,
                        rightsStatus = TaxonomyRightsStatus.SIRAJ_ORIGINAL,
                        visibility = TaxonomyVisibility.PUBLIC_APPROVED,
                        ownerId = "editor_sharia_1",
                        reviewerId = "rev_sharia_board",
                        sourceTitle = "تفسير القرآن العظيم (ابن كثير)",
                        sourceUrl = "https://shamela.ws/book/23604/3512",
                        sourceReference = "سورة طه: 114، ج5 ص322",
                        authorOrScholarName = "الحافظ ابن كثير",
                    ),
            ),
            ClassifiedContentItem(
                id = "item_hadith_1",
                title = "شرح حديث: «إنما الأعمال بالنيات»",
                contentSnippet = "حديث عظيم يعتبر ثلث الإسلام ومدار قبول الأعمال على إخلاص النية لله تعالى...",
                metadata =
                    ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                        originType = ContentOriginType.EDITORIAL_CONTENT,
                        disciplineType = ContentDisciplineType.HADITH,
                        mediaType = ContentMediaType.TEXT,
                        authorType = AuthorType.SCHOLAR_EDITOR,
                        generationMethod = GenerationMethod.MANUAL_HUMAN,
                        verificationStatus = TaxonomyVerificationStatus.SHARIA_VERIFIED,
                        rightsStatus = TaxonomyRightsStatus.SIRAJ_ORIGINAL,
                        visibility = TaxonomyVisibility.PUBLIC_APPROVED,
                        ownerId = "editor_sharia_1",
                        reviewerId = "rev_sharia_board",
                        sourceTitle = "صحيح البخاري وصحيح مسلم",
                        sourceReference = "البخاري رقم 1، مسلم رقم 1907",
                        authorOrScholarName = "عمر بن الخطاب رضي الله عنه",
                    ),
            ),
            ClassifiedContentItem(
                id = "item_ai_draft_1",
                title = "مسودة سيناريو مقترح: أهمية بر الوالدين",
                contentSnippet = "مشهد 1: لقطة للأب يتكئ على عصاه، يرافقه تعليق صوتي يذكر بفضل الإحسان...",
                metadata =
                    ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                        originType = ContentOriginType.AI_GENERATED,
                        disciplineType = ContentDisciplineType.EDUCATIONAL,
                        mediaType = ContentMediaType.VIDEO,
                        authorType = AuthorType.AI_ASSISTANT,
                        generationMethod = GenerationMethod.AI_GENERATED,
                        verificationStatus = TaxonomyVerificationStatus.PENDING_REVIEW,
                        rightsStatus = TaxonomyRightsStatus.SIRAJ_ORIGINAL,
                        visibility = TaxonomyVisibility.PRIVATE,
                        ownerId = "creator_user_10",
                        sourceTitle = "توليد مساعد سراج الذكي (Gemini API via Backend)",
                        authorOrScholarName = "مساعد سراج للإنتاج",
                    ),
            ),
            ClassifiedContentItem(
                id = "item_ugc_1",
                title = "تجربتي في حفظ جزء عم مع أولادي",
                contentSnippet = "جدول مقترح للآباء والأمهات لتقسيم السور القصيرة ومراجعتها يومياً بعد صلاة العصر...",
                metadata =
                    ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                        originType = ContentOriginType.USER_GENERATED,
                        disciplineType = ContentDisciplineType.GENERAL,
                        mediaType = ContentMediaType.TEXT,
                        authorType = AuthorType.CREATOR,
                        generationMethod = GenerationMethod.MANUAL_HUMAN,
                        verificationStatus = TaxonomyVerificationStatus.UNVERIFIED,
                        rightsStatus = TaxonomyRightsStatus.SIRAJ_ORIGINAL,
                        visibility = TaxonomyVisibility.WORKSPACE_ONLY,
                        ownerId = "creator_user_22",
                        sourceTitle = "مشاركة شخصية لصانع المحتوى",
                    ),
            ),
            ClassifiedContentItem(
                id = "item_external_audio_1",
                title = "تلاوة سورة الضحى - رواية حفص عن عاصم",
                contentSnippet = "تسجيل صوتي استوديو عالي النقاوة برخصة مشاع إبداعي متاح للإنتاج الدعوي...",
                metadata =
                    ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                        originType = ContentOriginType.LICENSED_EXTERNAL,
                        disciplineType = ContentDisciplineType.QURAN_TEXT,
                        mediaType = ContentMediaType.AUDIO,
                        authorType = AuthorType.THIRD_PARTY_CREATOR,
                        generationMethod = GenerationMethod.MANUAL_HUMAN,
                        verificationStatus = TaxonomyVerificationStatus.SHARIA_VERIFIED,
                        rightsStatus = TaxonomyRightsStatus.LICENSED_CC,
                        visibility = TaxonomyVisibility.PUBLIC_APPROVED,
                        ownerId = "admin_siraj",
                        sourceTitle = "مشروع المصحف الصوتي المفتوح",
                        sourceUrl = "https://everyayah.com",
                        sourceReference = "تلاوة موثقة برخصة CC-BY-SA",
                        authorOrScholarName = "القارئ الشيخ عبد الباسط عبد الصمد رحمه الله",
                        licenseAttributionText = "مرخص بموجب رخصة المشاع الإبداعي (EveryAyah Audio Project)",
                    ),
            ),
        )
}
