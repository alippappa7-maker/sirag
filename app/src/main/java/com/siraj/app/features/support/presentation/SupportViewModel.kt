package com.siraj.app.features.support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.support.*
import com.siraj.app.domain.repository.support.SupportRepository
import com.siraj.app.data.repository.support.FirebaseSupportRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SupportUiState(
    val articles: List<HelpArticle> = emptyList(),
    val selectedCategory: HelpCategory? = null,
    val searchQuery: String = "",
    val myTickets: List<SupportTicket> = emptyList(),
    val selectedTicket: SupportTicket? = null,
    val selectedArticle: HelpArticle? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val safeLogsPreview: SafeDiagnosticsLog? = null,
    val isSafeLogsAttached: Boolean = true,
    // Ticket creation form state
    val formCategory: TicketCategory = TicketCategory.GENERAL_INQUIRY,
    val formSubject: String = "",
    val formDescription: String = "",
    val formPriority: TicketPriority = TicketPriority.NORMAL,
    val formShariaRef: String = "",
    val formBillingTxId: String = "",
    val formProjectId: String = "",
    val replyInput: String = "",
    val ratingStars: Int = 5,
    val ratingFeedback: String = ""
)

class SupportViewModel(
    private val repository: SupportRepository,
    private val currentUserId: String = "user_current",
    private val currentUserEmail: String = "alippappa7@gmail.com",
    private val currentUserName: String = "مستخدم سراج"
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<HelpCategory?>(null)
    private val _selectedTicketId = MutableStateFlow<String?>(null)
    private val _selectedArticleId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadDiagnostics()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            combine(
                combine(
                    repository.getArticlesStream(),
                    repository.getUserTicketsStream(currentUserId),
                    _searchQuery
                ) { articles, tickets, query -> Triple(articles, tickets, query) },
                combine(
                    _selectedCategory,
                    _selectedTicketId,
                    _selectedArticleId
                ) { category, ticketId, articleId -> Triple(category, ticketId, articleId) }
            ) { (articles, tickets, query), (category, ticketId, articleId) ->
                val filteredArticles = articles.filter { article ->
                    val matchesCategory = category == null || article.category == category
                    val matchesQuery = query.isBlank() ||
                            article.title.contains(query, ignoreCase = true) ||
                            article.summary.contains(query, ignoreCase = true) ||
                            article.content.contains(query, ignoreCase = true) ||
                            article.tags.any { it.contains(query, ignoreCase = true) }
                    matchesCategory && matchesQuery
                }

                val currentTicket = tickets.find { it.id == ticketId }
                val currentArticle = articles.find { it.id == articleId }

                _uiState.value.copy(
                    articles = filteredArticles,
                    myTickets = tickets,
                    searchQuery = query,
                    selectedCategory = category,
                    selectedTicket = currentTicket,
                    selectedArticle = currentArticle,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun loadDiagnostics() {
        viewModelScope.launch {
            try {
                val diagnostics = repository.generateSafeDiagnostics()
                _uiState.value = _uiState.value.copy(safeLogsPreview = diagnostics)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: HelpCategory?) {
        _selectedCategory.value = category
    }

    fun selectArticle(articleId: String?) {
        _selectedArticleId.value = articleId
    }

    fun selectTicket(ticketId: String?) {
        _selectedTicketId.value = ticketId
    }

    fun setFormCategory(category: TicketCategory) {
        _uiState.value = _uiState.value.copy(formCategory = category)
    }

    fun setFormSubject(subject: String) {
        _uiState.value = _uiState.value.copy(formSubject = subject)
    }

    fun setFormDescription(desc: String) {
        _uiState.value = _uiState.value.copy(formDescription = desc)
    }

    fun setFormPriority(priority: TicketPriority) {
        _uiState.value = _uiState.value.copy(formPriority = priority)
    }

    fun setFormShariaRef(ref: String) {
        _uiState.value = _uiState.value.copy(formShariaRef = ref)
    }

    fun setFormBillingTxId(txId: String) {
        _uiState.value = _uiState.value.copy(formBillingTxId = txId)
    }

    fun setFormProjectId(projId: String) {
        _uiState.value = _uiState.value.copy(formProjectId = projId)
    }

    fun setSafeLogsAttached(attached: Boolean) {
        _uiState.value = _uiState.value.copy(isSafeLogsAttached = attached)
    }

    fun setReplyInput(input: String) {
        _uiState.value = _uiState.value.copy(replyInput = input)
    }

    fun setRatingStars(stars: Int) {
        _uiState.value = _uiState.value.copy(ratingStars = stars)
    }

    fun setRatingFeedback(feedback: String) {
        _uiState.value = _uiState.value.copy(ratingFeedback = feedback)
    }

    fun submitTicket(onSuccess: (SupportTicket) -> Unit) {
        val state = _uiState.value
        val ticket = SupportTicket(
            ticketNumber = "",
            userId = currentUserId,
            userEmail = currentUserEmail,
            userName = currentUserName,
            category = state.formCategory,
            priority = state.formPriority,
            subject = state.formSubject,
            description = state.formDescription,
            targetTeam = state.formCategory.defaultTeam,
            shariaSurahOrHadithRef = if (state.formCategory == TicketCategory.SHARIA_CONTENT_ERROR) state.formShariaRef.ifBlank { null } else null,
            billingTransactionId = if (state.formCategory == TicketCategory.PAYMENT_AND_BILLING) state.formBillingTxId.ifBlank { null } else null,
            relatedProjectId = state.formProjectId.ifBlank { null },
            safeLogs = if (state.isSafeLogsAttached) state.safeLogsPreview else null
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.createTicket(ticket)
            if (result.isSuccess) {
                val created = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "تم إنشاء التذكرة بنجاح برقم ${created.ticketNumber}",
                    formSubject = "",
                    formDescription = "",
                    formShariaRef = "",
                    formBillingTxId = ""
                )
                _selectedTicketId.value = created.id
                onSuccess(created)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "فشل إنشاء التذكرة"
                )
            }
        }
    }

    fun sendReply(ticketId: String) {
        val message = _uiState.value.replyInput.trim()
        if (message.isBlank()) return

        val reply = TicketReply(
            ticketId = ticketId,
            authorId = currentUserId,
            authorName = currentUserName,
            authorRole = ReplyAuthorRole.USER,
            message = message
        )

        viewModelScope.launch {
            val result = repository.addReply(ticketId, reply)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(replyInput = "")
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "فشل إرسال الرد"
                )
            }
        }
    }

    fun closeTicket(ticketId: String) {
        viewModelScope.launch {
            repository.updateTicketStatus(ticketId, TicketStatus.CLOSED, "قام المستخدم بإغلاق التذكرة")
        }
    }

    fun submitRating(ticketId: String) {
        val stars = _uiState.value.ratingStars
        val feedback = _uiState.value.ratingFeedback
        viewModelScope.launch {
            val result = repository.rateTicket(ticketId, stars, feedback.ifBlank { null })
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    successMessage = "شكراً لتقييمك! نسعى دائماً لخدمتكم على أكمل وجه."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "فشل إرسال التقييم"
                )
            }
        }
    }

    fun voteArticle(articleId: String, helpful: Boolean) {
        viewModelScope.launch {
            repository.voteArticle(articleId, helpful)
            _uiState.value = _uiState.value.copy(
                successMessage = if (helpful) "شكراً لملاحظتك الإيجابية!" else "شكراً، سنعمل على تحسين هذا المقال."
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}

class SupportViewModelFactory(
    private val repository: SupportRepository = FirebaseSupportRepositoryImpl(),
    private val currentUserId: String = "user_current",
    private val currentUserEmail: String = "alippappa7@gmail.com",
    private val currentUserName: String = "مستخدم سراج"
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SupportViewModel(repository, currentUserId, currentUserEmail, currentUserName) as T
    }
}
