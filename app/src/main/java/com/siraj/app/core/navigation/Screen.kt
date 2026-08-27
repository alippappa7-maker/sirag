package com.siraj.app.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    
    object Home : Screen("home")
    object Studio : Screen("studio")
    object Mihrab : Screen("mihrab")
    object Audio : Screen("audio")
    object Flashes : Screen("flashes")
    
    object Settings : Screen("settings")
    object WorkspaceSettings : Screen("workspace_settings")
    object Admin : Screen("admin")
    
    object Details : Screen("details/{id}") {
        fun createRoute(id: String) = "details/$id"
        const val DEEP_LINK_URI = "siraj://details"
    }
    
    object SceneEditor : Screen("scene_editor/{projectId}/{sceneId}") {
        fun createRoute(projectId: String, sceneId: String) = "scene_editor/$projectId/$sceneId"
    }
    
    object AssetLibrary : Screen("asset_library/{projectId}") {
        fun createRoute(projectId: String) = "asset_library/$projectId"
    }
    
    object ExternalMediaSearch : Screen("external_media_search/{projectId}") {
        fun createRoute(projectId: String) = "external_media_search/$projectId"
    }

    object AiImageGenerator : Screen("ai_image_generator/{projectId}?sceneId={sceneId}") {
        fun createRoute(projectId: String, sceneId: String? = null) = 
            if (sceneId != null) "ai_image_generator/$projectId?sceneId=$sceneId" else "ai_image_generator/$projectId"
    }

    object AudioStudio : Screen("audio_studio/{projectId}?sceneId={sceneId}&initialText={initialText}") {
        fun createRoute(projectId: String, sceneId: String? = null, initialText: String? = null): String {
            val base = "audio_studio/$projectId"
            val params = mutableListOf<String>()
            if (sceneId != null) params.add("sceneId=$sceneId")
            if (!initialText.isNullOrBlank()) params.add("initialText=${java.net.URLEncoder.encode(initialText, "UTF-8")}")
            return if (params.isNotEmpty()) "$base?${params.joinToString("&")}" else base
        }
    }

    object SoundtrackLibrary : Screen("soundtrack_library/{projectId}?sceneId={sceneId}") {
        fun createRoute(projectId: String, sceneId: String? = null): String {
            return if (sceneId != null) "soundtrack_library/$projectId?sceneId=$sceneId" else "soundtrack_library/$projectId"
        }
    }

    object SubtitleEditor : Screen("subtitle_editor/{projectId}/{sceneId}?initialText={initialText}") {
        fun createRoute(projectId: String, sceneId: String, initialText: String? = null): String {
            val base = "subtitle_editor/$projectId/$sceneId"
            return if (!initialText.isNullOrBlank()) "$base?initialText=${java.net.URLEncoder.encode(initialText, "UTF-8")}" else base
        }
    }

    object ProjectPreview : Screen("project_preview/{projectId}") {
        fun createRoute(projectId: String): String = "project_preview/$projectId"
    }

    object ProjectExport : Screen("project_export/{projectId}") {
        fun createRoute(projectId: String): String = "project_export/$projectId"
    }

    object ProductionJobs : Screen("production_jobs?projectId={projectId}") {
        fun createRoute(projectId: String? = null): String {
            return if (!projectId.isNullOrBlank()) "production_jobs?projectId=$projectId" else "production_jobs"
        }
    }

    object Ideation : Screen("ideation")
    object Quran : Screen("quran")
    object Surah : Screen("surah/{surahId}/{surahName}") {
        fun createRoute(surahId: Int, surahName: String) = "surah/$surahId/$surahName"
    }
        
    object ContentPlan : Screen("content_plan/{projectId}") {
        fun createRoute(projectId: String) = "content_plan/$projectId"
    }
    
    object Scenes : Screen("scenes/{projectId}") {
        fun createRoute(projectId: String) = "scenes/$projectId"
    }

    object ProjectEditor : Screen("project/{id}") {
        fun createRoute(id: String) = "project/$id"
    }
}
