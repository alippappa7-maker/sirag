package com.siraj.app.features.search

import com.siraj.app.core.utils.ArabicSearchUtils
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.local.SearchHistoryDao
import com.siraj.app.data.local.SearchHistoryEntity
import com.siraj.app.data.repository.search.UnifiedSearchRepositoryImpl
import com.siraj.app.domain.models.*
import com.siraj.app.domain.models.audio.*
import com.siraj.app.domain.models.quran.Ayah
import com.siraj.app.domain.models.quran.Surah
import com.siraj.app.domain.models.search.*
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.repository.QuranRepository
import com.siraj.app.domain.repository.TemplateRepository
import com.siraj.app.domain.repository.audio.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UnifiedSearchTest {
    private lateinit var fakeQuranRepository: FakeQuranRepository
    private lateinit var fakeAudioRepository: FakeAudioRepository
    private lateinit var fakeTemplateRepository: FakeTemplateRepository
    private lateinit var fakeProjectRepository: FakeProjectRepository
    private lateinit var fakeHistoryDao: FakeSearchHistoryDao
    private lateinit var searchRepository: UnifiedSearchRepositoryImpl

    @Before
    fun setup() {
        fakeQuranRepository = FakeQuranRepository()
        fakeAudioRepository = FakeAudioRepository()
        fakeTemplateRepository = FakeTemplateRepository()
        fakeProjectRepository = FakeProjectRepository()
        fakeHistoryDao = FakeSearchHistoryDao()

        searchRepository =
            UnifiedSearchRepositoryImpl(
                quranRepository = fakeQuranRepository,
                audioRepository = fakeAudioRepository,
                templateRepository = fakeTemplateRepository,
                projectRepository = fakeProjectRepository,
                historyDao = fakeHistoryDao,
            )
    }

    @Test
    fun testArabicTextNormalization_removesDiacriticsAndUnifiesForms() {
        val inputWithDiacritics = "سُورَةُ الإِخْلَاصِ"
        val normalized = ArabicSearchUtils.normalizeArabic(inputWithDiacritics)
        assertEquals("سوره الاخلاص", normalized)

        val alefVariations = "أحمد إبراهيم آمنة ٱلله"
        val normAlef = ArabicSearchUtils.normalizeArabic(alefVariations)
        assertEquals("احمد ابراهيم امنه الله", normAlef)
    }

    @Test
    fun testSearch_findsQuranSurahsAndVerses() =
        runBlocking {
            val result =
                searchRepository.search(
                    query = "البقرة",
                    filter = SearchFilter(category = SearchCategory.ALL),
                    page = 1,
                    pageSize = 20,
                )

            assertTrue(result is Resource.Success)
            val data = (result as Resource.Success).data
            assertTrue(data.items.isNotEmpty())
            assertTrue(data.items.any { it.category == SearchCategory.QURAN && it.title.contains("البقرة") })
        }

    @Test
    fun testSearch_filtersByAudioCategory() =
        runBlocking {
            val result =
                searchRepository.search(
                    query = "العفاسي",
                    filter = SearchFilter(category = SearchCategory.AUDIO),
                    page = 1,
                    pageSize = 20,
                )

            assertTrue(result is Resource.Success)
            val data = (result as Resource.Success).data
            assertTrue(data.items.all { it.category == SearchCategory.AUDIO })
        }

    @Test
    fun testSearch_privateProjectsStrictlyIsolated() =
        runBlocking {
            // Without user id -> Should not return private project
            val anonResult =
                searchRepository.search(
                    query = "مشروع سراج الأول",
                    filter = SearchFilter(category = SearchCategory.PROJECT),
                    page = 1,
                    pageSize = 20,
                    userId = null,
                    workspaceId = null,
                )
            val anonData = (anonResult as Resource.Success).data
            assertTrue(anonData.items.isEmpty())

            // With owner user id -> Should return private project
            val ownerResult =
                searchRepository.search(
                    query = "مشروع سراج الأول",
                    filter = SearchFilter(category = SearchCategory.PROJECT),
                    page = 1,
                    pageSize = 20,
                    userId = "user_123",
                    workspaceId = "ws_123",
                )
            val ownerData = (ownerResult as Resource.Success).data
            assertEquals(1, ownerData.items.size)
            assertTrue(ownerData.items.first().isPrivate)
            assertEquals(
                "مشاريعي",
                ownerData.items
                    .first()
                    .category.titleArabic,
            )
        }

    @Test
    fun testSearch_verifiedSourcesCatalog() =
        runBlocking {
            val result =
                searchRepository.search(
                    query = "البخاري",
                    filter = SearchFilter(category = SearchCategory.SOURCE),
                    page = 1,
                    pageSize = 20,
                )

            assertTrue(result is Resource.Success)
            val data = (result as Resource.Success).data
            assertTrue(data.items.isNotEmpty())
            val bukhariItem = data.items.first { it.title.contains("البخاري") }
            assertEquals("صحيح البخاري", bukhariItem.title)
            assertTrue(bukhariItem.isVerified)
            assertEquals("موثق ومعتمد", bukhariItem.verificationStatus)
        }

    @Test
    fun testSearch_historyManagement() =
        runBlocking {
            // Perform a search to record history
            searchRepository.search(
                query = "أذكار الصباح",
                filter = SearchFilter(),
                page = 1,
                pageSize = 10,
                userId = "user_123",
            )

            assertEquals(1, fakeHistoryDao.savedList.size)
            assertEquals("أذكار الصباح", fakeHistoryDao.savedList.first().query)

            // Delete single item
            val itemId = fakeHistoryDao.savedList.first().id
            searchRepository.deleteHistoryItem(itemId)
            assertTrue(fakeHistoryDao.savedList.isEmpty())

            // Add two queries and clear all
            searchRepository.recordSearchQuery("سورة يس", SearchCategory.QURAN, 1, "user_123")
            searchRepository.recordSearchQuery("تفسير السعدي", SearchCategory.SOURCE, 1, "user_123")
            assertEquals(2, fakeHistoryDao.savedList.size)

            searchRepository.clearAllHistory("user_123")
            assertTrue(fakeHistoryDao.savedList.isEmpty())
        }

    @Test
    fun testSearch_suggestionsReturnRelevantTopics() =
        runBlocking {
            val suggestions = searchRepository.getSuggestions("سورة")
            assertTrue(suggestions.isNotEmpty())
            assertTrue(suggestions.any { it.text.startsWith("سورة") })
        }
}

