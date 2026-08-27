package com.siraj.app.domain.repository

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun login(email: String, password: String): Resource<Unit>
    suspend fun register(name: String, email: String, password: String): Resource<Unit>
    suspend fun logout(): Resource<Unit>
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun verifyEmail(): Resource<Unit>
    suspend fun deleteAccount(): Resource<Unit>
    suspend fun updateProfile(name: String, avatarUrl: String? = null): Resource<Unit>
    suspend fun updatePreferences(preferences: com.siraj.app.domain.models.UserPreferences): Resource<Unit>
}
