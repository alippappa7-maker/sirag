package com.siraj.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavItem(Screen.Home.route, "الرئيسية", Icons.Default.Home)
    object Studio : NavItem(Screen.Studio.route, "الاستوديو", Icons.Default.Build)
    object Flashes : NavItem(Screen.Flashes.route, "ومضات", Icons.Default.PlayArrow)
    object Audio : NavItem(Screen.Audio.route, "صوتيات", Icons.Default.List)
    object Quran : NavItem(Screen.Quran.route, "القرآن", Icons.Default.Book)
}