// Fakes for testing
private class FakeQuranRepository : QuranRepository {
    override suspend fun getSurahs(): Resource<List<Surah>> =
        Resource.Success(
            listOf(
                Surah(chapterNumber = 1, nameArabic = "الفاتحة", nameTranslated = "The Opening", revelationPlace = "مكية", versesCount = 7),
                Surah(chapterNumber = 2, nameArabic = "البقرة", nameTranslated = "The Cow", revelationPlace = "مدنية", versesCount = 286),
                Surah(chapterNumber = 36, nameArabic = "يس", nameTranslated = "Ya-Sin", revelationPlace = "مكية", versesCount = 83),
                Surah(
                    chapterNumber = 112,
                    nameArabic = "الإخلاص",
                    nameTranslated = "The Sincerity",
                    revelationPlace = "مكية",
                    versesCount = 4,
                ),
            ),
        )

    override suspend fun getAyahs(surahId: Int): Resource<List<Ayah>> = Resource.Success(emptyList())

    override suspend fun toggleBookmark(
        verseKey: String,
        surahId: Int,
        verseNumber: Int,
        isBookmarked: Boolean,
    ) {}

    override suspend fun saveNote(
        verseKey: String,
        surahId: Int,
        verseNumber: Int,
        note: String,
    ) {}

    override fun getBookmarkedVerseKeys(): Flow<List<String>> = flowOf(emptyList())
}

private class FakeAudioRepository : AudioRepository {
    override suspend fun getTracks(
        filter: AudioFilter,
        page: Int,
        pageSize: Int,
    ): Resource<List<AudioTrack>> {
        val all =
            listOf(
                AudioTrack(
                    id = "track_1",
                    title = "تلاوة سورة الكهف كاملة",
                    speaker = "مشاري العفاسي",
                    category = "recitation",
                    source = "مصحف الحرمين",
                    durationSeconds = 1800,
                    verificationStatus = AudioVerificationStatus.APPROVED,
                    rights = AudioRights(licenseType = "Free", sourceUrl = "https://example.com/audio1"),
                ),
                AudioTrack(
                    id = "track_2",
                    title = "شرح كتاب التوحيد",
                    speaker = "الشيخ صالح الفوزان",
                    category = "lesson",
                    source = "إذاعة القرآن",
                    durationSeconds = 2400,
                    verificationStatus = AudioVerificationStatus.APPROVED,
                    rights = AudioRights(licenseType = "Free", sourceUrl = "https://example.com/audio2"),
                ),
            )
        return Resource.Success(all)
    }

