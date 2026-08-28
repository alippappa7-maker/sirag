package com.siraj.app.domain.repository.prayer

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.prayer.PrayerSettings
import com.siraj.app.domain.models.prayer.PrayerTimes
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {
    suspend fun getPrayerTimes(settings: PrayerSettings): Resource<PrayerTimes>
    fun getPrayerSettings(): Flow<PrayerSettings>
    suspend fun updateSettings(settings: PrayerSettings)
}
