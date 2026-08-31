package com.siraj.app.features.splash

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.siraj.app.features.splash.presentation.SplashScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashScreen_displaysContainerAndTitle() {
        var navigated = false

        composeTestRule.setContent {
            SplashScreen(onNavigateToHome = { navigated = true })
        }

        composeTestRule.onNodeWithTag("splash_screen_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_app_title").assertIsDisplayed()
    }
}
