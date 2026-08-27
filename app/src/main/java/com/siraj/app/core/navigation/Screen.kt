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

    object ProjectEditor : Screen("project/{id}") {
        fun createRoute(id: String) = "project/$id"
    }
}
