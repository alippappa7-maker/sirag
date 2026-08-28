package com.siraj.app.core.navigation



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
import com.siraj.app.features.search.presentation.SearchScreen
import com.siraj.app.features.search.presentation.SearchViewModel
import com.siraj.app.features.search.presentation.SearchViewModelFactory
import com.siraj.app.domain.models.search.SearchCategory
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import java.net.URLDecoder



@Composable
fun AppNavigation(
    navController: NavHostController,
    toggleTheme: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())
) {
    val authState by authViewModel.authState.collectAsState()

    // Wait for the auth state to load before determining the start destination
    if (authState is Resource.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isLoggedIn = (authState as? Resource.Success)?.data != null

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainRoutes = listOf(
        Screen.Home.route,
        Screen.Studio.route,
        Screen.Flashes.route,
        Screen.Audio.route,
        Screen.Mihrab.route
    )

    val isMainScreen = mainRoutes.contains(currentRoute)

    val content: @Composable (Modifier) -> Unit = { modifier ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = modifier,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            // Public Routes
            composable(Screen.Splash.route) {
                SplashScreen(onNavigateToHome = {
                    val nextRoute = if (isLoggedIn) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Onboarding.route) {
                if (isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Home.route) { popUpTo(0) } } }
                else {
                    OnboardingScreen(onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    })
                }
            }
            composable(Screen.Login.route) {
                if (isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Home.route) { popUpTo(0) } } }
                else {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }
            }
            composable(Screen.Register.route) {
                if (isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Home.route) { popUpTo(0) } } }
                else {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }
            }

            // Protected Routes
            composable(Screen.Home.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { 
                    HomeScreen(
                        toggleTheme = toggleTheme,
                        onNavigateToProject = { projectId ->
                            navController.navigate(Screen.ProjectEditor.createRoute(projectId))
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Screen.NotificationCenter.route)
                        },
                        onNavigateToHistory = {
                            navController.navigate(Screen.ActivityHistory.route)
                        },
                        onNavigateToSearch = {
                            navController.navigate(Screen.Search.route)
                        }
                    ) 
                }
            }
            composable(Screen.Studio.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { 
                    StudioScreen(
                        onNavigateToProject = { projectId ->
                            navController.navigate(Screen.ProjectEditor.createRoute(projectId))
                        },
                        onNavigateToIdeation = {
                            navController.navigate(Screen.Ideation.route)
                        }
                    ) 
                }
            }
            composable(Screen.Mihrab.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { 
                    MihrabScreen(
                        onNavigateToQuran = { navController.navigate(Screen.Quran.route) },
                        onNavigateToPrayerTimes = { navController.navigate(Screen.PrayerTimes.route) },
                        onNavigateToQibla = { navController.navigate(Screen.Qibla.route) },
                        onNavigateToCalendar = { navController.navigate(Screen.HijriCalendar.route) },
                        onNavigateToAdhkar = { navController.navigate(Screen.AdhkarCategories.route) }
                    ) 
                }
            }
            composable(Screen.Audio.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { AudioLibraryScreen() }
            }
            composable(Screen.Flashes.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { FlashesScreen() }
            }
                        composable(Screen.WorkspaceSettings.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    WorkspaceSettingsScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Settings.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else { 
                    com.siraj.app.features.settings.presentation.SettingsScreen(
                        onNavigateToWorkspaceSettings = { navController.navigate(Screen.WorkspaceSettings.route) },
                        onNavigateToActivityHistory = { navController.navigate(Screen.ActivityHistory.route) },
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable(Screen.Admin.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val user = (authState as? Resource.Success)?.data
                    if (user?.role == UserRole.ADMIN || user?.role == UserRole.OWNER) {
                        AdminScreen()
                    } else {
                        ErrorScreen(
                            message = "ليس لديك صلاحية للوصول إلى لوحة الإدارة.",
                            onRetry = { navController.popBackStack() }
                        )
                    }
                }
            }


            composable(
                route = Screen.ProjectEditor.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                if (!isLoggedIn) { 
                    LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } 
                } else {
                    val id = backStackEntry.arguments?.getString("id") ?: ""
                    com.siraj.app.features.project.presentation.ProjectEditorScreen(
                        projectId = id,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPlan = { projectId ->
                            navController.navigate(Screen.ContentPlan.createRoute(projectId))
                        },
                        onNavigateToAssetLibrary = { projectId ->
                            navController.navigate(Screen.AssetLibrary.createRoute(projectId))
                        }
                    )
                }
            }

            
            composable(Screen.Quran.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    QuranScreen(
                        onNavigateToSurah = { surahId, surahName -> navController.navigate(Screen.Surah.createRoute(surahId, surahName)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            
            composable(
                route = Screen.Surah.route,
                arguments = listOf(
                    navArgument("surahId") { type = NavType.IntType },
                    navArgument("surahName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val surahId = backStackEntry.arguments?.getInt("surahId") ?: 1
                    val surahName = backStackEntry.arguments?.getString("surahName") ?: ""
                    SurahScreen(
                        surahId = surahId,
                        surahName = surahName,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            
            composable(
                route = Screen.Scenes.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    ScenesScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSceneEditor = { sceneId -> 
                            navController.navigate(Screen.SceneEditor.createRoute(projectId, sceneId))
                        },
                        onNavigateToPreview = {
                            navController.navigate(Screen.ProjectPreview.createRoute(projectId))
                        }
                    )
                }
            }
            
            composable(
                route = Screen.SceneEditor.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType },
                    navArgument("sceneId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    val sceneId = backStackEntry.arguments?.getString("sceneId") ?: ""
                    com.siraj.app.features.project.presentation.scenes.SceneEditorScreen(
                        projectId = projectId,
                        sceneId = sceneId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAiGenerator = { pid, sid ->
                            navController.navigate(Screen.AiImageGenerator.createRoute(pid, sid))
                        },
                        onNavigateToAudioStudio = { pid, sid, initialText ->
                            navController.navigate(Screen.AudioStudio.createRoute(pid, sid, initialText))
                        },
                        onNavigateToSoundtracks = { pid, sid ->
                            navController.navigate(Screen.SoundtrackLibrary.createRoute(pid, sid))
                        },
                        onNavigateToSubtitles = { pid, sid, initialText ->
                            navController.navigate(Screen.SubtitleEditor.createRoute(pid, sid, initialText))
                        }
                    )
                }
            }
            
            composable(
                route = Screen.AssetLibrary.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    com.siraj.app.features.project.presentation.assets.AssetLibraryScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSearch = { navController.navigate(Screen.ExternalMediaSearch.createRoute(projectId)) },
                        onNavigateToAiGenerator = { navController.navigate(Screen.AiImageGenerator.createRoute(projectId)) },
                        onNavigateToAudioStudio = { navController.navigate(Screen.AudioStudio.createRoute(projectId)) },
                        onNavigateToSoundtracks = { navController.navigate(Screen.SoundtrackLibrary.createRoute(projectId)) }
                    )
                }
            }

            composable(
                route = Screen.ExternalMediaSearch.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    com.siraj.app.features.project.presentation.assets.ExternalMediaSearchScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Screen.AiImageGenerator.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType },
                    navArgument("sceneId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    val sceneId = backStackEntry.arguments?.getString("sceneId")
                    com.siraj.app.features.project.presentation.ai.AiImageGeneratorScreen(
                        projectId = projectId,
                        sceneId = sceneId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Screen.AudioStudio.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType },
                    navArgument("sceneId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("initialText") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    val sceneId = backStackEntry.arguments?.getString("sceneId")
                    val rawInitialText = backStackEntry.arguments?.getString("initialText") ?: ""
                    val decodedInitialText = try {
                        java.net.URLDecoder.decode(rawInitialText, "UTF-8")
                    } catch (e: Exception) {
                        rawInitialText
                    }
                    com.siraj.app.features.project.presentation.audio.AudioStudioScreen(
                        projectId = projectId,
                        sceneId = sceneId,
                        initialNarrationText = decodedInitialText,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Screen.SoundtrackLibrary.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType },
                    navArgument("sceneId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    val sceneId = backStackEntry.arguments?.getString("sceneId")
                    com.siraj.app.features.project.presentation.soundtrack.SoundtrackLibraryScreen(
                        projectId = projectId,
                        sceneId = sceneId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Screen.SubtitleEditor.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.StringType },
                    navArgument("sceneId") { type = NavType.StringType },
                    navArgument("initialText") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    val sceneId = backStackEntry.arguments?.getString("sceneId") ?: ""
                    val rawInitialText = backStackEntry.arguments?.getString("initialText")
                    val decodedInitialText = if (!rawInitialText.isNullOrBlank()) {
                        try { java.net.URLDecoder.decode(rawInitialText, "UTF-8") } catch (e: Exception) { rawInitialText }
                    } else ""
                    com.siraj.app.features.project.presentation.subtitles.SubtitleEditorScreen(
                        projectId = projectId,
                        sceneId = sceneId,
                        initialSceneText = decodedInitialText,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Screen.ProjectPreview.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    com.siraj.app.features.project.presentation.preview.ProjectPreviewScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSceneEdit = { sceneId ->
                            navController.navigate(Screen.SceneEditor.createRoute(projectId, sceneId))
                        },
                        onNavigateToSubtitles = { sceneId ->
                            navController.navigate(Screen.SubtitleEditor.createRoute(projectId, sceneId))
                        },
                        onNavigateToExportJob = {
                            navController.navigate(Screen.ProjectExport.createRoute(projectId))
                        }
                    )
                }
            }

            composable(
                route = Screen.ProjectExport.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    com.siraj.app.features.project.presentation.export.ProjectExportScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = Screen.ProductionJobs.route,
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId")
                    com.siraj.app.features.project.presentation.jobs.ProductionJobsScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.AudioPlayer.route) {
                AudioPlayerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Ideation.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    IdeationScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProject = { projectId ->
                            navController.navigate(Screen.ProjectEditor.createRoute(projectId)) {
                                popUpTo(Screen.Studio.route)
                            }
                        }
                    )
                }
            }

            composable(
                route = Screen.ContentPlan.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                    ContentPlanScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToScenes = { pid -> navController.navigate(Screen.Scenes.createRoute(pid)) }
                    )
                }
            }

            composable(Screen.NotificationCenter.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    com.siraj.app.features.notification.presentation.NotificationCenterScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = { navController.navigate(Screen.NotificationSettings.route) },
                        onNavigateToProject = { pid -> navController.navigate(Screen.ProjectEditor.createRoute(pid)) },
                        onNavigateToReview = { rid -> navController.navigate(Screen.ReviewList.route) },
                        onNavigateToAudio = { aid -> navController.navigate(Screen.Audio.route) },
                        onNavigateToFlashes = { navController.navigate(Screen.Flashes.route) },
                        onNavigateToMihrab = { navController.navigate(Screen.Mihrab.route) }
                    )
                }
            }

            composable(Screen.NotificationSettings.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    com.siraj.app.features.notification.presentation.NotificationSettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.ActivityHistory.route) {
                if (!isLoggedIn) { LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) { popUpTo(0) } } }
                else {
                    com.siraj.app.features.history.presentation.ActivityHistoryScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onResumeVideo = { videoId ->
                            // If it's a project ID, navigate to project preview or editor
                            navController.navigate(Screen.ProjectPreview.createRoute(videoId))
                        },
                        onResumeAudio = { audioId ->
                            navController.navigate(Screen.AudioPlayer.route)
                        },
                        onResumeQuran = { surahIdStr ->
                            val surahId = surahIdStr.toIntOrNull() ?: 1
                            navController.navigate(Screen.Surah.createRoute(surahId, "سورة"))
                        },
                        onResumeFlash = { _ ->
                            navController.navigate(Screen.Flashes.route)
                        }
                    )
                }
            }

            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink { uriPattern = "${Screen.Details.DEEP_LINK_URI}/{id}" })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                DetailsScreen(id = id)
            }

            composable(Screen.Search.route) {
                val context = LocalContext.current
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModelFactory(
                        application = context.applicationContext as Application,
                        currentUserId = (authState as? Resource.Success)?.data?.uid ?: "user_default"
                    )
                )

                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResult = { item ->
                        when (item.category) {
                            SearchCategory.QURAN -> {
                                if (item.targetRoute.isNotBlank()) {
                                    navController.navigate(item.targetRoute)
                                } else {
                                    val surahId = item.extraMetadata["surahId"]?.toIntOrNull() ?: 1
                                    navController.navigate(Screen.Surah.createRoute(surahId, item.title))
                                }
                            }
                            SearchCategory.AUDIO -> {
                                navController.navigate(Screen.AudioPlayer.route)
                            }
                            SearchCategory.FLASH -> {
                                navController.navigate(Screen.Flashes.route)
                            }
                            SearchCategory.PROJECT -> {
                                val projectId = item.extraMetadata["projectId"] ?: item.id.removePrefix("project_")
                                navController.navigate(Screen.ProjectEditor.createRoute(projectId))
                            }
                            SearchCategory.TEMPLATE -> {
                                navController.navigate(Screen.Ideation.route)
                            }
                            SearchCategory.SOURCE -> {
                                val sourceId = item.id.removePrefix("src_")
                                navController.navigate(Screen.Details.createRoute(sourceId))
                            }
                            SearchCategory.ALL -> {
                                if (item.targetRoute.isNotBlank()) {
                                    navController.navigate(item.targetRoute)
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (isMainScreen) {
        MainShellScreen(navController = navController) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                content(Modifier)
                MiniPlayer(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                    onExpand = { navController.navigate(Screen.AudioPlayer.route) }
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            content(Modifier)
            MiniPlayer(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                onExpand = { navController.navigate(Screen.AudioPlayer.route) }
            )
        }
    }
}
