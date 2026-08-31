package com.siraj.app.domain.repository.share

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.share.ShareLink
import com.siraj.app.domain.models.share.ShareType

interface ShareRepository {
    /**
     * Creates a new share link for the specified content.
     */
    suspend fun createShareLink(
        targetId: String,
        type: ShareType,
        userId: String,
        isPrivate: Boolean,
        expiresAt: Long? = null,
    ): Resource<ShareLink>

    /**
     * Validates a deep link by its ID and optional token.
     * Returns the ShareLink object if valid.
     */
    suspend fun getAndValidateShareLink(
        linkId: String,
        token: String? = null,
    ): Resource<ShareLink>

    /**
     * Revokes an existing share link (owner only).
     */
    suspend fun revokeShareLink(
        linkId: String,
        userId: String,
    ): Resource<Unit>

    /**
     * Logs anonymous view on a share link.
     */
    suspend fun logAnonymousView(linkId: String)
}
