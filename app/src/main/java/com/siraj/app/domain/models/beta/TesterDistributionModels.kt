package com.siraj.app.domain.models.beta

enum class TesterGroup(val id: String, val title: String, val description: String) {
    INTERNAL_TEAM("internal_team", "الفريق الداخلي والتطوير", "فريق العمل والمطورون والمختبرون الداخليون"),
    SHARIA_REVIEWERS("sharia_reviewers", "المراجعون والباحثون الشرعيون", "هيئة المراجعة والتدقيق والتحقق من صحة وتوثيق النصوص"),
    CONTENT_CREATORS("content_creators", "صناع المحتوى والدعاة", "صناع الفيديوهات القصيرة، كتاب السيناريو، والمعلقون"),
    ACCESSIBILITY_QA("accessibility_qa", "مختبرو الوصول وتجربة المستخدم", "مختبرو سهولة الاستخدام وقارئات الشاشة والخطوط"),
    COMMUNITY_BETA("community_beta", "مختبرو المجتمع الموثوقون", "عينة مختارة من المستخدمين لتجربة الاستخدام الفعلي اليومي")
}

enum class TesterStatus(val title: String, val colorHex: Long) {
    INVITED("تمت الدعوة", 0xFF2196F3),
    ACTIVE("نشط ومعتمد", 0xFF4CAF50),
    SUSPENDED("موقوف مؤقتاً", 0xFFFF9800),
    REVOKED("مسحوبة الصلاحية", 0xFFF44336)
}

data class BetaTesterProfile(
    val testerId: String = "",
    val email: String = "",
    val name: String = "",
    val group: TesterGroup = TesterGroup.COMMUNITY_BETA,
    val status: TesterStatus = TesterStatus.ACTIVE,
    val platform: String = "Android",
    val deviceModel: String = "",
    val osVersion: String = "",
    val installedAppVersion: String = "",
    val installedBuildCode: Int = 1,
    val invitedAt: Long = 0L,
    val firstLaunchAt: Long = 0L,
    val lastActiveAt: Long = 0L,
    val completedJourneys: List<String> = emptyList(),
    val completedSurveysCount: Int = 0,
    val submittedFeedbacksCount: Int = 0
)

data class CriticalJourney(
    val id: String,
    val title: String,
    val description: String,
    val targetRoute: String,
    val iconName: String
)

data class TesterExperienceSurvey(
    val id: String = "",
    val testerId: String = "",
    val testerEmail: String = "",
    val overallRating: Int = 5,
    val easeOfUseRating: Int = 5,
    val shariaContentRating: Int = 5,
    val performanceRating: Int = 5,
    val mostValuableFeature: String = "",
    val biggestPainPoint: String = "",
    val generalSuggestions: String = "",
    val deviceModel: String = "",
    val appVersion: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class BetaReleaseNote(
    val versionName: String,
    val buildCode: Int,
    val releaseDate: String,
    val platform: String,
    val channel: String,
    val highlights: List<String>,
    val fixedIssues: List<String>,
    val knownLimitations: List<String>,
    val targetGroups: List<TesterGroup>
)

data class DistributionChannelInfo(
    val platform: String,
    val channelName: String,
    val methodTitle: String,
    val stepGuide: List<String>,
    val updateInstructions: String,
    val supportNote: String
)
