package com.siraj.app.features.taxonomy.domain

import com.siraj.app.domain.models.taxonomy.*

/**
 * المحرك المركزي لإدارة وتطبيق قواعد تصنيف المحتوى ومصدره (PROMPT 088)
 */
object ContentTaxonomyEngine {
    /**
     * التحقق الخادمي الصارم من تصنيف المحتوى وضبط قواعد الأمان والمسارات
     */
    fun validateAndEnforceTaxonomy(
        originType: ContentOriginType,
        disciplineType: ContentDisciplineType,
        mediaType: ContentMediaType,
        authorType: AuthorType,
        generationMethod: GenerationMethod,
        verificationStatus: TaxonomyVerificationStatus,
        rightsStatus: TaxonomyRightsStatus,
        visibility: TaxonomyVisibility,
        ownerId: String,
        reviewerId: String? = null,
        versionId: String = "v1",
        sourceId: String? = null,
        sourceTitle: String? = null,
        sourceUrl: String? = null,
        sourceReference: String? = null,
        authorOrScholarName: String? = null,
        licenseAttributionText: String? = null,
        clientReportedCategory: String? = null,
    ): ContentTaxonomyMetadata {
        // 1. قاعدة النص القرآني (Quran_text): مقفل بالكامل، مستورد من مصحف معتمد، لا يعدل أبداً
        val isQuran = disciplineType == ContentDisciplineType.QURAN_TEXT
        if (isQuran) {
            val quranRights = TaxonomyRightsStatus.PUBLIC_DOMAIN
            val quranGeneration = GenerationMethod.IMPORTED_DATASET
            val quranAuthor = AuthorType.SYSTEM
            val quranVerification = TaxonomyVerificationStatus.SHARIA_VERIFIED
            val quranPath = ReviewPipelinePath.LOCKED_IMMUTABLE_PASSTHROUGH

            return ContentTaxonomyMetadata(
                originType = ContentOriginType.SYSTEM_CONTENT,
                disciplineType = ContentDisciplineType.QURAN_TEXT,
                mediaType = mediaType,
                authorType = quranAuthor,
                generationMethod = quranGeneration,
                verificationStatus = quranVerification,
                rightsStatus = quranRights,
                visibility = TaxonomyVisibility.PUBLIC_APPROVED,
                ownerId = "system_quran",
                reviewerId = reviewerId ?: "sharia_board_verified",
                versionId = versionId,
                sourceId = sourceId ?: "quran_complex_madinah",
                sourceTitle = sourceTitle ?: "مصحف مجمع الملك فهد لطباعة المصحف الشريف",
                sourceUrl = sourceUrl ?: "https://qurancomplex.gov.sa",
                sourceReference = sourceReference ?: "الرسم العثماني",
                authorOrScholarName = "مصحف المدينة المنورة",
                licenseAttributionText = "النص القرآني بالرسم العثماني الموثق - وقف لله تعالى",
                isLockedImmutable = true, // مقفل لا يقبل التعديل إطلاقاً
                isQuranicText = true,
                isAiAssisted = false,
                reviewPipelinePath = quranPath,
                allowedRolesToEdit = emptyList(), // لا أحد يستطيع تعديل القرآن
                clientReportedCategory = clientReportedCategory,
                serverValidatedAt = System.currentTimeMillis(),
            )
        }

        // 2. قاعدة المحتوى المرخص الخارجي (licensed_external): لا يعتبر ملكاً لسراج، ويلزم الترخيص والعزو
        var validatedRights = rightsStatus
        var validatedVisibility = visibility
        if (originType == ContentOriginType.LICENSED_EXTERNAL) {
            if (validatedRights == TaxonomyRightsStatus.SIRAJ_ORIGINAL) {
                // تصحيح فوري: لا يجوز ادعاء ملكية سراج لمحتوى خارجي
                validatedRights = TaxonomyRightsStatus.LICENSED_CC
            }
            if (validatedRights == TaxonomyRightsStatus.UNKNOWN) {
                // منع النشر العام إن كان الترخيص مجهولاً
                validatedVisibility = TaxonomyVisibility.RESTRICTED_SUSPENDED
            }
        }

        // 3. قاعدة المحتوى المولد بالذكاء الاصطناعي (ai_generated)
        val isAi = originType == ContentOriginType.AI_GENERATED || generationMethod == GenerationMethod.AI_GENERATED
        var validatedVerification = verificationStatus
        if (isAi) {
            // المحتوى المولد بالذكاء الاصطناعي لا يساوى أبداً بالمحتوى الموثق شرعياً تلقائياً
            if (validatedVerification == TaxonomyVerificationStatus.SHARIA_VERIFIED && reviewerId == null) {
                validatedVerification = TaxonomyVerificationStatus.PENDING_REVIEW
            }
        }

        // 4. قاعدة محتوى المستخدمين (user_generated): لا يظهر كتحرير رسمي
        var validatedOrigin = originType
        var validatedAuthor = authorType
        if (authorType == AuthorType.CREATOR && originType == ContentOriginType.EDITORIAL_CONTENT) {
            validatedOrigin = ContentOriginType.USER_GENERATED
        }
        if (originType == ContentOriginType.USER_GENERATED && authorType == AuthorType.SCHOLAR_EDITOR && reviewerId == null) {
            validatedAuthor = AuthorType.CREATOR
        }

        // 5. تحديد مسار المراجعة (Review Pipeline Path)
        val pipelinePath =
            determineReviewPipeline(
                origin = validatedOrigin,
                discipline = disciplineType,
                generation = generationMethod,
                verification = validatedVerification,
            )

        // 6. تحديد صلاحيات التعديل
        val allowedRoles =
            determineAllowedRolesToEdit(
                origin = validatedOrigin,
                discipline = disciplineType,
                isLocked = false,
            )

        return ContentTaxonomyMetadata(
            originType = validatedOrigin,
            disciplineType = disciplineType,
            mediaType = mediaType,
            authorType = validatedAuthor,
            generationMethod =
                if (isAi &&
                    generationMethod == GenerationMethod.MANUAL_HUMAN
                ) {
                    GenerationMethod.AI_GENERATED
                } else {
                    generationMethod
                },
            verificationStatus = validatedVerification,
            rightsStatus = validatedRights,
            visibility = validatedVisibility,
            ownerId = ownerId,
            reviewerId = reviewerId,
            versionId = versionId,
            sourceId = sourceId,
            sourceTitle = sourceTitle,
            sourceUrl = sourceUrl,
            sourceReference = sourceReference,
            authorOrScholarName = authorOrScholarName,
            licenseAttributionText = licenseAttributionText ?: generateDefaultAttribution(validatedRights, validatedOrigin),
            isLockedImmutable = false,
            isQuranicText = false,
            isAiAssisted = isAi,
            reviewPipelinePath = pipelinePath,
            allowedRolesToEdit = allowedRoles,
            clientReportedCategory = clientReportedCategory,
            serverValidatedAt = System.currentTimeMillis(),
        )
    }

