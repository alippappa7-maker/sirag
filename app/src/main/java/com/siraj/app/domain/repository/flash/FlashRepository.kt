package com.siraj.app.domain.repository.flash

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.flash.Flash
import com.siraj.app.domain.models.flash.FlashesFeedResult

interface FlashRepository {
    suspend fun getFlashesFeed(pageToken: String?, limit: Int = 10): Resource<FlashesFeedResult>
    
    suspend fun toggleLike(flashId: String): Resource<Boolean> // returns new state
    
    suspend fun toggleSave(flashId: String): Resource<Boolean> // returns new state
    
    suspend fun logView(flashId: String)
    
    suspend fun reportFlash(flashId: String, reason: String): Resource<Unit>
    
    suspend fun followCreator(creatorId: String): Resource<Unit>
}
