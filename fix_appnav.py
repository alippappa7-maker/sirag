import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# We found why it fails: it complains about `arguments` and `composable` mostly inside `MainShellScreen.kt` and `AppNavigation.kt`.
# In AppNavigation.kt, `NavBackStackEntry` needs to be imported from `androidx.navigation.NavBackStackEntry`.
# And `composable` from `androidx.navigation.compose.composable`.

# Wait! If the package `androidx.navigation.compose.composable` is NOT imported correctly, it won't resolve it.
import re

content = re.sub(r'import .*\n(?:import .*\n)*', """
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.siraj.app.core.ui.components.ErrorScreen
import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.UserRole
import com.siraj.app.features.admin.presentation.AdminScreen
import com.siraj.app.features.audio.presentation.AudioLibraryScreen
import com.siraj.app.features.auth.presentation.AuthViewModel
import com.siraj.app.features.auth.presentation.AuthViewModelFactory
import com.siraj.app.features.auth.presentation.LoginScreen
import com.siraj.app.features.auth.presentation.RegisterScreen
import com.siraj.app.features.details.presentation.DetailsScreen
import com.siraj.app.features.flashes.presentation.FlashesScreen
import com.siraj.app.features.home.presentation.HomeScreen
import com.siraj.app.features.audio.presentation.AudioScreen
import com.siraj.app.features.audio.presentation.AudioPlayerScreen
import com.siraj.app.core.audio.MiniPlayer
import com.siraj.app.features.mihrab.presentation.MihrabScreen
import com.siraj.app.features.onboarding.presentation.OnboardingScreen
import com.siraj.app.features.settings.presentation.ProfileScreen
import com.siraj.app.features.project.presentation.scenes.ScenesScreen
import com.siraj.app.features.settings.presentation.WorkspaceSettingsScreen
import com.siraj.app.features.splash.presentation.SplashScreen
import com.siraj.app.features.studio.presentation.StudioScreen
import com.siraj.app.features.ideation.presentation.IdeationScreen
import com.siraj.app.features.project.presentation.plan.ContentPlanScreen
import com.siraj.app.features.quran.presentation.QuranScreen
import com.siraj.app.features.quran.presentation.SurahScreen
import com.siraj.app.features.mihrab.prayer.presentation.PrayerSettingsScreen
import com.siraj.app.features.mihrab.prayer.presentation.PrayerTimesScreen
import com.siraj.app.features.mihrab.qibla.presentation.QiblaScreen
import com.siraj.app.features.mihrab.calendar.presentation.HijriCalendarScreen
import com.siraj.app.features.mihrab.adhkar.presentation.AdhkarCategoriesScreen
import com.siraj.app.features.mihrab.adhkar.presentation.AdhkarReaderScreen
import java.net.URLDecoder

""", content, count=1)

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