    /**
     * تحديد مسار المراجعة المناسب بناءً على التصنيف
     */
    fun determineReviewPipeline(
        origin: ContentOriginType,
        discipline: ContentDisciplineType,
        generation: GenerationMethod,
        verification: TaxonomyVerificationStatus,
    ): ReviewPipelinePath =
        when {
            discipline == ContentDisciplineType.QURAN_TEXT -> ReviewPipelinePath.LOCKED_IMMUTABLE_PASSTHROUGH
            discipline == ContentDisciplineType.HADITH ||
                discipline == ContentDisciplineType.FIQH ||
                discipline == ContentDisciplineType.TAFSIR -> {
                ReviewPipelinePath.SHARIA_SCHOLAR_MANDATORY
            }
            origin == ContentOriginType.EDITORIAL_CONTENT -> ReviewPipelinePath.EDITORIAL_STANDARD
            origin == ContentOriginType.LICENSED_EXTERNAL || generation == GenerationMethod.AI_GENERATED -> {
                ReviewPipelinePath.RIGHTS_AND_SAFETY_SCAN
            }
            else -> ReviewPipelinePath.COMMUNITY_MODERATION
        }

    /**
     * تحديد الأدوار المسموح لها بالتعديل
     */
    private fun determineAllowedRolesToEdit(
        origin: ContentOriginType,
        discipline: ContentDisciplineType,
        isLocked: Boolean,
    ): List<String> {
        if (isLocked || discipline == ContentDisciplineType.QURAN_TEXT) {
            return emptyList() // مقفل
        }
        return when (origin) {
            ContentOriginType.SYSTEM_CONTENT -> listOf("ADMIN", "OWNER")
            ContentOriginType.EDITORIAL_CONTENT -> listOf("SCHOLAR_EDITOR", "REVIEWER", "ADMIN")
            ContentOriginType.USER_GENERATED -> listOf("CREATOR", "OWNER")
            ContentOriginType.AI_GENERATED -> listOf("CREATOR", "SCHOLAR_EDITOR", "ADMIN")
            ContentOriginType.LICENSED_EXTERNAL -> listOf("ADMIN", "OWNER")
        }
    }

