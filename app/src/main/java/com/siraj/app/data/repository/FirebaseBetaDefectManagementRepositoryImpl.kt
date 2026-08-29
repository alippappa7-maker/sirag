package com.siraj.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.domain.models.beta.BetaDefectRecord
import com.siraj.app.domain.models.beta.DefectClassification
import com.siraj.app.domain.models.beta.DefectDomain
import com.siraj.app.domain.models.beta.DefectPriority
import com.siraj.app.domain.models.beta.DefectStatus
import com.siraj.app.domain.models.beta.DefectTriageSummary
import com.siraj.app.domain.repository.BetaDefectManagementRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseBetaDefectManagementRepositoryImpl(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
) : BetaDefectManagementRepository {

    private val defectsCollection get() = firestore?.collection("beta_defects")

    // قائمة العيوب المصنفة الأولية الموثقة لبيئة Beta
    private val initialCatalog = listOf(
        BetaDefectRecord(
            id = "BUG-001",
            title = "تدقيق عزو رقم طبعة التفسير الميسر في شاشة المحراب",
            description = "ملاحظة من لجنة المراجعة الشرعية تفيد بظهور رقم طبعة غير محدث عند عرض حاشية تفسير سورة البقرة، ويجب مطابقة النسخة مع الطبعة المعتمدة لمجمع الملك فهد.",
            classification = DefectClassification.CRITICAL,
            domain = DefectDomain.SHARIA_CONTENT,
            priority = DefectPriority.P0_IMMEDIATE,
            status = DefectStatus.IN_PROGRESS,
            deviceModel = "Google Pixel 8 Pro",
            osVersion = "Android 14 (API 34)",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf(
                "افتح التطبيق وتوجه إلى تبويب المحراب",
                "اختر سورة البقرة، الآية 255",
                "اضغط على بطاقة 'التفسير والمصدر'",
                "لاحظ رقم الطبعة وسنة النشر المسجلة في الحاشية السفلية"
            ),
            expectedResult = "عرض رقم الطبعة المعتمدة رسميًا (طبعة مجمع الملك فهد 1444هـ) مع رابط التوثيق",
            actualResult = "ظهور رقم طبعة عام بدون تحديد المرجع المعتمد بدقة",
            safeLogsOrBreadcrumbs = "[AuditTrail] QuranTafsirSourceLookup -> editionCode mismatch: EXP='KFA-1444' ACT='GENERIC-2020'",
            assignedRole = "هيئة المراجعة والتدقيق الشرعي",
            targetRelease = "1.0.0-beta.2"
        ),
        BetaDefectRecord(
            id = "BUG-002",
            title = "انهيار عند تصدير فيديو مركب بدقة 1080p على أجهزة معالجات Exynos القديمة",
            description = "تعطل معالج الرسوميات أثناء دمج طبقات الصوت والمؤثرات البصرية عند الوصول إلى نسبة 85% في عملية التصدير.",
            classification = DefectClassification.BLOCKER,
            domain = DefectDomain.MEDIA_STUDIO,
            priority = DefectPriority.P0_IMMEDIATE,
            status = DefectStatus.RESOLVED,
            deviceModel = "Samsung Galaxy S21 (Exynos 2100)",
            osVersion = "Android 13 (API 33)",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf(
                "افتح الاستوديو وأنشئ مشروع فيديو جديد",
                "أضف 4 مشاهد مع نص وتلاوة صوتية",
                "اختر دقة التصدير 1080p Full HD",
                "اضغط 'بدء التصدير' وانتظر حتى بلوغ 85%"
            ),
            expectedResult = "اكتمال تصدير الفيديو بنجاح وحفظه في المعرض والمشروع",
            actualResult = "إغلاق مفاجئ لشاشة المعالجة مع خطأ OutOfMemory في طبقة SurfaceTexture",
            safeLogsOrBreadcrumbs = "[MediaMuxer] java.lang.IllegalStateException: BufferQueue producer has been abandoned (Anonymized trace)",
            assignedRole = "مهندس الوسائط والاستوديو",
            targetRelease = "1.0.0-beta.2",
            resolutionNote = "تم ضبط الذاكرة المؤقتة لمحلل الفيديو وتطبيق استهلاك تدريجي لطبقات الإطارات (Frame chunking).",
            verificationTest = "VideoExportPipelineUnitTest#testExynos1080pChunkingStability"
        ),
        BetaDefectRecord(
            id = "BUG-003",
            title = "عدم حفظ عداد الأذكار المقروءة محلياً عند انقطاع الاتصال المفاجئ",
            description = "عند قراءة أذكار الصباح وانقطاع الإنترنت أثناء التسبيح، لا يتم تخزين الحالة في قاعدة Room فورياً.",
            classification = DefectClassification.MAJOR,
            domain = DefectDomain.OFFLINE_SYNC,
            priority = DefectPriority.P1_HIGH,
            status = DefectStatus.RESOLVED,
            deviceModel = "Xiaomi Redmi Note 12",
            osVersion = "Android 13",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf(
                "افتح شاشة الأذكار",
                "قم بتفعيل وضع الطيران (Offline)",
                "اضغط على عداد التسبيح 15 مرة",
                "أغلق التطبيق من الشاشة الرئيسية ثم أعد فتحه في وضع عدم الاتصال"
            ),
            expectedResult = "بقاء العداد عند القيمة 15 واستئناف القراءة",
            actualResult = "تصفير العداد إلى 0 لعدم وجود كتابة متزامنة في قاعدة البيانات المحلية",
            safeLogsOrBreadcrumbs = "[AdhkarTracker] Room transaction delayed waiting for network dispatcher sync",
            assignedRole = "مهندس التخزين المحلي والمزامنة",
            targetRelease = "1.0.0-beta.2",
            resolutionNote = "تم تحويل التخزين إلى نمط Local-First مع كتابة فورية متزامنة في Room قبل محاولة المزامنة السحابية.",
            verificationTest = "AdhkarOfflinePersistenceUnitTest#testImmediateLocalStateSync"
        ),
        BetaDefectRecord(
            id = "BUG-004",
            title = "عدم وجود وصف صوتي دقيق لزر العودة في بطاقة تفاصيل الفلاشات لقارئ TalkBack",
            description = "قارئ الشاشة ينطق 'Unlabelled button' عند الوقوف على زر إغلاق بطاقة الفلاشة السريعة.",
            classification = DefectClassification.MINOR,
            domain = DefectDomain.UI_ACCESSIBILITY,
            priority = DefectPriority.P2_MEDIUM,
            status = DefectStatus.VERIFIED,
            deviceModel = "Samsung Galaxy A54",
            osVersion = "Android 14",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf(
                "قم بتشغيل قارئ الشاشة TalkBack",
                "افتح شاشة الفلاشات التفاعلية",
                "انقر مرتين على أي بطاقة فلاشة للدخول للتفاصيل",
                "اسحب للوصول إلى زر الرجوع العلوي"
            ),
            expectedResult = "نطق: 'رجوع إلى قائمة الفلاشات - زر'",
            actualResult = "نطق: 'زر غير مسمى'",
            safeLogsOrBreadcrumbs = "[AccessibilityNodeInfo] missing contentDescription attribute in FlashDetailTopBar",
            assignedRole = "مهندس واجهات المستخدم وسهولة الوصول",
            targetRelease = "1.0.0-beta.2",
            resolutionNote = "تمت إضافة contentDescription='الرجوع إلى الفلاشات' مع وسم TestTag ومطابقة معايير M3 A11y.",
            verificationTest = "AccessibilityComplianceTest#verifyFlashDetailBackButtonSemantics"
        ),
        BetaDefectRecord(
            id = "BUG-005",
            title = "طلب إضافة مؤثرات انتقال سينمائية مخصصة بين مشاهد الفيديو",
            description = "اقتراح من صانعي المحتوى لإضافة انتقالات تدرج وظلال احترافية لتعزيز جمالية الفيديوهات الدعوية المنتجة.",
            classification = DefectClassification.ENHANCEMENT,
            domain = DefectDomain.MEDIA_STUDIO,
            priority = DefectPriority.P3_LOW,
            status = DefectStatus.DEFERRED,
            deviceModel = "Generic Android / iOS",
            osVersion = "All Platforms",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf("شاشة محرر المشاهد في الاستوديو"),
            expectedResult = "توفر مكتبة انتقالات مخصصة قابلة للاختيار",
            actualResult = "الانتقال الحالي هو قطع فوري (Cut) أو تلاشي افتراضي",
            safeLogsOrBreadcrumbs = "[FeatureRequest] Transition engine enhancement proposal logged in roadmap backlog",
            assignedRole = "فريق تجربة المنتج والتصميم",
            targetRelease = "1.1.0-feature-update",
            closureReason = "تم تأجيل الطلب لما بعد إطلاق النسخة الرسمية 1.0.0 للتركيز التام على استقرار المعالجة الأساسية."
        ),
        BetaDefectRecord(
            id = "BUG-006",
            title = "بطء نسبي في معالجة الفيديو عند دمج 5 مشاهد متتالية",
            description = "إبلاغ مكرر يتطابق في جذوره مع مشكلة استهلاك الذاكرة وتراكم الإطارات الموثقة في BUG-002.",
            classification = DefectClassification.DUPLICATE,
            domain = DefectDomain.MEDIA_STUDIO,
            priority = DefectPriority.P3_LOW,
            status = DefectStatus.CLOSED,
            deviceModel = "Motorola Edge 40",
            osVersion = "Android 14",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf("تصدير أكثر من 5 مشاهد في الاستوديو"),
            expectedResult = "تصدير متسق",
            actualResult = "بطء في شريط التقدم",
            safeLogsOrBreadcrumbs = "[Telemetry] Duplicate stack trace matches BUG-002 frame chunking",
            assignedRole = "مهندس الوسائط والاستوديو",
            targetRelease = "1.0.0-beta.2",
            closureReason = "تذكرة مكررة مطابقة لـ BUG-002، وتم الدمج والمعالجة ضمن الحل الجذري نفسه."
        ),
        BetaDefectRecord(
            id = "BUG-007",
            title = "اختفاء مؤقت لزر تسجيل الدخول عبر Google بعد إعادة تشغيل الهاتف لمرة واحدة",
            description = "بلاغ فردي عن عدم ظهور الزر، وبمحاولة المحاكاة على 20 جهازاً مختلفاً لم يتم تكرار المشكلة إطلاقاً.",
            classification = DefectClassification.NOT_REPRODUCIBLE,
            domain = DefectDomain.AUTH_ACCOUNT,
            priority = DefectPriority.P3_LOW,
            status = DefectStatus.CLOSED,
            deviceModel = "Oppo Reno 8",
            osVersion = "Android 13",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf(
                "إعادة تشغيل الجهاز",
                "فتح التطبيق مباشرة قبل اكتمال تهيئة خدمات Google Play"
            ),
            expectedResult = "ظهور خيارات المصادقة",
            actualResult = "تأخر ظهور الزر لمدة ثانيتين فقط أثناء تحميل Play Services",
            safeLogsOrBreadcrumbs = "[GoogleSignIn] CredentialManager initialized successfully across all automated UI test runs",
            assignedRole = "مهندس المصادقة والأمان",
            targetRelease = "1.0.0-beta.2",
            closureReason = "غير قابل للتكرار، كان ناتجاً عن بطء مؤقت في إقلاع خدمات الهاتف الخارجية ولم يتكرر برمجياً."
        ),
        BetaDefectRecord(
            id = "BUG-008",
            title = "إيقاف عمليات التوليد الآلي للفيديو عند نفاد الرصيد المخصص للمساحة",
            description = "المستخدم ظن أن توقف التوليد خلل، بينما هو تطبيق نظامي لمحددات التكلفة ومبدأ حماية الأرصدة المعتمد.",
            classification = DefectClassification.EXPECTED_BEHAVIOR,
            domain = DefectDomain.BILLING_CREDITS,
            priority = DefectPriority.P3_LOW,
            status = DefectStatus.CLOSED,
            deviceModel = "Google Pixel 7",
            osVersion = "Android 14",
            appVersion = "1.0.0-beta.1",
            buildCode = 100,
            stepsToReproduce = listOf(
                "استهلاك كامل رصيد العمليات المجانية اليومية",
                "محاولة طلب توليد سيناريو جديد"
            ),
            expectedResult = "إظهار تنبيه وصول الحد اليومي وتوجيه المستخدم لإدارة الرصيد",
            actualResult = "ظهور بطاقة تنبيه استنفاد الرصيد ومنع الطلب الخادمي",
            safeLogsOrBreadcrumbs = "[QuotaGuard] RateLimit & Balance Policy applied: 402 Payment Required / Quota Exceeded",
            assignedRole = "مسؤول الاشتراكات والفوترة",
            targetRelease = "1.0.0-beta.1",
            closureReason = "سلوك متوقع ومطابق لسياسة سراج لمنع الاستهلاك غير المنضبط وحماية موارد الخدمة."
        )
    )

    // ذاكرة محلية متزامنة
    private val inMemoryDefects = initialCatalog.toMutableList()

    override fun getAllDefects(): Flow<List<BetaDefectRecord>> = callbackFlow {
        // إرسال البيانات الفورية
        trySend(inMemoryDefects.toList())

        val collection = defectsCollection
        if (collection == null) {
            awaitClose { }
            return@callbackFlow
        }

        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null && !snapshot.isEmpty) {
                val remoteList = snapshot.documents.mapNotNull { doc ->
                    try {
                        mapDocumentToDefect(doc.data ?: emptyMap(), doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                // دمج مع الكتالوج الأولي
                val merged = (remoteList + inMemoryDefects).distinctBy { it.id }
                trySend(merged)
            } else {
                trySend(inMemoryDefects.toList())
            }
        }

        awaitClose { subscription.remove() }
    }

    override fun getDefectById(id: String): Flow<BetaDefectRecord?> {
        return getAllDefects().map { list -> list.find { it.id == id } }
    }

    override suspend fun triageDefect(
        id: String,
        classification: DefectClassification,
        priority: DefectPriority,
        assignedRole: String,
        targetRelease: String
    ): Result<Unit> {
        return try {
            val index = inMemoryDefects.indexOfFirst { it.id == id }
            if (index != -1) {
                val current = inMemoryDefects[index]
                
                // القاعدة الإلزامية: أي محتوى ديني/شرعي لا يجوز أن يصنف أقل من CRITICAL
                val safeClassification = if (current.domain == DefectDomain.SHARIA_CONTENT &&
                    (classification == DefectClassification.MINOR || classification == DefectClassification.ENHANCEMENT)
                ) {
                    DefectClassification.CRITICAL
                } else {
                    classification
                }

                val safePriority = if (safeClassification == DefectClassification.BLOCKER || safeClassification == DefectClassification.CRITICAL) {
                    if (priority == DefectPriority.P3_LOW) DefectPriority.P1_HIGH else priority
                } else {
                    priority
                }

                val updated = current.copy(
                    classification = safeClassification,
                    priority = safePriority,
                    assignedRole = assignedRole.ifBlank { current.assignedRole },
                    targetRelease = targetRelease.ifBlank { current.targetRelease },
                    status = if (current.status == DefectStatus.REPORTED) DefectStatus.TRIAGED else current.status,
                    updatedAt = System.currentTimeMillis()
                )
                inMemoryDefects[index] = updated

                // حفظ في Firestore إن أمكن
                try {
                    defectsCollection?.document(id)?.set(mapDefectToMap(updated))?.await()
                } catch (_: Exception) { }

                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("العيب غير موجود"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDefectStatus(
        id: String,
        newStatus: DefectStatus,
        resolutionNote: String?,
        closureReason: String?,
        verificationTest: String?
    ): Result<Unit> {
        return try {
            val index = inMemoryDefects.indexOfFirst { it.id == id }
            if (index != -1) {
                val current = inMemoryDefects[index]

                // القواعد الإلزامية:
                // 1. لا يغلق عيب بدون ذكر سبب واضح
                if (newStatus == DefectStatus.CLOSED && closureReason.isNullOrBlank()) {
                    return Result.failure(IllegalStateException("لا يمكن إغلاق العيب دون تقديم سبب الإغلاق والتبرير الفني."))
                }

                if (newStatus == DefectStatus.DEFERRED && closureReason.isNullOrBlank()) {
                    return Result.failure(IllegalStateException("يجب توثيق سبب تأجيل العيب للإصدار القادم."))
                }

                // 2. التحقق من حل المشكلات الحرجة بربطها باختبار تحقق
                if (newStatus == DefectStatus.RESOLVED && (current.classification == DefectClassification.BLOCKER || current.classification == DefectClassification.CRITICAL)) {
                    if (resolutionNote.isNullOrBlank()) {
                        return Result.failure(IllegalStateException("الأعطال الحرجة والمانعة للإطلاق (Blocker / Critical) تتطلب توثيق تفاصيل الحل الفني بدقة."))
                    }
                }

                val updated = current.copy(
                    status = newStatus,
                    resolutionNote = resolutionNote ?: current.resolutionNote,
                    closureReason = closureReason ?: current.closureReason,
                    verificationTest = verificationTest ?: current.verificationTest,
                    updatedAt = System.currentTimeMillis()
                )
                inMemoryDefects[index] = updated

                try {
                    defectsCollection?.document(id)?.set(mapDefectToMap(updated))?.await()
                } catch (_: Exception) { }

                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("العيب غير موجود"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPrioritizedFixList(): Flow<List<BetaDefectRecord>> {
        return getAllDefects().map { list ->
            // الترتيب: P0 Blockers أولاً -> P1 Critical -> P2 Major -> P3 Minor -> Enhancements
            list.filter { it.status != DefectStatus.CLOSED && it.status != DefectStatus.DEFERRED }
                .sortedWith(
                    compareBy<BetaDefectRecord> { it.priority.orderWeight }
                        .thenBy {
                            when (it.classification) {
                                DefectClassification.BLOCKER -> 0
                                DefectClassification.CRITICAL -> 1
                                DefectClassification.MAJOR -> 2
                                DefectClassification.MINOR -> 3
                                DefectClassification.ENHANCEMENT -> 4
                                else -> 5
                            }
                        }
                        .thenByDescending { it.updatedAt }
                )
        }
    }

    override fun getDeferredDefectsList(): Flow<List<BetaDefectRecord>> {
        return getAllDefects().map { list ->
            list.filter { it.status == DefectStatus.DEFERRED || it.classification == DefectClassification.ENHANCEMENT }
        }
    }

    override fun getTriageSummary(): Flow<DefectTriageSummary> {
        return getAllDefects().map { list ->
            DefectTriageSummary(
                totalCount = list.size,
                blockerCount = list.count { it.classification == DefectClassification.BLOCKER },
                criticalCount = list.count { it.classification == DefectClassification.CRITICAL },
                majorCount = list.count { it.classification == DefectClassification.MAJOR },
                minorCount = list.count { it.classification == DefectClassification.MINOR },
                enhancementCount = list.count { it.classification == DefectClassification.ENHANCEMENT },
                duplicateCount = list.count { it.classification == DefectClassification.DUPLICATE },
                notReproducibleCount = list.count { it.classification == DefectClassification.NOT_REPRODUCIBLE },
                expectedBehaviorCount = list.count { it.classification == DefectClassification.EXPECTED_BEHAVIOR },
                openOrInProgressCount = list.count { it.status == DefectStatus.REPORTED || it.status == DefectStatus.TRIAGED || it.status == DefectStatus.IN_PROGRESS },
                resolvedOrVerifiedCount = list.count { it.status == DefectStatus.RESOLVED || it.status == DefectStatus.VERIFIED },
                deferredCount = list.count { it.status == DefectStatus.DEFERRED },
                closedCount = list.count { it.status == DefectStatus.CLOSED },
                shariaDomainCount = list.count { it.domain == DefectDomain.SHARIA_CONTENT }
            )
        }
    }

    override suspend fun registerDefect(defect: BetaDefectRecord): Result<Unit> {
        return try {
            // التحقق من تصنيف المحتوى الشرعي
            val finalDefect = if (defect.domain == DefectDomain.SHARIA_CONTENT &&
                (defect.classification == DefectClassification.MINOR || defect.classification == DefectClassification.ENHANCEMENT)
            ) {
                defect.copy(classification = DefectClassification.CRITICAL, priority = DefectPriority.P0_IMMEDIATE)
            } else {
                defect
            }

            inMemoryDefects.add(0, finalDefect)
            try {
                defectsCollection?.document(finalDefect.id)?.set(mapDefectToMap(finalDefect))?.await()
            } catch (_: Exception) { }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDefectToMap(defect: BetaDefectRecord): Map<String, Any?> {
        return mapOf(
            "id" to defect.id,
            "title" to defect.title,
            "description" to defect.description,
            "classification" to defect.classification.name,
            "domain" to defect.domain.name,
            "priority" to defect.priority.name,
            "status" to defect.status.name,
            "deviceModel" to defect.deviceModel,
            "osVersion" to defect.osVersion,
            "appVersion" to defect.appVersion,
            "buildCode" to defect.buildCode,
            "stepsToReproduce" to defect.stepsToReproduce,
            "expectedResult" to defect.expectedResult,
            "actualResult" to defect.actualResult,
            "safeLogsOrBreadcrumbs" to defect.safeLogsOrBreadcrumbs,
            "assignedRole" to defect.assignedRole,
            "targetRelease" to defect.targetRelease,
            "resolutionNote" to defect.resolutionNote,
            "closureReason" to defect.closureReason,
            "verificationTest" to defect.verificationTest,
            "reportedAt" to defect.reportedAt,
            "updatedAt" to defect.updatedAt
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapDocumentToDefect(data: Map<String, Any?>, docId: String): BetaDefectRecord {
        return BetaDefectRecord(
            id = data["id"] as? String ?: docId,
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            classification = try {
                DefectClassification.valueOf(data["classification"] as? String ?: "MINOR")
            } catch (_: Exception) { DefectClassification.MINOR },
            domain = try {
                DefectDomain.valueOf(data["domain"] as? String ?: "UI_ACCESSIBILITY")
            } catch (_: Exception) { DefectDomain.UI_ACCESSIBILITY },
            priority = try {
                DefectPriority.valueOf(data["priority"] as? String ?: "P2_MEDIUM")
            } catch (_: Exception) { DefectPriority.P2_MEDIUM },
            status = try {
                DefectStatus.valueOf(data["status"] as? String ?: "REPORTED")
            } catch (_: Exception) { DefectStatus.REPORTED },
            deviceModel = data["deviceModel"] as? String ?: "",
            osVersion = data["osVersion"] as? String ?: "",
            appVersion = data["appVersion"] as? String ?: "1.0.0-beta.1",
            buildCode = (data["buildCode"] as? Number)?.toInt() ?: 100,
            stepsToReproduce = (data["stepsToReproduce"] as? List<String>) ?: emptyList(),
            expectedResult = data["expectedResult"] as? String ?: "",
            actualResult = data["actualResult"] as? String ?: "",
            safeLogsOrBreadcrumbs = data["safeLogsOrBreadcrumbs"] as? String ?: "",
            assignedRole = data["assignedRole"] as? String ?: "",
            targetRelease = data["targetRelease"] as? String ?: "1.0.0-beta.2",
            resolutionNote = data["resolutionNote"] as? String,
            closureReason = data["closureReason"] as? String,
            verificationTest = data["verificationTest"] as? String,
            reportedAt = (data["reportedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
