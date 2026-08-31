package com.siraj.app.domain.repository.community

import com.siraj.app.core.utils.Resource

interface InteractionRepository {
    // Likes
    suspend fun toggleLike(userId: String, targetId: String): Resource<Boolean> // returns new like state

    suspend fun getLikeCount(targetId: String): Resource<Int>

    // Saves
    suspend fun toggleSave(
        userId: String,
        targetId: String,
    ): Resource<Boolean>

    suspend fun getSavedItems(userId: String): Resource<List<String>>

    // Follows
    suspend fun toggleFollow(
        userId: String,
        targetUserId: String,
    ): Resource<Boolean>

    suspend fun getFollowers(userId: String): Resource<List<String>>

    suspend fun getFollowing(userId: String): Resource<List<String>>

    // Block & Hide
    suspend fun blockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit>

    suspend fun unblockUser(
        userId: String,
        blockedUserId: String,
    ): Resource<Unit>

    suspend fun getBlockedUsers(userId: String): Resource<List<String>>

    suspend fun hideContent(
        userId: String,
        contentId: String,
    ): Resource<Unit>

    suspend fun getHiddenContent(userId: String): Resource<List<String>>

    // Shares
    suspend fun recordShare(
        userId: String,
        targetId: String,
    ): Resource<Unit>
}
