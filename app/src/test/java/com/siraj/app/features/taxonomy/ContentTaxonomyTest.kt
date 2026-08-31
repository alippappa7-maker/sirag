package com.siraj.app.features.taxonomy

import com.siraj.app.core.utils.Resource
import com.siraj.app.data.repository.taxonomy.ContentTaxonomyRepositoryImpl
import com.siraj.app.domain.models.taxonomy.*
import com.siraj.app.domain.repository.taxonomy.ContentTaxonomyFilter
import com.siraj.app.features.taxonomy.domain.ContentTaxonomyEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ContentTaxonomyTest {
    @Test
    fun testQuranText_isStrictlyLockedAndImmutableAndImportedDataset() {
        val quranMeta =
            ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                originType = ContentOriginType.SYSTEM_CONTENT,
                disciplineType = ContentDisciplineType.QURAN_TEXT,
                mediaType = ContentMediaType.TEXT,
                authorType = AuthorType.CREATOR, // Even if client passed creator
                generationMethod = GenerationMethod.AI_GENERATED, // Even if client passed AI
                verificationStatus = TaxonomyVerificationStatus.UNVERIFIED,
                rightsStatus = TaxonomyRightsStatus.UNKNOWN,
                visibility = TaxonomyVisibility.PRIVATE,
                ownerId = "user_1",
            )

        // 1. Must be locked and immutable
        assertTrue(quranMeta.isLockedImmutable)
        assertTrue(quranMeta.isQuranicText)
        assertEquals(emptyList<String>(), quranMeta.allowedRolesToEdit)

        // 2. Must be imported dataset and system author
        assertEquals(GenerationMethod.IMPORTED_DATASET, quranMeta.generationMethod)
        assertEquals(AuthorType.SYSTEM, quranMeta.authorType)
        assertEquals(TaxonomyRightsStatus.PUBLIC_DOMAIN, quranMeta.rightsStatus)
        assertEquals(TaxonomyVerificationStatus.SHARIA_VERIFIED, quranMeta.verificationStatus)
        assertEquals(ReviewPipelinePath.LOCKED_IMMUTABLE_PASSTHROUGH, quranMeta.reviewPipelinePath)

        // 3. Mutability test: nobody can edit Quran text
        val quranItem =
            ClassifiedContentItem(
                id = "quran_1",
                title = "سورة الفاتحة",
                contentSnippet = "...",
                metadata = quranMeta,
            )
        val (canEditAdmin, reasonAdmin) = ContentTaxonomyEngine.canEditContent(quranItem, "ADMIN", "admin_1")
        assertFalse(canEditAdmin)
        assertTrue(reasonAdmin.contains("مقفل وغير قابل للتعديل"))

        val (canEditOwner, _) = ContentTaxonomyEngine.canEditContent(quranItem, "OWNER", "owner_1")
        assertFalse(canEditOwner)
    }

    @Test
    fun testAiGenerated_neverEquatedToVerifiedEditorial() {
        val aiMeta =
            ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                originType = ContentOriginType.AI_GENERATED,
                disciplineType = ContentDisciplineType.EDUCATIONAL,
                mediaType = ContentMediaType.VIDEO,
                authorType = AuthorType.AI_ASSISTANT,
                generationMethod = GenerationMethod.AI_GENERATED,
                verificationStatus = TaxonomyVerificationStatus.SHARIA_VERIFIED, // Attempt to mark verified without reviewer
                rightsStatus = TaxonomyRightsStatus.SIRAJ_ORIGINAL,
                visibility = TaxonomyVisibility.PRIVATE,
                ownerId = "creator_1",
                reviewerId = null, // No reviewer
            )

        // AI generated cannot be auto-verified without a human reviewer
        assertTrue(aiMeta.isAiAssisted)
        assertEquals(TaxonomyVerificationStatus.PENDING_REVIEW, aiMeta.verificationStatus)
        assertEquals(ReviewPipelinePath.RIGHTS_AND_SAFETY_SCAN, aiMeta.reviewPipelinePath)
    }

    @Test
    fun testLicensedExternal_neverOwnedBySiraj_andUnknownRightsRestricted() {
        // Attempting to claim SIRAJ_ORIGINAL on external content
        val externalMeta =
            ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                originType = ContentOriginType.LICENSED_EXTERNAL,
                disciplineType = ContentDisciplineType.GENERAL,
                mediaType = ContentMediaType.AUDIO,
                authorType = AuthorType.THIRD_PARTY_CREATOR,
                generationMethod = GenerationMethod.MANUAL_HUMAN,
                verificationStatus = TaxonomyVerificationStatus.UNVERIFIED,
                rightsStatus = TaxonomyRightsStatus.SIRAJ_ORIGINAL, // Cannot claim original
                visibility = TaxonomyVisibility.PUBLIC_APPROVED,
                ownerId = "creator_1",
            )

        assertEquals(TaxonomyRightsStatus.LICENSED_CC, externalMeta.rightsStatus)

        // Unknown rights must be suspended/restricted
        val unknownRightsMeta =
            ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                originType = ContentOriginType.LICENSED_EXTERNAL,
                disciplineType = ContentDisciplineType.GENERAL,
                mediaType = ContentMediaType.IMAGE,
                authorType = AuthorType.THIRD_PARTY_CREATOR,
                generationMethod = GenerationMethod.MANUAL_HUMAN,
                verificationStatus = TaxonomyVerificationStatus.UNVERIFIED,
                rightsStatus = TaxonomyRightsStatus.UNKNOWN,
                visibility = TaxonomyVisibility.PUBLIC_APPROVED,
                ownerId = "creator_1",
            )

        assertEquals(TaxonomyVisibility.RESTRICTED_SUSPENDED, unknownRightsMeta.visibility)
    }

    @Test
    fun testUserGeneratedContent_neverAppearsAsOfficialEditorial() {
        val ugcMeta =
            ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                originType = ContentOriginType.EDITORIAL_CONTENT, // Attempt to mark UGC as editorial
                disciplineType = ContentDisciplineType.GENERAL,
                mediaType = ContentMediaType.TEXT,
                authorType = AuthorType.CREATOR,
                generationMethod = GenerationMethod.MANUAL_HUMAN,
                verificationStatus = TaxonomyVerificationStatus.UNVERIFIED,
                rightsStatus = TaxonomyRightsStatus.SIRAJ_ORIGINAL,
                visibility = TaxonomyVisibility.WORKSPACE_ONLY,
                ownerId = "creator_user_10",
            )

        // Corrected to UGC
        assertEquals(ContentOriginType.USER_GENERATED, ugcMeta.originType)
        assertEquals(ReviewPipelinePath.COMMUNITY_MODERATION, ugcMeta.reviewPipelinePath)

        val ugcItem =
            ClassifiedContentItem(
                id = "ugc_1",
                title = "مقال تجربة",
                contentSnippet = "...",
                metadata = ugcMeta,
            )

        // Another creator cannot edit this user's content
        val (canOtherEdit, reason) = ContentTaxonomyEngine.canEditContent(ugcItem, "CREATOR", "other_creator_99")
        assertFalse(canOtherEdit)
        assertTrue(reason.contains("لا تملك صلاحية تعديل محتوى مستخدم آخر"))

        // Original creator can edit
        val (canOwnerEdit, _) = ContentTaxonomyEngine.canEditContent(ugcItem, "CREATOR", "creator_user_10")
        assertTrue(canOwnerEdit)
    }

    @Test
    fun testReviewPipelineRouting_forAllDisciplines() {
        val quranPath =
            ContentTaxonomyEngine.determineReviewPipeline(
                ContentOriginType.SYSTEM_CONTENT,
                ContentDisciplineType.QURAN_TEXT,
                GenerationMethod.IMPORTED_DATASET,
                TaxonomyVerificationStatus.SHARIA_VERIFIED,
            )
        assertEquals(ReviewPipelinePath.LOCKED_IMMUTABLE_PASSTHROUGH, quranPath)

        val hadithPath =
            ContentTaxonomyEngine.determineReviewPipeline(
                ContentOriginType.EDITORIAL_CONTENT,
                ContentDisciplineType.HADITH,
                GenerationMethod.MANUAL_HUMAN,
                TaxonomyVerificationStatus.SHARIA_VERIFIED,
            )
        assertEquals(ReviewPipelinePath.SHARIA_SCHOLAR_MANDATORY, hadithPath)

        val tafsirPath =
            ContentTaxonomyEngine.determineReviewPipeline(
                ContentOriginType.EDITORIAL_CONTENT,
                ContentDisciplineType.TAFSIR,
                GenerationMethod.MANUAL_HUMAN,
                TaxonomyVerificationStatus.SHARIA_VERIFIED,
            )
        assertEquals(ReviewPipelinePath.SHARIA_SCHOLAR_MANDATORY, tafsirPath)

        val editorialPath =
            ContentTaxonomyEngine.determineReviewPipeline(
                ContentOriginType.EDITORIAL_CONTENT,
                ContentDisciplineType.GENERAL,
                GenerationMethod.MANUAL_HUMAN,
                TaxonomyVerificationStatus.EDITORIAL_VERIFIED,
            )
        assertEquals(ReviewPipelinePath.EDITORIAL_STANDARD, editorialPath)

        val externalPath =
            ContentTaxonomyEngine.determineReviewPipeline(
                ContentOriginType.LICENSED_EXTERNAL,
                ContentDisciplineType.GENERAL,
                GenerationMethod.MANUAL_HUMAN,
                TaxonomyVerificationStatus.UNVERIFIED,
            )
        assertEquals(ReviewPipelinePath.RIGHTS_AND_SAFETY_SCAN, externalPath)
    }

    @Test
    fun testTaxonomyAuditReport_calculatesAccurately() {
        val items =
            listOf(
                ClassifiedContentItem(
                    id = "item_1",
                    title = "قرآن",
                    contentSnippet = "...",
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
                            ownerId = "system",
                        ),
                ),
                ClassifiedContentItem(
                    id = "item_2",
                    title = "AI Script",
                    contentSnippet = "...",
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
                            ownerId = "user_1",
                        ),
                ),
                ClassifiedContentItem(
                    id = "item_3",
                    title = "External Audio",
                    contentSnippet = "...",
                    metadata =
                        ContentTaxonomyEngine.validateAndEnforceTaxonomy(
                            originType = ContentOriginType.LICENSED_EXTERNAL,
                            disciplineType = ContentDisciplineType.GENERAL,
                            mediaType = ContentMediaType.AUDIO,
                            authorType = AuthorType.THIRD_PARTY_CREATOR,
                            generationMethod = GenerationMethod.MANUAL_HUMAN,
                            verificationStatus = TaxonomyVerificationStatus.UNVERIFIED,
                            rightsStatus = TaxonomyRightsStatus.UNKNOWN,
                            visibility = TaxonomyVisibility.RESTRICTED_SUSPENDED,
                            ownerId = "user_1",
                        ),
                ),
            )

        val report = ContentTaxonomyEngine.auditContentTaxonomy(items)
        assertEquals(3, report.totalItemsCount)
        assertEquals(1, report.quranTextLockedCount)
        assertEquals(1, report.aiGeneratedItemsCount)
        assertEquals(1, report.licensedExternalCount)
        assertEquals(1, report.rightsMissingCount)
        assertTrue(report.compliancePercentage > 0f)
    }

    @Test
    fun testLegacyMigration_migratesLegacyCategoriesSuccessfully() {
        val legacyList =
            listOf(
                LegacyContentItem("leg_1", "سورة الإخلاص", "quran", "المصحف", isQuran = true, isAi = false, ownerId = "system"),
                LegacyContentItem(
                    "leg_2",
                    "تفسير سورة الفلق",
                    "tafsir",
                    "تفسير الطبري",
                    isQuran = false,
                    isAi = false,
                    ownerId = "editor_1",
                ),
                LegacyContentItem("leg_3", "مسودة ذكية", "ai_draft", "Gemini", isQuran = false, isAi = true, ownerId = "creator_1"),
            )

        val result = ContentTaxonomyEngine.migrateLegacyContent(legacyList)
        assertEquals(3, result.totalMigrated)
        assertEquals(3, result.successCount)
        assertEquals(0, result.failedCount)

        val migratedQuran = result.migratedItems.find { it.id == "leg_1" }!!
        assertTrue(migratedQuran.metadata.isQuranicText)
        assertTrue(migratedQuran.metadata.isLockedImmutable)
        assertEquals(ContentDisciplineType.QURAN_TEXT, migratedQuran.metadata.disciplineType)

        val migratedTafsir = result.migratedItems.find { it.id == "leg_2" }!!
        assertEquals(ContentDisciplineType.TAFSIR, migratedTafsir.metadata.disciplineType)

        val migratedAi = result.migratedItems.find { it.id == "leg_3" }!!
        assertTrue(migratedAi.metadata.isAiAssisted)
        assertEquals(ContentOriginType.AI_GENERATED, migratedAi.metadata.originType)
    }

    @Test
    fun testRepositoryIntegration_andFilterOperations() =
        runTest {
            val repo = ContentTaxonomyRepositoryImpl()

            // 1. Initial items
            val allItems = repo.getClassifiedItems(ContentTaxonomyFilter()).first()
            assertTrue(allItems.isNotEmpty())

            // 2. Filter Quran only
            val quranItems = repo.getClassifiedItems(ContentTaxonomyFilter(disciplineType = ContentDisciplineType.QURAN_TEXT)).first()
            assertTrue(quranItems.all { it.metadata.disciplineType == ContentDisciplineType.QURAN_TEXT })

            // 3. Filter AI only
            val aiItems = repo.getClassifiedItems(ContentTaxonomyFilter(isAiOnly = true)).first()
            assertTrue(aiItems.all { it.metadata.isAiAssisted })

            // 4. Update taxonomy with permission check
            val tafsirItem = allItems.find { it.metadata.disciplineType == ContentDisciplineType.TAFSIR }!!
            val updateRes =
                repo.updateTaxonomyMetadata(
                    id = tafsirItem.id,
                    newMetadata = tafsirItem.metadata.copy(sourceReference = "ج3 ص150"),
                    userRole = "ADMIN",
                    userId = "admin_1",
                )
            assertTrue(updateRes is Resource.Success)
            val successData = (updateRes as Resource.Success).data
            assertEquals("ج3 ص150", successData.metadata.sourceReference)
        }
}
