package com.siraj.app.domain.repository.copilot

import com.siraj.app.domain.models.copilot.*
import kotlinx.coroutines.flow.Flow

/**
 * مستودع المساعد الإسلامي الذكي
 * يدمج: القرآن + الحديث + التفسير + الفقه
 */
interface CopilotRepository {

    /**
     * إرسال سؤال والحصول على رد بمصادر موثّقة
     */
    suspend fun ask(query: CopilotQuery): Flow<CopilotResponse>

    /**
     * البحث الدلالي في المصادر الإسلامية
     */
    suspend fun semanticSearch(query: String, language: String): List<CopilotSource>

    /**
     * حفظ واسترجاع المحادثات
     */
    suspend fun saveConversation(conversation: CopilotConversation)
    suspend fun getConversations(): List<CopilotConversation>
    suspend fun getConversation(id: String): CopilotConversation?

    /**
     * أسئلة مقترحة حسب السياق
     */
    suspend fun getSuggestedQuestions(): List<String>

    /**
     * تصفية المصادر حسب النوع
     */
    suspend fun getSourcesByType(type: CopilotSourceType): List<CopilotSource>
}
