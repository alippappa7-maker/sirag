package com.siraj.app.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey val verseKey: String, // e.g. "2:255"
    val chapterId: Int,
    val verseNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_notes")
data class QuranNoteEntity(
    @PrimaryKey val verseKey: String,
    val chapterId: Int,
    val verseNumber: Int,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface QuranDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: QuranBookmarkEntity)
    
    @Delete
    suspend fun deleteBookmark(bookmark: QuranBookmarkEntity)
    
    @Query("SELECT * FROM quran_bookmarks")
    fun getAllBookmarks(): Flow<List<QuranBookmarkEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuranNoteEntity)
    
    @Query("SELECT * FROM quran_notes WHERE verseKey = :verseKey")
    suspend fun getNote(verseKey: String): QuranNoteEntity?
    
    @Query("SELECT * FROM quran_notes")
    fun getAllNotes(): Flow<List<QuranNoteEntity>>
}

@Database(entities = [QuranBookmarkEntity::class, QuranNoteEntity::class], version = 1, exportSchema = false)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    
    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null
        
        fun getDatabase(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
