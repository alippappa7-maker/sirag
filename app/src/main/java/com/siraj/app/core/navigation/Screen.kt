package com.siraj.app.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ProjectEditor : Screen("project_editor/{id}") {
        fun createRoute(projectId: String) = "project_editor/$projectId"
    }
    object Export : Screen("export/{projectId}") {
        fun createRoute(projectId: String) = "export/$projectId"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object WorkspaceSettings : Screen("workspace_settings")
    object Admin : Screen("admin")
    object Library : Screen("library")
    object ContentModeration : Screen("content_moderation")
    object ReviewList : Screen("review_list")
    object ShareRouter : Screen("share_router/{linkId}?token={token}") {
        const val DEEP_LINK_URI_HTTPS = "https://siraj.app/share"
        const val DEEP_LINK_URI_APP = "siraj://share"
        fun createRoute(linkId: String, token: String? = null): String {
            return if (token != null) "share_router/$linkId?token=$token" else "share_router/$linkId"
        }
    }
    object Details : Screen("details/{id}") {
        const val DEEP_LINK_URI = "siraj://details"
        fun createRoute(id: String) = "details/$id"
    }
    object WorkspaceSelection : Screen("workspace_selection")
    object Ideation : Screen("ideation")
    object SceneEditor : Screen("scene_editor/{projectId}/{sceneId}") {
        fun createRoute(projectId: String, sceneId: String) = "scene_editor/$projectId/$sceneId"
    }
    object Scenes : Screen("scenes/{projectId}") {
        fun createRoute(projectId: String) = "scenes/$projectId"
    }
    object ContentPlan : Screen("content_plan/{projectId}") {
        fun createRoute(projectId: String) = "content_plan/$projectId"
    }
    object ProjectPreview : Screen("project_preview/{projectId}") {
        fun createRoute(projectId: String) = "project_preview/$projectId"
    }
    object ProjectExport : Screen("project_export/{projectId}") {
        fun createRoute(projectId: String) = "project_export/$projectId"
    }
    object ProductionJobs : Screen("production_jobs/{projectId}") {
        fun createRoute(projectId: String?) = "production_jobs/${projectId ?: ""}"
    }
    object ScriptEditor : Screen("script_editor/{projectId}/{sceneId}?initialText={initialText}") {
        fun createRoute(projectId: String, sceneId: String? = null, initialText: String? = null) =
            if (sceneId != null && initialText != null) "script_editor/$projectId/$sceneId?initialText=${java.net.URLEncoder.encode(initialText, "UTF-8")}"
            else if (sceneId != null) "script_editor/$projectId/$sceneId"
            else "script_editor/$projectId"
    }
    object ImageStudio : Screen("image_studio/{projectId}/{sceneId}?initialPrompt={initialPrompt}") {
        fun createRoute(projectId: String, sceneId: String? = null, initialPrompt: String? = null) =
            if (sceneId != null && initialPrompt != null) "image_studio/$projectId/$sceneId?initialPrompt=${java.net.URLEncoder.encode(initialPrompt, "UTF-8")}"
            else if (sceneId != null) "image_studio/$projectId/$sceneId"
            else "image_studio/$projectId"
    }
    object AudioStudio : Screen("audio_studio/{projectId}/{sceneId}?initialText={initialText}") {
        fun createRoute(projectId: String, sceneId: String? = null, initialText: String? = null) =
            if (sceneId != null && initialText != null) "audio_studio/$projectId/$sceneId?initialText=${java.net.URLEncoder.encode(initialText, "UTF-8")}"
            else if (sceneId != null) "audio_studio/$projectId/$sceneId"
            else "audio_studio/$projectId"
    }
    object SoundtrackLibrary : Screen("soundtrack_library/{projectId}?sceneId={sceneId}") {
        fun createRoute(projectId: String, sceneId: String? = null) =
            if (sceneId != null) "soundtrack_library/$projectId?sceneId=$sceneId"
            else "soundtrack_library/$projectId"
    }
    object SubtitleEditor : Screen("subtitle_editor/{projectId}/{sceneId}?initialText={initialText}") {
        fun createRoute(projectId: String, sceneId: String? = null, initialText: String? = null) =
            if (sceneId != null && initialText != null) "subtitle_editor/$projectId/$sceneId?initialText=${java.net.URLEncoder.encode(initialText, "UTF-8")}"
            else if (sceneId != null) "subtitle_editor/$projectId/$sceneId"
            else "subtitle_editor/$projectId"
    }
    object AssetLibrary : Screen("asset_library/{projectId}") {
        fun createRoute(projectId: String) = "asset_library/$projectId"
    }
    object ExternalMediaSearch : Screen("external_media_search/{projectId}") {
        fun createRoute(projectId: String) = "external_media_search/$projectId"
    }
    object AiImageGenerator : Screen("ai_image_generator/{projectId}?sceneId={sceneId}") {
        fun createRoute(projectId: String, sceneId: String? = null) =
            if (sceneId != null) "ai_image_generator/$projectId?sceneId=$sceneId"
            else "ai_image_generator/$projectId"
    }
    object Studio : Screen("studio")
    object CreatorAnalytics : Screen("creator_analytics")
    object Flashes : Screen("flashes")
    object FlashPublishing : Screen("flash_publishing")
    object Mihrab : Screen("mihrab")
    object PrayerTimes : Screen("prayer_times")
    object Qibla : Screen("qibla")
    object HijriCalendar : Screen("hijri_calendar")
    object AdhkarCategories : Screen("adhkar_categories")
    object Adhkar : Screen("adhkar")
    object AdhkarReader : Screen("adhkar_reader/{categoryId}") {
        fun createRoute(categoryId: String) = "adhkar_reader/$categoryId"
    }
    object Quran : Screen("quran")
    object Surah : Screen("surah/{surahId}?surahName={surahName}") {
        fun createRoute(id: Int, name: String) = "surah/$id?surahName=${java.net.URLEncoder.encode(name, "UTF-8")}"
    }
    object Audio : Screen("audio")
    object AudioPlayer : Screen("audio_player")
    object NotificationCenter : Screen("notifications")
    object NotificationSettings : Screen("notification_settings")
    object ActivityHistory : Screen("activity_history")
    object Search : Screen("search")
    object ShariaReviewQueue : Screen("sharia_review_queue")
    object ShariaReviewDetail : Screen("sharia_review_detail/{itemId}") {
        fun createRoute(itemId: String) = "sharia_review_detail/$itemId"
    }
    object SubscriptionPlans : Screen("subscription_plans")
    object UsageAndBilling : Screen("usage_and_billing")
    object TesterHub : Screen("tester_hub")
    object DefectTriage : Screen("defect_triage")
}
