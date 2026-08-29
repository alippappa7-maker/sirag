package com.siraj.app.data.repository.beta

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.siraj.app.core.monitoring.CrashMonitoringManager
import com.siraj.app.domain.models.beta.*
import com.siraj.app.domain.repository.BetaTesterDistributionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseBetaTesterDistributionRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : BetaTesterDistributionRepository {

    private val testersCollection = firestore.collection("beta_testers")
    private val surveysCollection = firestore.collection("beta_experience_surveys")

    override fun getTesterProfile(userId: String): Flow<BetaTesterProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = testersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val profile = BetaTesterProfile(
                            testerId = snapshot.getString("testerId") ?: snapshot.id,
                            email = snapshot.getString("email") ?: "",
                            name = snapshot.getString("name") ?: "",
                            group = snapshot.getString("group")?.let { enumValueOf<TesterGroup>(it) } ?: TesterGroup.COMMUNITY_BETA,
                            status = snapshot.getString("status")?.let { enumValueOf<TesterStatus>(it) } ?: TesterStatus.ACTIVE,
                            platform = snapshot.getString("platform") ?: "Android",
                            deviceModel = snapshot.getString("deviceModel") ?: "",
                            osVersion = snapshot.getString("osVersion") ?: "",
                            installedAppVersion = snapshot.getString("installedAppVersion") ?: "",
                            installedBuildCode = snapshot.getLong("installedBuildCode")?.toInt() ?: 1,
                            invitedAt = snapshot.getLong("invitedAt") ?: 0L,
                            firstLaunchAt = snapshot.getLong("firstLaunchAt") ?: 0L,
                            lastActiveAt = snapshot.getLong("lastActiveAt") ?: 0L,
                            completedJourneys = (snapshot.get("completedJourneys") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            completedSurveysCount = snapshot.getLong("completedSurveysCount")?.toInt() ?: 0,
                            submittedFeedbacksCount = snapshot.getLong("submittedFeedbacksCount")?.toInt() ?: 0
                        )
                        trySend(profile)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun registerTesterSession(
        testerId: String,
        email: String,
        name: String,
        deviceModel: String,
        osVersion: String,
        appVersion: String,
        buildCode: Int
    ): Result<BetaTesterProfile> {
        return try {
            val docRef = testersCollection.document(testerId)
            val snapshot = docRef.get().await()

            val now = System.currentTimeMillis()
            val initialProfile = if (snapshot.exists()) {
                val groupStr = snapshot.getString("group")
                val group = if (groupStr != null) enumValueOf<TesterGroup>(groupStr) else TesterGroup.COMMUNITY_BETA
                val statusStr = snapshot.getString("status")
                val status = if (statusStr != null) enumValueOf<TesterStatus>(statusStr) else TesterStatus.ACTIVE

                BetaTesterProfile(
                    testerId = testerId,
                    email = email.ifBlank { snapshot.getString("email") ?: "" },
                    name = name.ifBlank { snapshot.getString("name") ?: "" },
                    group = group,
                    status = status,
                    platform = "Android",
                    deviceModel = deviceModel,
                    osVersion = osVersion,
                    installedAppVersion = appVersion,
                    installedBuildCode = buildCode,
                    invitedAt = snapshot.getLong("invitedAt") ?: now,
                    firstLaunchAt = snapshot.getLong("firstLaunchAt") ?: now,
                    lastActiveAt = now,
                    completedJourneys = (snapshot.get("completedJourneys") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                    completedSurveysCount = snapshot.getLong("completedSurveysCount")?.toInt() ?: 0,
                    submittedFeedbacksCount = snapshot.getLong("submittedFeedbacksCount")?.toInt() ?: 0
                )
            } else {
                BetaTesterProfile(
                    testerId = testerId,
                    email = email,
                    name = name,
                    group = TesterGroup.COMMUNITY_BETA,
                    status = TesterStatus.ACTIVE,
                    platform = "Android",
                    deviceModel = deviceModel,
                    osVersion = osVersion,
                    installedAppVersion = appVersion,
                    installedBuildCode = buildCode,
                    invitedAt = now,
                    firstLaunchAt = now,
                    lastActiveAt = now,
                    completedJourneys = emptyList(),
                    completedSurveysCount = 0,
                    submittedFeedbacksCount = 0
                )
            }

            val dataMap = hashMapOf<String, Any>(
                "testerId" to initialProfile.testerId,
                "email" to initialProfile.email,
                "name" to initialProfile.name,
                "group" to initialProfile.group.name,
                "status" to initialProfile.status.name,
                "platform" to initialProfile.platform,
                "deviceModel" to initialProfile.deviceModel,
                "osVersion" to initialProfile.osVersion,
                "installedAppVersion" to initialProfile.installedAppVersion,
                "installedBuildCode" to initialProfile.installedBuildCode,
                "invitedAt" to initialProfile.invitedAt,
                "firstLaunchAt" to (initialProfile.firstLaunchAt ?: now),
                "lastActiveAt" to now,
                "completedJourneys" to initialProfile.completedJourneys,
                "completedSurveysCount" to initialProfile.completedSurveysCount,
                "submittedFeedbacksCount" to initialProfile.submittedFeedbacksCount
            )

            docRef.set(dataMap).await()
            CrashMonitoringManager.logBreadcrumb("Tester Session Registered: ${initialProfile.email} (Group: ${initialProfile.group.name}) on ${initialProfile.deviceModel}")

            Result.success(initialProfile)
        } catch (e: Exception) {
            CrashMonitoringManager.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun recordJourneyCompletion(testerId: String, journeyId: String): Result<Unit> {
        return try {
            val docRef = testersCollection.document(testerId)
            docRef.update(
                "completedJourneys", FieldValue.arrayUnion(journeyId),
                "lastActiveAt", System.currentTimeMillis()
            ).await()
            CrashMonitoringManager.logBreadcrumb("Tester $testerId completed journey: $journeyId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitExperienceSurvey(survey: TesterExperienceSurvey): Result<String> {
        return try {
            val surveyId = if (survey.id.isNotBlank()) survey.id else "survey_${UUID.randomUUID()}"
            val surveyToSave = survey.copy(
                id = surveyId,
                timestamp = if (survey.timestamp > 0) survey.timestamp else System.currentTimeMillis()
            )

            val surveyMap = hashMapOf<String, Any>(
                "id" to surveyToSave.id,
                "testerId" to surveyToSave.testerId,
                "testerEmail" to surveyToSave.testerEmail,
                "overallRating" to surveyToSave.overallRating,
                "easeOfUseRating" to surveyToSave.easeOfUseRating,
                "shariaContentRating" to surveyToSave.shariaContentRating,
                "performanceRating" to surveyToSave.performanceRating,
                "mostValuableFeature" to surveyToSave.mostValuableFeature,
                "biggestPainPoint" to surveyToSave.biggestPainPoint,
                "generalSuggestions" to surveyToSave.generalSuggestions,
                "deviceModel" to surveyToSave.deviceModel,
                "appVersion" to surveyToSave.appVersion,
                "timestamp" to surveyToSave.timestamp
            )

            surveysCollection.document(surveyId).set(surveyMap).await()

            // Increment survey count in tester document
            if (surveyToSave.testerId.isNotBlank()) {
                testersCollection.document(surveyToSave.testerId)
                    .update("completedSurveysCount", FieldValue.increment(1))
                    .await()
            }

            CrashMonitoringManager.logBreadcrumb("Tester Survey Submitted: Rating ${surveyToSave.overallRating}/5 by ${surveyToSave.testerEmail}")
            Result.success(surveyId)
        } catch (e: Exception) {
            CrashMonitoringManager.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun revokeTesterAccess(testerId: String, reason: String): Result<Unit> {
        return try {
            testersCollection.document(testerId).update(
                "status", TesterStatus.REVOKED.name,
                "revokedReason", reason,
                "revokedAt", System.currentTimeMillis()
            ).await()
            CrashMonitoringManager.logBreadcrumb("Tester Access Revoked for $testerId - Reason: $reason")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReleaseNotes(): List<BetaReleaseNote> {
        return listOf(
            BetaReleaseNote(
                versionName = "1.0.0-beta.1",
                buildCode = 1,
                releaseDate = "أغسطس 2026",
                platform = "Android / iOS",
                channel = "Firebase App Distribution & Play Closed Track",
                highlights = listOf(
                    "المحراب والمصحف الشريف: تلاوات صوتية، مواقيت الصلاة، اتجاه القبلة، والتقويم الهجري",
                    "توليد الفكرة والسيناريو: محرك توليد الأفكار وضبط زوايا المحتوى الإسلامي",
                    "نظام المراجعة والتدقيق الشرعي: التحقق من الآيات والأحاديث والمصادر الموثقة",
                    "استوديو المشاهد والصوت: المعلق الصوتي والمؤثرات وتنسيق الشاشة",
                    "نظام الملاحظات والتشخيص التلقائي للأعطال"
                ),
                fixedIssues = listOf(
                    "عزل بيئة التجربة (Staging) عن الإنتاج وتعيين الحزمة المستقلة com.siraj.app.beta",
                    "ضبط توافق Google Services وإدارة الحسابات التجريبية",
                    "تحسين أداء عرض خطوط المصحف الشريف ودعم قراءة الشاشة"
                ),
                knownLimitations = listOf(
                    "توليد الفيديو عالي الدقة يتم خادمياً ويخضع لحدود الاستخدام التجريبي",
                    "الاشتراكات تعمل في وضع Sandbox التجريبي فقط ولا تخصم أي مبالغ حقيقية"
                ),
                targetGroups = listOf(
                    TesterGroup.INTERNAL_TEAM,
                    TesterGroup.SHARIA_REVIEWERS,
                    TesterGroup.CONTENT_CREATORS,
                    TesterGroup.ACCESSIBILITY_QA,
                    TesterGroup.COMMUNITY_BETA
                )
            )
        )
    }

    override fun getDistributionChannels(): List<DistributionChannelInfo> {
        return listOf(
            DistributionChannelInfo(
                platform = "Android",
                channelName = "Firebase App Distribution",
                methodTitle = "تطبيق App Tester المعتمد",
                stepGuide = listOf(
                    "تحقق من وصول رسالة الدعوة إلى بريدك الإلكتروني المعتمد من Firebase App Distribution.",
                    "قم بتثبيت تطبيق 'App Tester' الرسمي من متجر Google Play.",
                    "سجّل الدخول إلى App Tester باستخدام نفس البريد الإلكتروني الذي تلقى الدعوة.",
                    "انقر على تطبيق 'سراج (Beta)' ثم اضغط 'Download' لتحميل وتثبيت ملف APK.",
                    "عند توفر أي تحديث أو إصلاحات جديدة، ستظهر لك إشعارات تلقائية في App Tester لتحديث التطبيق بنقرة واحدة."
                ),
                updateInstructions = "يقوم تطبيق App Tester بإشعارك فور رفع أي إصدار بيتا جديد دون الحاجة لإعادة التثبيت اليدوي.",
                supportNote = "في حال واجهتك مشكلة في التثبيت، تأكد من تفعيل صلاحية 'تثبيت التطبيقات غير المعروفة' لتطبيق App Tester."
            ),
            DistributionChannelInfo(
                platform = "Android",
                channelName = "Google Play Closed Testing",
                methodTitle = "المسار المغلق لمتجر Google Play",
                stepGuide = listOf(
                    "تأكد من إضافة بريدك الإلكتروني إلى قائمة المختبرين المغلقة (Closed Testing Track).",
                    "افتح رابط الانضمام لبرنامج الاختبار عبر الويب واضغط على 'Become a Tester'.",
                    "افتح صفحة سراج على متجر Google Play وقم بتثبيت النسخة التجريبية مباشرة.",
                    "تصل التحديثات تلقائياً عبر متجر Google Play مع بقية التطبيقات."
                ),
                updateInstructions = "تحديث تلقائي عبر متجر Google Play مع إمكانية تقديم الملاحظات للمطورين مباشرة من المتجر.",
                supportNote = "تستغرق المراجعة الأولى للحساب بضع دقائق قبل ظهور زر التثبيت في متجر Play."
            ),
            DistributionChannelInfo(
                platform = "iOS",
                channelName = "Apple TestFlight",
                methodTitle = "منصة TestFlight الرسمية من Apple",
                stepGuide = listOf(
                    "قم بتثبيت تطبيق TestFlight من App Store على جهاز الآيفون أو الآيباد.",
                    "افتح رسالة الدعوة الإلكترونية واضغط على زر 'Start Testing' أو استخدم رمز الاسترداد (Redemption Code).",
                    "اضغط على 'Install' لتثبيت نسخة سراج التجريبية.",
                    "يمكنك إرسال لقطات شاشة وملاحظات مباشرة عبر الضغط المتزامن على زري التشغيل ومستوى الصوت ثم اختيار 'Share Beta Feedback'."
                ),
                updateInstructions = "يقوم TestFlight بإشعارك تلقائياً عند اعتماد ونشر كل بناء (Build) جديد.",
                supportNote = "صلاحية كل بناء في TestFlight تدوم حتى 90 يوماً وتتجدد مع كل إصدار جديد."
            )
        )
    }

    override fun getCriticalJourneys(): List<CriticalJourney> {
        return listOf(
            CriticalJourney(
                id = "ideation",
                title = "توليد فكرة ومسودة سيناريو",
                description = "استكشاف مساعد الإنتاج الذكي، صياغة زوايا المحتوى، وتوليد الفكرة الأولية",
                targetRoute = "ideation",
                iconName = "AutoAwesome"
            ),
            CriticalJourney(
                id = "sharia_sources",
                title = "ربط المصادر والتحقق الشرعي",
                description = "توثيق الآيات والأحاديث والمراجع المعتمدة وفحص حالة الاعتماد",
                targetRoute = "sharia_review_queue",
                iconName = "Verified"
            ),
            CriticalJourney(
                id = "quran_mihrab",
                title = "المصحف الشريف وقسم المحراب",
                description = "تلاوة القرآن، الاستماع للأصوات، الأذكار، مواقيت الصلاة واتجاه القبلة",
                targetRoute = "mihrab",
                iconName = "MenuBook"
            ),
            CriticalJourney(
                id = "video_studio",
                title = "استوديو المشاهد والصوتيات",
                description = "ترتيب المشاهد، تجربة التعليق الصوتي، والمؤثرات المرئية والصوتية",
                targetRoute = "studio",
                iconName = "MovieFilter"
            ),
            CriticalJourney(
                id = "export_share",
                title = "تصدير الفيديو والنشر التجريبي",
                description = "اختبار التصدير النهائي، تحميل الفيديو، والمشاركة أو النشر في ومضات سراج",
                targetRoute = "flashes",
                iconName = "Share"
            )
        )
    }
}
