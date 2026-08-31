package com.siraj.app.core.di

import android.content.Context
import com.siraj.app.core.network.SirajNetworkClient
import com.siraj.app.core.network.NetworkClient
import com.siraj.app.data.repository.*
import com.siraj.app.data.repository.adhkar.AdkarRepositoryImpl
import com.siraj.app.data.repository.admin.AdminSecurityRepositoryImpl
import com.siraj.app.data.repository.analytics.FirebaseAnalyticsRepositoryImpl
import com.siraj.app.data.repository.audio.FirebaseAudioRepositoryImpl
import com.siraj.app.data.repository.beta.FirebaseBetaDefectManagementRepositoryImpl
import com.siraj.app.data.repository.backup.FirebaseBackupRepositoryImpl
import com.siraj.app.data.repository.community.FirebaseCommunityRepositoryImpl
import com.siraj.app.data.repository.cost.FirebaseCostRepositoryImpl
import com.siraj.app.data.repository.flash.FirebaseFlashRepositoryImpl
import com.siraj.app.data.repository.incident.FirebaseIncidentResponseRepositoryImpl
import com.siraj.app.data.repository.minor.FirebaseMinorSafetyRepositoryImpl
import com.siraj.app.data.repository.monitoring.FirebaseMonitoringRepositoryImpl
import com.siraj.app.data.repository.notification.FirebaseNotificationRepositoryImpl
import com.siraj.app.data.repository.prayer.FirebasePrayerRepositoryImpl
import com.siraj.app.data.repository.review.FirebaseShariaReviewRepositoryImpl
import com.siraj.app.data.repository.search.UnifiedSearchRepositoryImpl
import com.siraj.app.data.repository.share.FirebaseShareRepositoryImpl
import com.siraj.app.data.repository.subscription.FirebaseSubscriptionRepositoryImpl
import com.siraj.app.data.repository.support.FirebaseSupportRepositoryImpl
import com.siraj.app.data.repository.taxonomy.FirebaseContentTaxonomyRepositoryImpl
import com.siraj.app.domain.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Lightweight ServiceLocator for dependency injection.
 * Provides a single initialization point and lazy singleton access.
 * This avoids the overhead of Hilt/Ksp annotation processing while
 * still centralizing dependency creation and making it testable.
 *
 * Usage:
 *   ServiceLocator.init(context)
 *   val repo = ServiceLocator.authRepository
 */
object ServiceLocator {

    private lateinit var appContext: Context

    // Firebase instances (lazy singletons)
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val functions: FirebaseFunctions by lazy { FirebaseFunctions.getInstance() }
    val analytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(appContext) }
    val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    // Network
    val networkClient: NetworkClient by lazy { SirajNetworkClient() }

    // Repositories (lazy singletons)
    val authRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepositoryImpl(auth, firestore) }
    val activityHistoryRepository: ActivityHistoryRepository by lazy { FirebaseActivityHistoryRepositoryImpl(firestore) }
    val adhkarRepository: AdhkarRepository by lazy { AdhkarRepositoryImpl(firestore) }
    val adminSecurityRepository: AdminSecurityRepository by lazy { AdminSecurityRepositoryImpl(firestore) }
    val analyticsRepository: AnalyticsRepository by lazy { FirebaseAnalyticsRepositoryImpl(analytics, firestore) }
    val audioRepository: AudioRepository by lazy { FirebaseAudioRepositoryImpl(firestore, storage, auth) }
    val backupRepository: BackupRepository by lazy { FirebaseBackupRepositoryImpl(firestore, storage) }
    val betaDefectRepository: BetaDefectManagementRepository by lazy { FirebaseBetaDefectManagementRepositoryImpl(firestore) }
    val communityRepository: CommunityRepository by lazy { FirebaseCommunityRepositoryImpl(firestore) }
    val costRepository: CostRepository by lazy { FirebaseCostRepositoryImpl(firestore) }
    val flashRepository: FlashRepository by lazy { FirebaseFlashRepositoryImpl(firestore, storage, auth) }
    val incidentRepository: IncidentResponseRepository by lazy { FirebaseIncidentResponseRepositoryImpl(firestore) }
    val minorSafetyRepository: MinorSafetyRepository by lazy { FirebaseMinorSafetyRepositoryImpl(firestore) }
    val monitoringRepository: MonitoringRepository by lazy { FirebaseMonitoringRepositoryImpl(firestore) }
    val notificationRepository: NotificationRepository by lazy { FirebaseNotificationRepositoryImpl(firestore) }
    val prayerRepository: PrayerRepository by lazy { FirebasePrayerRepositoryImpl(firestore) }
    val shariaReviewRepository: ShariaReviewRepository by lazy { FirebaseShariaReviewRepositoryImpl(firestore) }
    val searchRepository: SearchRepository by lazy { UnifiedSearchRepositoryImpl(firestore) }
    val shareRepository: ShareRepository by lazy { FirebaseShareRepositoryImpl(firestore) }
    val subscriptionRepository: SubscriptionRepository by lazy { FirebaseSubscriptionRepositoryImpl(firestore) }
    val supportRepository: SupportRepository by lazy { FirebaseSupportRepositoryImpl(firestore) }
    val taxonomyRepository: ContentTaxonomyRepository by lazy { FirebaseContentTaxonomyRepositoryImpl(firestore) }

    /**
     * Initialize the ServiceLocator with application context.
     * Must be called once in Application.onCreate().
     */
    fun init(context: Context) {
        if (this::appContext.isInitialized) return
        appContext = context.applicationContext
    }

    /**
     * Check if ServiceLocator has been initialized.
     */
    val isInitialized: Boolean get() = this::appContext.isInitialized
}
