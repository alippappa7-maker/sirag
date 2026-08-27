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
