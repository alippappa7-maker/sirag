package com.siraj.app.data.repository.support

import com.siraj.app.domain.models.support.*
import com.siraj.app.domain.repository.support.SupportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirebaseSupportRepositoryImpl : SupportRepository {
    override fun getArticlesStream(query: String?, category: HelpCategory?): Flow<List<HelpArticle>> = flow { emit(emptyList()) }
    override fun getArticle(articleId: String): Flow<HelpArticle?> = flow { emit(null) }
    override suspend fun voteArticle(articleId: String, helpful: Boolean): Result<Unit> = Result.success(Unit)
    override fun getUserTicketsStream(userId: String): Flow<List<SupportTicket>> = flow { emit(emptyList()) }
    override fun getTicketStream(ticketId: String): Flow<SupportTicket?> = flow { emit(null) }
    override suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket> = Result.success(ticket)
    override suspend fun addReply(ticketId: String, reply: TicketReply): Result<Unit> = Result.success(Unit)
    override suspend fun updateTicketStatus(ticketId: String, status: TicketStatus, note: String?): Result<Unit> = Result.success(Unit)
    override suspend fun rateTicket(ticketId: String, stars: Int, feedback: String?): Result<Unit> = Result.success(Unit)
    override suspend fun generateSafeDiagnostics(): SafeDiagnosticsLog = SafeDiagnosticsLog(
        appVersion = "1.0.0",
        buildNumber = "1",
        osVersion = "Android",
        deviceModel = "Device",
        networkState = "ONLINE",
        memoryAvailableMb = 512L,
        sanitizedLogs = emptyList()
    )
}
