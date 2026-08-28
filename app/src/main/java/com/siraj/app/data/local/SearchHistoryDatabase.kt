package com.siraj.app.data.local

import android.content.Context
import androidx.room.*
import com.siraj.app.domain.models.search.SearchCategory
import com.siraj.app.domain.models.search.SearchHistoryItem
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val id: String,
    val query: String,
    val category: String,
    val timestamp: Long,
    val resultCount: Int,
    val userId: String?
) {
    fun toDomain(): SearchHistoryItem {
        val cat = try {
            SearchCategory.valueOf(category)
        } catch (_: Exception) {
            SearchCategory.ALL
        }
        return SearchHistoryItem(
            id = id,
            query = query,
            category = cat,
            timestamp = timestamp,
            resultCount = resultCount,
            userId = userId
        )
    }

    companion object {
        fun fromDomain(item: SearchHistoryItem): SearchHistoryEntity {
            return SearchHistoryEntity(
                id = item.id,
                query = item.query,
                category = item.category.name,
                timestamp = item.timestamp,
                resultCount = item.resultCount,
                userId = item.userId
            )
        }
    }
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history WHERE (userId = :userId OR (userId IS NULL AND :userId IS NULL)) ORDER BY timestamp DESC LIMIT 20")
    fun observeHistory(userId: String?): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query AND (userId = :userId OR (userId IS NULL AND :userId IS NULL))")
    suspend fun deleteByQuery(query: String, userId: String?)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM search_history WHERE (userId = :userId OR (userId IS NULL AND :userId IS NULL))")
    suspend fun clearAll(userId: String?)

    @Query("DELETE FROM search_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)
}

@Database(
    entities = [SearchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SearchHistoryDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: SearchHistoryDatabase? = null

        fun getInstance(context: Context): SearchHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SearchHistoryDatabase::class.java,
                    "siraj_search_history.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
