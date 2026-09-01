package com.siraj.app.features.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.core.ui.components.SirajButton
import com.siraj.app.core.ui.components.SirajTextField
import androidx.compose.ui.unit.sp
import com.siraj.app.core.utils.Resource
import com.siraj.app.features.auth.presentation.AuthViewModel
import com.siraj.app.features.auth.presentation.AuthViewModelFactory

private val OnboardingBackground = Color(0xFF060B14)
private val OnboardingSurface = Color(0xFF0D1521)
private val AccentGold = Color(0xFFFFD54F)
private val AccentEmerald = Color(0xFF00F0B0)
private val TextPrimary = Color(0xFFE2E8F0)
private val TextSecondary = Color(0xFF94A3B8)

private data class FeaturePage(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val featurePages = listOf(
    FeaturePage(
        icon = Icons.Filled.Verified,
        title = "محتوى إسلامي موثّق",
        description = "كل نص يمرّ بمراجعة شرعية صارمة قبل النشر، مع إسناد إلى المصادر المعتمدة.",
    ),
    FeaturePage(
        icon = Icons.Filled.AutoAwesome,
        title = "استوديو الذكاء الاصطناعي",
        description = "أنشئ المحتوى والأشرطة والترجمات بمساعدة ذكاء اصطناعي مُوجَّه وفق الضوابط الشرعية.",
    ),
    FeaturePage(
        icon = Icons.Filled.Mosque,
        title = "مركز المحراب",
        description = "مواقيت الصلاة، الأذكار، والقرآن في مكان واحد، مع تذكيرات روحانية لطيفة.",
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val pageCount = featurePages.size + 2 // welcome + features + login
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(OnboardingBackground, OnboardingSurface),
                        ),
                )
                .testTag("onboarding_root"),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                in 1..featurePages.size -> FeaturePage(featurePages[page - 1])
                else -> LoginPage(
                    onLoginSuccess = onNavigateToHome,
                    onNavigateToRegister = onNavigateToRegister,
                    onContinueAsGuest = onContinueAsGuest,
                )
            }
        }

        // Bottom controls
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PageIndicator(currentPage = pagerState.currentPage, pageCount = pageCount)

            Spacer(modifier = Modifier.height(20.dp))

            if (pagerState.currentPage == pageCount - 1) {
                // Login page renders its own primary action; only a hint here.
                Text(
                    text = "اسحب للعودة • أو تابع كزائر",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            } else {
                SirajButton(
                    text = if (pagerState.currentPage == pageCount - 2) "ابدأ الآن" else "التالي",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                )
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(AccentEmerald.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✦", fontSize = 40.sp, color = AccentGold)
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "سراج",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "نورٌ يهديك في صناعة المحتوى الإسلامي",
            style = MaterialTheme.typography.titleMedium,
            color = AccentEmerald,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "المنصة الأولى لصناعة ونشر المحتوى الإسلامي العربي الموثّق.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FeaturePage(page: FeaturePage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(AccentGold.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoginPage(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory()),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(actionState) {
        if (actionState is Resource.Success) {
            viewModel.resetActionState()
            onLoginSuccess()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = AccentEmerald,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "تسجيل الدخول",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(24.dp))

        SirajTextField(value = email, onValueChange = { email = it }, label = "البريد الإلكتروني")
        Spacer(modifier = Modifier.height(14.dp))
        SirajTextField(value = password, onValueChange = { password = it }, label = "كلمة المرور")

        if (actionState is Resource.Error) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = (actionState as Resource.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (actionState is Resource.Loading) {
            CircularProgressIndicator(color = AccentEmerald)
        } else {
            SirajButton(
                text = "دخول",
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ليس لديك حساب؟ تسجيل جديد",
            style = MaterialTheme.typography.bodyMedium,
            color = AccentEmerald,
            modifier =
                Modifier
                    .testTag("onboarding_register_link")
                    .clickable(onClick = onNavigateToRegister),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "المتابعة كزائر",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier =
                Modifier
                    .testTag("onboarding_guest_link")
                    .clickable(onClick = onContinueAsGuest),
        )
    }
}

@Composable
private fun PageIndicator(currentPage: Int, pageCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier =
                    Modifier
                        .size(if (selected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) AccentEmerald else TextSecondary.copy(alpha = 0.4f),
                        ),
            )
        }
    }
}