    override suspend fun toggleFavorite(trackId: String): Resource<Boolean> = Resource.Success(true)

    override suspend fun updateProgress(
        trackId: String,
        progressSeconds: Int,
    ): Resource<Boolean> = Resource.Success(true)

    override suspend fun reportTrack(
        trackId: String,
        reason: String,
    ): Resource<Boolean> = Resource.Success(true)
}

private class FakeTemplateRepository : TemplateRepository {
    override fun getActiveTemplates(): Flow<Resource<List<ContentTemplate>>> =
        flowOf(
            Resource.Success(
                listOf(
                    ContentTemplate(
                        id = "tmpl_1",
                        name = "فيديو حديث شريف تيك توك",
                        description = "قالب مخصص للأحاديث النبوية القصيرة",
                        sceneStyle = "حديث نبوي",
                        recommendedPlatform = "TikTok",
                        status = TemplateStatus.ACTIVE,
                    ),
                ),
            ),
        )

    override fun getFavoriteTemplates(userId: String): Flow<Resource<List<String>>> = flowOf(Resource.Success(emptyList()))

    override suspend fun toggleFavorite(
        userId: String,
        templateId: String,
        isFavorite: Boolean,
    ): Resource<Unit> = Resource.Success(Unit)

    override suspend fun seedDefaultTemplates(): Resource<Unit> = Resource.Success(Unit)

    override suspend fun createTemplate(template: ContentTemplate): Resource<String> = Resource.Success(template.id)

    override suspend fun updateTemplateStatus(
        templateId: String,
        status: TemplateStatus,
    ): Resource<Unit> = Resource.Success(Unit)

    override suspend fun updateTemplate(template: ContentTemplate): Resource<Unit> = Resource.Success(Unit)
}

private class FakeProjectRepository : ProjectRepository {
    override fun getRecentProjects(
        workspaceId: String,
        limit: Int,
    ): Flow<Resource<List<Project>>> = flowOf(Resource.Success(emptyList()))

    override fun getAllProjects(workspaceId: String): Flow<Resource<List<Project>>> = flowOf(Resource.Success(emptyList()))

    override suspend fun getProjects(
        workspaceId: String,
        limit: Int,
        offset: Int,
        query: String,
        sortBy: String,
    ): Resource<List<Project>> =
        Resource.Success(
            listOf(
                Project(
                    id = "proj_101",
                    workspaceId = workspaceId,
                    ownerId = "user_123",
                    title = "مشروع سراج الأول",
                    description = "فيديو تعريفي بالمنصة والمحراب",
                    scenes = emptyList(),
                ),
            ),
        )

    override suspend fun getProject(projectId: String): Resource<Project> = Resource.Error("Not implemented")

    override suspend fun createProject(project: Project): Resource<String> = Resource.Success(project.id)

    override suspend fun updateProject(project: Project): Resource<Unit> = Resource.Success(Unit)

    override suspend fun deleteProject(projectId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun restoreProject(projectId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun archiveProject(projectId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun copyProject(
        projectId: String,
        newOwnerId: String,
    ): Resource<String> = Resource.Success("new_proj")

    override suspend fun logActivity(activity: ProjectActivity): Resource<Unit> = Resource.Success(Unit)

    override suspend fun getProjectActivities(projectId: String): Resource<List<ProjectActivity>> = Resource.Success(emptyList())

    override suspend fun createVersion(version: ProjectVersion): Resource<String> = Resource.Success("v1")

    override suspend fun getProjectVersions(projectId: String): Resource<List<ProjectVersion>> = Resource.Success(emptyList())
}

private class FakeSearchHistoryDao : SearchHistoryDao {
    val savedList = mutableListOf<SearchHistoryEntity>()

    override fun observeHistory(userId: String?): Flow<List<SearchHistoryEntity>> = flowOf(savedList.filter { it.userId == userId })

    override suspend fun insertHistory(item: SearchHistoryEntity) {
        savedList.add(item)
    }

    override suspend fun deleteByQuery(
        query: String,
        userId: String?,
    ) {
        savedList.removeAll { it.query == query && it.userId == userId }
    }

    override suspend fun deleteById(id: String) {
        savedList.removeAll { it.id == id }
    }

    override suspend fun clearAll(userId: String?) {
        savedList.removeAll { it.userId == userId }
    }

    override suspend fun deleteOlderThan(cutoffTime: Long) {
        savedList.removeAll { it.timestamp < cutoffTime }
    }
}
