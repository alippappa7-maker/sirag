package com.siraj.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.siraj.app.MainActivity
import com.siraj.app.R
import com.siraj.app.domain.models.notification.NotificationPreferences
import com.siraj.app.domain.models.notification.NotificationType
import com.siraj.app.domain.models.notification.SirajNotification
import com.siraj.app.core.error.GlobalErrorHandler

object NotificationHelper {

    const val CHANNEL_PROJECTS = "siraj_projects_channel"
    const val CHANNEL_REVIEW = "siraj_review_channel"
    const val CHANNEL_PRAYERS = "siraj_prayers_channel"
    const val CHANNEL_CONTENT = "siraj_content_channel"
    const val CHANNEL_BILLING = "siraj_billing_channel"
    const val CHANNEL_SYSTEM = "siraj_system_channel"

    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_ENTITY_TYPE = "extra_entity_type"
    const val EXTRA_ENTITY_ID = "extra_entity_id"
    const val EXTRA_ACTION_URL = "extra_action_url"

    /**
     * Initializes all Android notification channels with localized Arabic names and descriptions.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PROJECTS,
                    "المشاريع والإنتاج",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات اكتمال تصيير الفيديو، فشل التصدير، وتحديثات المشاهد"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_REVIEW,
                    "المراجعة والاعتماد",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "طلبات المراجعة الشرعية، ونتائج التدقيق والاعتماد"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_PRAYERS,
                    "المحراب والصلاة والأذكار",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات مواقيت الصلاة والأذكار اليومية"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_CONTENT,
                    "المحتوى الصوتي والومضات",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "إشعارات المحتوى القرآني والتلاوات والومضات الجديدة"
                },
                NotificationChannel(
                    CHANNEL_BILLING,
                    "الاشتراك والفوترة",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تنبيهات تجديد الاشتراك وتحديثات الرصيد"
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "رسائل النظام",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تنبيهات الصيانة، التحديثات، ورسائل منصة سراج"
                }
            )

            manager.createNotificationChannels(channels)
            Log.d("NotificationHelper", "Siraj notification channels created successfully.")
        }
    }

    /**
     * Displays a system notification if permitted by user preferences and quiet hours.
     */
    fun showSystemNotification(
        context: Context,
        notification: SirajNotification,
        preferences: NotificationPreferences = NotificationPreferences()
    ) {
        // 1. Check if user enabled this notification type
        if (!preferences.isTypeEnabled(notification.type)) {
            Log.d("NotificationHelper", "Notification type ${notification.type} disabled by user preferences.")
            return
        }

        // 2. Check Quiet Hours (unless it's an urgent prayer reminder or critical system notice)
        if (preferences.isQuietHourNow() && notification.type != NotificationType.PRAYER_REMINDER) {
            Log.d("NotificationHelper", "Quiet hours active. Suppressing notification: ${notification.title}")
            return
        }

        val channelId = notification.type.channelId

        // Build target intent to open appropriate screen in MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_ID, notification.id)
            putExtra(EXTRA_ENTITY_TYPE, notification.entityType)
            putExtra(EXTRA_ENTITY_ID, notification.entityId)
            putExtra(EXTRA_ACTION_URL, notification.actionUrl)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            pendingIntentFlags
        )

        // Lock screen visibility: if sensitive, protect content on lock screen
        val visibility = if (notification.isSensitive || preferences.hideSensitiveOnLockScreen) {
            NotificationCompat.VISIBILITY_PRIVATE
        } else {
            NotificationCompat.VISIBILITY_PUBLIC
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setPriority(
                if (notification.type == NotificationType.PRAYER_REMINDER || notification.type == NotificationType.VIDEO_GENERATION_COMPLETED)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setVisibility(visibility)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notification.id.hashCode(), builder.build())
            }
        } catch (e: SecurityException) {
            Log.w("NotificationHelper", "Notification permission not granted", e)
        } catch (e: Exception) { GlobalErrorHandler.handle(e) }
    }
}
