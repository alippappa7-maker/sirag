package com.siraj.app.domain.repository.search

import com.siraj.app.core.utils.Resource
import com.siraj.app.domain.models.search.*
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    /**
     * تنفيذ بحث شامل وموحد عبر كافة الأقسام مع تطبيق الفلاتر والتصفح المقسم
     */
    suspend fun search(
        query: String,
        filter: SearchFilter,
        page: Int = 1,
        pageSize: Int = 20,
        userId: String? = null,
        workspaceId: String? = null
    ): Resource<GlobalSearchResult>

    /**
     * اقتراحات البحث التلقائية السريعة أثناء الكتابة
     */
    suspend fun getSuggestions(query: String, limit: Int = 8): List<SearchSuggestion>

    /**
     * مراقبة سجل البحث المحلي للمستخدم
     */
    fun getSearchHistory(userId: String?): Flow<List<SearchHistoryItem>>

    /**
     * إضافة استعلام جديد إلى سجل البحث
     */
    suspend fun recordSearchQuery(
        query: String,
        category: SearchCategory,
        resultCount: Int,
        userId: String?
    )

    /**
     * حذف عنصر محدد من سجل البحث
     */
    suspend fun deleteHistoryItem(id: String)

    /**
     * مسح سجل البحث كاملاً للمستخدم
     */
    suspend fun clearAllHistory(userId: String?)
}
