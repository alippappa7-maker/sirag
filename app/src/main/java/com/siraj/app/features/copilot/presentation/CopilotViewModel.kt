package com.siraj.app.features.copilot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.BuildConfig
import com.siraj.app.data.repository.CopilotRepositoryImpl
import com.siraj.app.domain.models.copilot.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CopilotUiState(
    val messages: List<CopilotMessage> = emptyList(),
    val suggestedQuestions: List<String> = emptyList(),
    val isThinking: Boolean = false,
    val conversationId: String? = null,
    val hasGeminiKey: Boolean = false,
)

class CopilotViewModel : ViewModel() {

    private val repository = CopilotRepositoryImpl().apply {
        geminiApiKey = BuildConfig.GEMINI_API_KEY
    }

    private val _uiState = MutableStateFlow(CopilotUiState())
    val uiState: StateFlow<CopilotUiState> = _uiState.asStateFlow()

    init {
        loadSuggestions()
        addWelcomeMessage()
        _uiState.value = _uiState.value.copy(
            hasGeminiKey = BuildConfig.GEMINI_API_KEY.isNotBlank(),
        )
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            val questions = repository.getSuggestedQuestions()
            _uiState.value = _uiState.value.copy(suggestedQuestions = questions)
        }
    }

    private fun addWelcomeMessage() {
        val hasKey = BuildConfig.GEMINI_API_KEY.isNotBlank()
        val welcomeText = if (hasKey) {
            "السلام عليكم. أنا مساعدك الإسلامي المعرفي الذكي.\n\n" +
            "اسألني أي سؤال عن القرآن، الحديث، التفسير، الأدعية، أو أي موضوع إسلامي.\n" +
            "أبحث في Quran.com و UmmahAPI، ثم أصيغ لك إجابة ذكية بمصادر موثّقة."
        } else {
            "السلام عليكم. أنا مساعدك الإسلامي المعرفي.\n\n" +
            "اسألني أي سؤال عن القرآن، الحديث، التفسير، أو أي موضوع إسلامي.\n" +
            "كل رد مدعوم بمصادر موثّقة من Quran.com و UmmahAPI."
        }

        val welcome = CopilotMessage(
            id = "welcome",
            role = CopilotRole.ASSISTANT,
            content = welcomeText,
            sources = emptyList(),
            timestamp = System.currentTimeMillis(),
        )
        _uiState.value = _uiState.value.copy(messages = listOf(welcome))
    }

    fun askQuestion(question: String) {
        if (question.isBlank()) return

        val userMessage = CopilotMessage(
            id = System.currentTimeMillis().toString(),
            role = CopilotRole.USER,
            content = question,
            sources = emptyList(),
            timestamp = System.currentTimeMillis(),
        )

        val loadingMessage = CopilotMessage(
            id = "loading_${System.currentTimeMillis()}",
            role = CopilotRole.ASSISTANT,
            content = "",
            sources = emptyList(),
            timestamp = System.currentTimeMillis(),
            isLoading = true,
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage + loadingMessage,
            isThinking = true,
        )

        viewModelScope.launch {
            try {
                val response = repository.ask(
                    CopilotQuery(text = question)
                )
                response.collect { copilotResponse ->
                    val assistantMessage = CopilotMessage(
                        id = "answer_${System.currentTimeMillis()}",
                        role = CopilotRole.ASSISTANT,
                        content = copilotResponse.answer,
                        sources = copilotResponse.sources,
                        timestamp = System.currentTimeMillis(),
                    )

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages.filter { !it.isLoading } + assistantMessage,
                        isThinking = false,
                        suggestedQuestions = copilotResponse.followUpQuestions,
                    )
                }
            } catch (e: Exception) {
                val errorMessage = CopilotMessage(
                    id = "error_${System.currentTimeMillis()}",
                    role = CopilotRole.ASSISTANT,
                    content = "عذراً، حدث خطأ أثناء معالجة سؤالك. يرجى المحاولة مرة أخرى.",
                    sources = emptyList(),
                    timestamp = System.currentTimeMillis(),
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.filter { !it.isLoading } + errorMessage,
                    isThinking = false,
                )
            }
        }
    }

    fun clearConversation() {
        addWelcomeMessage()
        loadSuggestions()
    }
}
