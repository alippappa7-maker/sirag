package com.siraj.app.domain.notification

import com.siraj.app.domain.models.notification.NotificationPreferences
import com.siraj.app.domain.models.notification.NotificationType
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class NotificationPreferencesTest {

    @Test
    fun defaultPreferences_marketingIsDisabledByDefault() {
        val prefs = NotificationPreferences()
        assertFalse(prefs.marketingAllowed)
        assertTrue(prefs.videoGeneration)
        assertTrue(prefs.reviewResults)
        assertTrue(prefs.prayerReminders)
        assertTrue(prefs.adhkarReminders)
        assertTrue(prefs.hideSensitiveOnLockScreen)
    }

    @Test
    fun isTypeEnabled_respectsIndividualToggles() {
        var prefs = NotificationPreferences(videoGeneration = false, prayerReminders = true)
        assertFalse(prefs.isTypeEnabled(NotificationType.VIDEO_GENERATION_COMPLETED))
        assertTrue(prefs.isTypeEnabled(NotificationType.PRAYER_REMINDER))

        prefs = prefs.copy(prayerReminders = false)
        assertFalse(prefs.isTypeEnabled(NotificationType.PRAYER_REMINDER))
    }

    @Test
    fun isQuietHourNow_returnsFalseWhenDisabled() {
        val prefs = NotificationPreferences(quietHoursEnabled = false)
        assertFalse(prefs.isQuietHourNow())
    }

    @Test
    fun quietHours_wrappingAroundMidnight() {
        // 22:00 to 07:00
        val prefs = NotificationPreferences(
            quietHoursEnabled = true,
            quietHoursStartHour = 22,
            quietHoursStartMinute = 0,
            quietHoursEndHour = 7,
            quietHoursEndMinute = 0
        )
        assertTrue(prefs.quietHoursStartHour > prefs.quietHoursEndHour)
    }
}
