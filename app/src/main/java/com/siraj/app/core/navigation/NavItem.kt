package com.siraj.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home : NavItem("الرئيسية", Icons.Default.Home, Screen.Home.route)
    object Studio : NavItem("الاستوديو", Icons.Default.CameraAlt, Screen.Studio.route)
    object Flashes : NavItem("فلاشات", Icons.Default.PlayCircle, "flashes")
    object Library : NavItem("المكتبة", Icons.Default.LibraryBooks, Screen.Audio.route)
    object Mihrab : NavItem("المحراب", Icons.Default.Star, "mihrab")
}