    /**
     * فحص قابلية التعديل للمستخدم المعين
     */
    fun canEditContent(
        item: ClassifiedContentItem,
        userRole: String,
        userId: String,
    ): Pair<Boolean, String> {
        if (item.metadata.isLockedImmutable || item.metadata.isQuranicText) {
            return Pair(false, "النص القرآني ومحتوى النظام المحمي مقفل وغير قابل للتعديل حفاظاً على الأمانة والنزاهة.")
        }
        if (item.metadata.allowedRolesToEdit.contains(userRole)) {
            if (item.metadata.originType == ContentOriginType.USER_GENERATED &&
                item.metadata.ownerId != userId &&
                userRole != "ADMIN" &&
                userRole != "OWNER"
            ) {
                return Pair(false, "لا تملك صلاحية تعديل محتوى مستخدم آخر.")
            }
            return Pair(true, "مسموح بالتعديل وفق الدور والصلاحية.")
        }
        return Pair(false, "دورك ($userRole) غير مخول بتعديل هذا الصنف من المحتوى (${item.metadata.originType.titleArabic}).")
    }

    /**
     * توليد نص العزو التلقائي للحقوق
     */
    private fun generateDefaultAttribution(
        rights: TaxonomyRightsStatus,
        origin: ContentOriginType,
    ): String =
        when (rights) {
            TaxonomyRightsStatus.PUBLIC_DOMAIN -> "تراث إسلامي / ملك عام"
            TaxonomyRightsStatus.SIRAJ_ORIGINAL -> "منصة سراج - جميع الحقوق محفوظة"
            TaxonomyRightsStatus.LICENSED_CC -> "مرخص بموجب Creative Commons - يلزم ذكر المصدر"
            TaxonomyRightsStatus.LICENSED_COMMERCIAL -> "مرخص بموجب اتفاقية ترخيص تجاري معتمدة"
            TaxonomyRightsStatus.RESTRICTED -> "محتوى خاص ومقيد الاستخدام"
            TaxonomyRightsStatus.UNKNOWN -> "غير محدد - غير مصرح بالنشر العام"
        }

    /**
     * إجراء فحص وتدقيق للمحتوى وإنتاج تقرير التصنيف الشامل
     */
    fun auditContentTaxonomy(items: List<ClassifiedContentItem>): TaxonomyAuditReport {
        val total = items.size
        var unclassifiedCount = 0
        var quranCount = 0
        var aiCount = 0
        var ugcCount = 0
        var editorialCount = 0
        var licensedCount = 0
        var missingRightsCount = 0
        val unclassifiedIds = mutableListOf<String>()

        items.forEach { item ->
            val meta = item.metadata
            if (meta.disciplineType == ContentDisciplineType.QURAN_TEXT && meta.isLockedImmutable) {
                quranCount++
            }
            if (meta.originType == ContentOriginType.AI_GENERATED || meta.isAiAssisted) {
                aiCount++
            }
            if (meta.originType == ContentOriginType.USER_GENERATED) {
                ugcCount++
            }
            if (meta.originType == ContentOriginType.EDITORIAL_CONTENT) {
                editorialCount++
            }
            if (meta.originType == ContentOriginType.LICENSED_EXTERNAL) {
                licensedCount++
            }
            if (meta.rightsStatus == TaxonomyRightsStatus.UNKNOWN) {
                missingRightsCount++
            }

            // فحص هل العنصر مصنف بشكل سليم
            val isUnclassified =
                meta.originType == ContentOriginType.SYSTEM_CONTENT &&
                    meta.sourceTitle.isNullOrBlank() &&
                    !meta.isQuranicText &&
                    meta.ownerId.isBlank()
            if (isUnclassified) {
                unclassifiedCount++
                unclassifiedIds.add(item.id)
            }
        }

        val classifiedCount = total - unclassifiedCount
        val compliancePercentage = if (total > 0) (classifiedCount.toFloat() / total) * 100f else 100f

        val summary = "تدقيق التصنيف: $classifiedCount من أصل $total مادة مصنفة بنجاح ($compliancePercentage%). يتضمن $quranCount مادة قرآنية مقفلة، و $aiCount مادة بمساعدة الذكاء الاصطناعي، و $missingRightsCount مادة بحاجة لتحديد الترخيص."

        return TaxonomyAuditReport(
            totalItemsCount = total,
            classifiedItemsCount = classifiedCount,
            unclassifiedItemsCount = unclassifiedCount,
            quranTextLockedCount = quranCount,
            aiGeneratedItemsCount = aiCount,
            userGeneratedItemsCount = ugcCount,
            editorialItemsCount = editorialCount,
            licensedExternalCount = licensedCount,
            rightsMissingCount = missingRightsCount,
            compliancePercentage = compliancePercentage,
            unclassifiedItemIds = unclassifiedIds,
            auditSummary = summary,
            auditedAt = System.currentTimeMillis(),
        )
    }

