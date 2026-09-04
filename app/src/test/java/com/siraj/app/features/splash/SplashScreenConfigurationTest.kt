package com.siraj.app.features.splash

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.siraj.app.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SplashScreenConfigurationTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun verifySplashBackgroundColorResource() {
        val color = ContextCompat.getColor(context, R.color.splash_background)
        // 0xFF060B14
        val expectedColor = 0xFF060B14.toInt()
        assertTrue("Splash background color should match brand palette #060B14", color == expectedColor)
    }

    @Test
    fun verifySplashLogoDrawableExists() {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_siraj_splash_logo)
        assertNotNull("ic_siraj_splash_logo drawable must be valid and resolvable", drawable)
    }

    @Test
    fun verifySplashBrandingFooterDrawableExists() {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_siraj_branding_footer)
        assertNotNull("ic_siraj_branding_footer drawable must be valid and resolvable", drawable)
    }

    @Test
    fun verifyThemeAppStartingStyleExists() {
        val resourceName = context.resources.getResourceName(R.style.Theme_App_Starting)
        assertNotNull("Theme attributes should be accessible in resources", resourceName)
        assertTrue("Resource name should match Theme.App.Starting", resourceName.contains("Theme.App.Starting") || resourceName.contains("Theme_App_Starting"))
    }
}
