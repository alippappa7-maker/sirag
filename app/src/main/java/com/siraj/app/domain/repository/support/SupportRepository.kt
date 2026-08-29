package com.siraj.app.domain.repository.support

import com.siraj.app.domain.models.support.*
import kotlinx.coroutines.flow.Flow

/**
 * Interface for Help Center knowledge base and Support Tickets
 */
interface SupportRepository {
    fun getArticlesStream(query: String? = null, category: HelpCategory? = null): Flow<List<HelpArticle>>
    fun getArticle(articleId: String): Flow<HelpArticle?>
    suspend fun voteArticle(articleId: String, helpful: Boolean): Result<Unit>
    
    fun getUserTicketsStream(userId: String): Flow<List<SupportTicket>>
    fun getTicketStream(ticketId: String): Flow<SupportTicket?>
    
    suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket>
    suspend fun addReply(ticketId: String, reply: TicketReply): Result<Unit>
    suspend fun updateTicketStatus(ticketId: String, status: TicketStatus, note: String? = null): Result<Unit>
    suspend fun rateTicket(ticketId: String, stars: Int, feedback: String? = null): Result<Unit>
    
    suspend fun generateSafeDiagnostics(): SafeDiagnosticsLog
}