    /**
     * ترحيل البيانات القديمة إلى التصنيف الجديد (Legacy Migration)
     */
    fun migrateLegacyContent(legacyItems: List<LegacyContentItem>): TaxonomyMigrationResult {
        val migrated = mutableListOf<ClassifiedContentItem>()
        val logs = mutableListOf<String>()
        var success = 0
        var failed = 0

        legacyItems.forEach { legacy ->
            try {
                val origin =
                    when {
                        legacy.isQuran -> ContentOriginType.SYSTEM_CONTENT
                        legacy.isAi -> ContentOriginType.AI_GENERATED
                        legacy.rawCategory?.contains("editorial", ignoreCase = true) == true -> ContentOriginType.EDITORIAL_CONTENT
                        legacy.rawCategory?.contains("external", ignoreCase = true) == true -> ContentOriginType.LICENSED_EXTERNAL
                        else -> ContentOriginType.USER_GENERATED
                    }

                val discipline =
                    when {
                        legacy.isQuran -> ContentDisciplineType.QURAN_TEXT
                        legacy.rawCategory?.contains("tafsir", ignoreCase = true) == true -> ContentDisciplineType.TAFSIR
                        legacy.rawCategory?.contains("hadith", ignoreCase = true) == true -> ContentDisciplineType.HADITH
                        legacy.rawCategory?.contains("fiqh", ignoreCase = true) == true -> ContentDisciplineType.FIQH
                        legacy.rawCategory?.contains("edu", ignoreCase = true) == true -> ContentDisciplineType.EDUCATIONAL
                        else -> ContentDisciplineType.GENERAL
                    }

                val generation =
                    when {
                        legacy.isQuran -> GenerationMethod.IMPORTED_DATASET
                        legacy.isAi -> GenerationMethod.AI_GENERATED
                        else -> GenerationMethod.MANUAL_HUMAN
                    }

                val rights =
                    when {
                        legacy.isQuran -> TaxonomyRightsStatus.PUBLIC_DOMAIN
                        origin == ContentOriginType.EDITORIAL_CONTENT -> TaxonomyRightsStatus.SIRAJ_ORIGINAL
                        origin == ContentOriginType.LICENSED_EXTERNAL -> TaxonomyRightsStatus.LICENSED_CC
                        else -> TaxonomyRightsStatus.SIRAJ_ORIGINAL
                    }

                val metadata =
                    validateAndEnforceTaxonomy(
                        originType = origin,
                        disciplineType = discipline,
                        mediaType = ContentMediaType.TEXT,
                        authorType =
                            if (legacy.isQuran) {
                                AuthorType.SYSTEM
                            } else if (legacy.isAi) {
                                AuthorType.AI_ASSISTANT
                            } else {
                                AuthorType.CREATOR
                            },
                        generationMethod = generation,
                        verificationStatus = if (legacy.isQuran) TaxonomyVerificationStatus.SHARIA_VERIFIED else TaxonomyVerificationStatus.UNVERIFIED,
                        rightsStatus = rights,
                        visibility = if (legacy.isQuran) TaxonomyVisibility.PUBLIC_APPROVED else TaxonomyVisibility.PRIVATE,
                        ownerId = legacy.ownerId,
                        sourceTitle = legacy.rawSource ?: (if (legacy.isQuran) "المصحف الشريف" else "مصدر غير مسجل"),
                        clientReportedCategory = legacy.rawCategory,
                    )

                val classifiedItem =
                    ClassifiedContentItem(
                        id = legacy.id,
                        title = legacy.title,
                        contentSnippet = "تم الترحيل من البنية السابقة: ${legacy.title}",
                        metadata = metadata,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )

                migrated.add(classifiedItem)
                logs.add("نجاح ترحيل المادة ${legacy.id} إلى تصنيف: ${origin.titleArabic} / ${discipline.titleArabic}")
                success++
            } catch (e: Exception) {
                logs.add("فشل ترحيل المادة ${legacy.id}: ${e.message}")
                failed++
            }
        }

        return TaxonomyMigrationResult(
            totalMigrated = legacyItems.size,
            successCount = success,
            failedCount = failed,
            migratedItems = migrated,
            migrationLog = logs,
            completedAt = System.currentTimeMillis(),
        )
    }
}
