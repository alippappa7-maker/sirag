package com.siraj.app.data.repository.share

import com.siraj.app.core.utils.Resource
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.domain.models.share.ShareLink
import com.siraj.app.domain.models.share.ShareStatus
import com.siraj.app.domain.models.share.ShareType
import com.siraj.app.domain.repository.share.ShareRepository
import kotlinx.coroutines.delay
import java.util.UUID

class FirebaseShareRepositoryImpl : ShareRepository {

    // Using in-memory map to mock Firebase behavior for the MVP/prototype
    private val shareLinks = mutableMapOf<String, ShareLink>()

    override suspend fun createShareLink(
        targetId: String,
        type: ShareType,
        userId: String,
        isPrivate: Boolean,
        expiresAt: Long?
    ): Resource<ShareLink> {
        return try {
            delay(500) // Simulate network
            val linkId = UUID.randomUUID().toString().take(8)
            val token = if (isPrivate) UUID.randomUUID().toString().replace("-", "") else null
            
            val shareLink = ShareLink(
                id = linkId,
                targetId = targetId,
                type = type,
                token = token,
                createdBy = userId,
                isPrivate = isPrivate,
                expiresAt = expiresAt
            )
            
            shareLinks[linkId] = shareLink
            Resource.Success(shareLink)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun getAndValidateShareLink(
        linkId: String,
        token: String?
    ): Resource<ShareLink> {
        return try {
            delay(600) // Simulate network
            val link = shareLinks[linkId] 
                ?: return Resource.Error("هذا الرابط غير موجود أو تم حذفه.")

            if (link.isExpired) {
                return Resource.Error("عذراً، انتهت صلاحية هذا الرابط.")
            }
            
            if (link.status == ShareStatus.REVOKED) {
                return Resource.Error("تم إبطال هذا الرابط من قبل صاحبه.")
            }
            
            if (link.status == ShareStatus.UNAVAILABLE) {
                return Resource.Error("المحتوى المرتبط بهذا الرابط غير متاح.")
            }

            if (link.isPrivate && link.token != token) {
                return Resource.Error("ليس لديك الصلاحية للوصول إلى هذا المحتوى.")
            }

            Resource.Success(link)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun revokeShareLink(linkId: String, userId: String): Resource<Unit> {
        return try {
            delay(400)
            val link = shareLinks[linkId] 
                ?: return Resource.Error("الرابط غير موجود.")
                
            if (link.createdBy != userId) {
                return Resource.Error("ليس لديك صلاحية لإبطال هذا الرابط.")
            }
            
            shareLinks[linkId] = link.copy(status = ShareStatus.REVOKED)
            Resource.Success(Unit)
        } catch (e: Exception) {
            val error = ErrorHandler.handle(e)
            Resource.Error(error.userMessage, error)
        }
    }

    override suspend fun logAnonymousView(linkId: String) {
        try {
            val link = shareLinks[linkId] ?: return
            shareLinks[linkId] = link.copy(accessCount = link.accessCount + 1)
        } catch (e: Exception) {
            // Ignore logging errors
        }
    }
}
