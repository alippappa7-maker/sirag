package com.siraj.app.data.repository.community

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.repository.community.InteractionRepository
import kotlinx.coroutines.delay

class FirebaseInteractionRepositoryImpl : InteractionRepository {

    // Mock Databases
    private val likes = mutableMapOf<String, MutableSet<String>>() // targetId -> set of userIds
    private val saves = mutableMapOf<String, MutableSet<String>>() // targetId -> set of userIds
    private val follows = mutableMapOf<String, MutableSet<String>>() // targetUserId -> set of follower userIds
    private val blocks = mutableMapOf<String, MutableSet<String>>() // userId -> set of blocked userIds
    private val hides = mutableMapOf<String, MutableSet<String>>() // userId -> set of hidden targetIds

    override suspend fun toggleLike(userId: String, targetId: String): Resource<Boolean> {
        delay(200)
        val targetLikes = likes.getOrPut(targetId) { mutableSetOf() }
        val isLiked = if (targetLikes.contains(userId)) {
            targetLikes.remove(userId)
            false
        } else {
            targetLikes.add(userId)
            true
        }
        return Resource.Success(isLiked)
    }

    override suspend fun getLikeCount(targetId: String): Resource<Int> {
        return Resource.Success(likes[targetId]?.size ?: 0)
    }

    override suspend fun toggleSave(userId: String, targetId: String): Resource<Boolean> {
        delay(200)
        val targetSaves = saves.getOrPut(targetId) { mutableSetOf() }
        val isSaved = if (targetSaves.contains(userId)) {
            targetSaves.remove(userId)
            false
        } else {
            targetSaves.add(userId)
            true
        }
        return Resource.Success(isSaved)
    }

    override suspend fun getSavedItems(userId: String): Resource<List<String>> {
        val userSaves = saves.entries.filter { it.value.contains(userId) }.map { it.key }
        return Resource.Success(userSaves)
    }

    override suspend fun toggleFollow(userId: String, targetUserId: String): Resource<Boolean> {
        delay(200)
        val targetFollowers = follows.getOrPut(targetUserId) { mutableSetOf() }
        val isFollowing = if (targetFollowers.contains(userId)) {
            targetFollowers.remove(userId)
            false
        } else {
            targetFollowers.add(userId)
            true
        }
        return Resource.Success(isFollowing)
    }

    override suspend fun getFollowers(userId: String): Resource<List<String>> {
        return Resource.Success(follows[userId]?.toList() ?: emptyList())
    }

    override suspend fun getFollowing(userId: String): Resource<List<String>> {
        val following = follows.entries.filter { it.value.contains(userId) }.map { it.key }
        return Resource.Success(following)
    }

    override suspend fun blockUser(userId: String, blockedUserId: String): Resource<Unit> {
        delay(200)
        val userBlocks = blocks.getOrPut(userId) { mutableSetOf() }
        userBlocks.add(blockedUserId)
        return Resource.Success(Unit)
    }

    override suspend fun unblockUser(userId: String, blockedUserId: String): Resource<Unit> {
        delay(200)
        blocks[userId]?.remove(blockedUserId)
        return Resource.Success(Unit)
    }

    override suspend fun getBlockedUsers(userId: String): Resource<List<String>> {
        return Resource.Success(blocks[userId]?.toList() ?: emptyList())
    }

    override suspend fun hideContent(userId: String, contentId: String): Resource<Unit> {
        delay(200)
        val userHides = hides.getOrPut(userId) { mutableSetOf() }
        userHides.add(contentId)
        return Resource.Success(Unit)
    }

    override suspend fun getHiddenContent(userId: String): Resource<List<String>> {
        return Resource.Success(hides[userId]?.toList() ?: emptyList())
    }

    override suspend fun recordShare(userId: String, targetId: String): Resource<Unit> {
        // Analytics mapping (mocked)
        return Resource.Success(Unit)
    }
}
