package com.siraj.app.data.repository.search

import com.siraj.app.core.navigation.Screen
import com.siraj.app.core.utils.ArabicSearchUtils
import com.siraj.app.core.utils.Resource
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.data.local.SearchHistoryDao
import com.siraj.app.data.local.SearchHistoryEntity
import com.siraj.app.domain.models.TemplateStatus
import com.siraj.app.domain.models.audio.AudioFilter
import com.siraj.app.domain.models.audio.AudioVerificationStatus
import com.siraj.app.domain.models.quran.Surah
import com.siraj.app.domain.models.search.*
import com.siraj.app.domain.repository.ProjectRepository
import com.siraj.app.domain.repository.QuranRepository
import com.siraj.app.domain.repository.TemplateRepository
import com.siraj.app.domain.repository.audio.AudioRepository
import com.siraj.app.domain.repository.search.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class UnifiedSearchRepositoryImpl(
    private val quranRepository: QuranRepository,
    private val audioRepository: AudioRepository,
    private val templateRepository: TemplateRepository,
    private val projectRepository: ProjectRepository,
    private val historyDao: SearchHistoryDao,
) : SearchRepository {
    // Verified Islamic Sources Catalog
    private val verifiedSourcesCatalog =
        listOf(
            SearchResultItem(
                id = "src_bukhari",
                title = "صحيح البخاري",
                snippet = "الجامع المسند الصحيح المختصر من أمور رسول الله صلى الله عليه وسلم وسننه وأيامه للإمام محمد بن إسماعيل البخاري.",
                category = SearchCategory.SOURCE,
                sourceName = "دار طوق النجاة (طبعة معتمدة)",
                authorOrReciter = "الإمام البخاري (ت 256هـ)",
                referenceUrl = "https://sunnah.com/bukhari",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Details.createRoute("src_bukhari"),
                extraMetadata = mapOf("grade" to "صحيح مجمع عليه", "type" to "حديث نبوي"),
            ),
            SearchResultItem(
                id = "src_muslim",
                title = "صحيح مسلم",
                snippet = "المسند الصحيح المختصر بنقل العدل عن العدل إلى رسول الله صلى الله عليه وسلم للإمام مسلم بن الحجاج النيسابوري.",
                category = SearchCategory.SOURCE,
                sourceName = "دار إحياء التراث العربي",
                authorOrReciter = "الإمام مسلم (ت 261هـ)",
                referenceUrl = "https://sunnah.com/muslim",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Details.createRoute("src_muslim"),
                extraMetadata = mapOf("grade" to "صحيح مجمع عليه", "type" to "حديث نبوي"),
            ),
            SearchResultItem(
                id = "src_ibn_kathir",
                title = "تفسير القرآن العظيم (تفسير ابن كثير)",
                snippet = "عمدة التفاسير المأثورة للعلامة الحافظ عماد الدين إسماعيل بن عمر بن كثير القرشي الدمشقي، يفسر القرآن بالقرآن والسنة وآثار الصحابة.",
                category = SearchCategory.SOURCE,
                sourceName = "دار طيبة للنشر والتوزيع",
                authorOrReciter = "ابن كثير (ت 774هـ)",
                referenceUrl = "https://quran.ksu.edu.sa/tafseer/katheer",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Details.createRoute("src_ibn_kathir"),
                extraMetadata = mapOf("type" to "تفسير القرآن"),
            ),
            SearchResultItem(
                id = "src_saadi",
                title = "تيسير الكريم الرحمن في تفسير كلام المنان (تفسير السعدي)",
                snippet = "تفسير ميسر وسهل العبارة للعلامة عبد الرحمن بن ناصر السعدي، مع التركيز على المعاني الإيمانية والتربوية والأحكام.",
                category = SearchCategory.SOURCE,
                sourceName = "مؤسسة الرسالة",
                authorOrReciter = "الشيخ عبد الرحمن السعدي (ت 1376هـ)",
                referenceUrl = "https://quran.ksu.edu.sa/tafseer/saadi",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Details.createRoute("src_saadi"),
                extraMetadata = mapOf("type" to "تفسير القرآن"),
            ),
            SearchResultItem(
                id = "src_nawawi_40",
                title = "الأربعون النووية",
                snippet = "الأحاديث النبوية الجامعة التي عليها مدار الدين للإمام يحيى بن شرف النووي مع الشرح والفوائد المسلكية.",
                category = SearchCategory.SOURCE,
                sourceName = "دار المنهاج للنشر",
                authorOrReciter = "الإمام النووي (ت 676هـ)",
                referenceUrl = "https://sunnah.com/nawawi40",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Details.createRoute("src_nawawi_40"),
                extraMetadata = mapOf("type" to "حديث نبوي"),
            ),
            SearchResultItem(
                id = "src_riyad_salihin",
                title = "رياض الصالحين من كلام سيد المرسلين",
                snippet = "كتاب جامع للأحاديث الصحيحة في الرقائق والآداب والأخلاق والعبادات والمعاملات.",
                category = SearchCategory.SOURCE,
                sourceName = "دار ابن كثير",
                authorOrReciter = "الإمام النووي (ت 676هـ)",
                referenceUrl = "https://sunnah.com/riyadussalihin",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Details.createRoute("src_riyad_salihin"),
                extraMetadata = mapOf("type" to "أخلاق وآداب"),
            ),
        )

    // Verified Flashes Catalog
    private val verifiedFlashesCatalog =
        listOf(
            SearchResultItem(
                id = "flash_1",
                title = "فضل الطهور وشطر الإيمان",
                snippet = "قال رسول الله ﷺ: (الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ، وَسُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ تَمْلآنِ مَا بَيْنَ السَّمَاوَاتِ وَالأَرْضِ).",
                category = SearchCategory.FLASH,
                sourceName = "صحيح مسلم (رقم 223)",
                authorOrReciter = "أبو مالك الأشعري رضي الله عنه",
                referenceUrl = "https://sunnah.com/muslim:223",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Flashes.route,
            ),
            SearchResultItem(
                id = "flash_2",
                title = "فضل الاستغفار وتفريج الكروب",
                snippet = "من لزم الاستغفار جعل الله له من كل هم فرجاً، ومن كل ضيق مخرجاً، ورزقه من حيث لا يحتسب.",
                category = SearchCategory.FLASH,
                sourceName = "سنن أبي داود (رقم 1518)",
                authorOrReciter = "عبد الله بن عباس رضي الله عنهما",
                referenceUrl = "https://sunnah.com/abudawud:1518",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Flashes.route,
            ),
            SearchResultItem(
                id = "flash_3",
                title = "خيركم من تعلم القرآن وعلمه",
                snippet = "عن عثمان بن عفان رضي الله عنه عن النبي ﷺ قال: (خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ).",
                category = SearchCategory.FLASH,
                sourceName = "صحيح البخاري (رقم 5027)",
                authorOrReciter = "عثمان بن عفان رضي الله عنه",
                referenceUrl = "https://sunnah.com/bukhari:5027",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Flashes.route,
            ),
            SearchResultItem(
                id = "flash_4",
                title = "صنائع المعروف تقي مصارع السوء",
                snippet = "صنائع المعروف تقي مصارع السوء والآفات والهلكات، وأهل المعروف في الدنيا هم أهل المعروف في الآخرة.",
                category = SearchCategory.FLASH,
                sourceName = "المستدرك على الصحيحين للحاكم (صحيح لغيره)",
                authorOrReciter = "أنس بن مالك رضي الله عنه",
                referenceUrl = "https://shamela.ws",
                verificationStatus = "موثق ومعتمد",
                isVerified = true,
                language = "العربية",
                targetRoute = Screen.Flashes.route,
            ),
        )

    override suspend fun search(
        query: String,
        filter: SearchFilter,
        page: Int,
        pageSize: Int,
        userId: String?,
        workspaceId: String?,
    ): Resource<GlobalSearchResult> =
        withContext(Dispatchers.IO) {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isEmpty() && filter.category == SearchCategory.ALL) {
                return@withContext Resource.Success(
                    GlobalSearchResult(query = "", totalCount = 0, items = emptyList()),
                )
            }

            try {
                val allMatchedItems = mutableListOf<SearchResultItem>()

                // 1. Search Quran (Surahs & Key Verses)
                if (filter.category == SearchCategory.ALL || filter.category == SearchCategory.QURAN) {
                    val quranItems = searchQuran(trimmedQuery)
                    allMatchedItems.addAll(quranItems)
                }

                // 2. Search Audio Library (Only Approved public content)
                if (filter.category == SearchCategory.ALL || filter.category == SearchCategory.AUDIO) {
                    val audioItems = searchAudio(trimmedQuery)
                    allMatchedItems.addAll(audioItems)
                }

                // 3. Search Flashes & Quotes
                if (filter.category == SearchCategory.ALL || filter.category == SearchCategory.FLASH) {
                    val flashItems = searchFlashes(trimmedQuery)
                    allMatchedItems.addAll(flashItems)
                }

                // 4. Search Templates (Active approved templates)
                if (filter.category == SearchCategory.ALL || filter.category == SearchCategory.TEMPLATE) {
                    val templateItems = searchTemplates(trimmedQuery)
                    allMatchedItems.addAll(templateItems)
                }

                // 5. Search Verified Sources & References
                if (filter.category == SearchCategory.ALL || filter.category == SearchCategory.SOURCE) {
                    val sourceItems = searchSources(trimmedQuery)
                    allMatchedItems.addAll(sourceItems)
                }

                // 6. Search User's Private Projects (ONLY if userId / workspaceId is provided, strictly isolated)
                if (!filter.onlyPrivateProjects &&
                    (filter.category == SearchCategory.ALL || filter.category == SearchCategory.PROJECT) ||
                    filter.onlyPrivateProjects
                ) {
                    if (!userId.isNullOrBlank() || !workspaceId.isNullOrBlank()) {
                        val projectItems = searchPrivateProjects(trimmedQuery, userId, workspaceId)
                        allMatchedItems.addAll(projectItems)
                    }
                }

                // Calculate category counts
                val categoryCounts = mutableMapOf<SearchCategory, Int>()
                SearchCategory.values().forEach { cat ->
                    categoryCounts[cat] =
                        if (cat == SearchCategory.ALL) {
                            allMatchedItems.size
                        } else {
                            allMatchedItems.count { it.category == cat }
                        }
                }

                // Apply Filters (Language, ContentType, Verification)
                var filteredItems =
                    allMatchedItems.filter { item ->
                        // Category Filter
                        val matchesCategory = (filter.category == SearchCategory.ALL) || (item.category == filter.category)

                        // Language Filter
                        val matchesLanguage =
                            when (filter.language) {
                                SearchLanguage.ALL -> true
                                SearchLanguage.ARABIC ->
                                    item.language.contains("عرب", ignoreCase = true) ||
                                        item.language.equals("ar", ignoreCase = true)
                                SearchLanguage.ENGLISH ->
                                    item.language.contains("انجليز", ignoreCase = true) ||
                                        item.language.equals("en", ignoreCase = true)
                                SearchLanguage.URDU ->
                                    item.language.contains("أرد", ignoreCase = true) ||
                                        item.language.equals("ur", ignoreCase = true)
                                SearchLanguage.FRENCH ->
                                    item.language.contains("فرنس", ignoreCase = true) ||
                                        item.language.equals("fr", ignoreCase = true)
                                SearchLanguage.OTHER -> true
                            }

                        // Content Type Filter
                        val matchesContentType =
                            when (filter.contentType) {
                                SearchContentType.ALL -> true
                                SearchContentType.TEXT -> item.category == SearchCategory.QURAN || item.category == SearchCategory.FLASH
                                SearchContentType.AUDIO -> item.category == SearchCategory.AUDIO
                                SearchContentType.VIDEO ->
                                    item.category == SearchCategory.PROJECT ||
                                        item.category == SearchCategory.TEMPLATE
                                SearchContentType.PROJECT -> item.category == SearchCategory.PROJECT
                                SearchContentType.TEMPLATE -> item.category == SearchCategory.TEMPLATE
                                SearchContentType.REFERENCE -> item.category == SearchCategory.SOURCE
                            }

                        // Verification Filter (Public search: never display unapproved content)
                        val matchesVerification =
                            when (filter.verificationFilter) {
                                SearchVerificationFilter.ALL_APPROVED -> item.isVerified || item.isPrivate
                                SearchVerificationFilter.VERIFIED_ONLY -> item.isVerified
                            }

                        matchesCategory && matchesLanguage && matchesContentType && matchesVerification
                    }

                // Apply Sorting
                filteredItems =
                    when (filter.sortOption) {
                        SearchSortOption.RELEVANCE -> {
                            filteredItems.sortedByDescending { item ->
                                ArabicSearchUtils.calculateScore(item.title, item.snippet, trimmedQuery)
                            }
                        }
                        SearchSortOption.NEWEST -> {
                            filteredItems.sortedByDescending { it.timestamp }
                        }
                        SearchSortOption.POPULAR -> {
                            filteredItems.sortedByDescending { item ->
                                // Prioritize Quran and Audio play count
                                when (item.category) {
                                    SearchCategory.QURAN -> 1000
                                    SearchCategory.AUDIO -> 500
                                    SearchCategory.SOURCE -> 300
                                    else -> 100
                                }
                            }
                        }
                    }

                val totalCount = filteredItems.size

                // Pagination calculation
                val startIndex = (page - 1) * pageSize
                val pagedItems =
                    if (startIndex >= totalCount) {
                        emptyList()
                    } else {
                        val endIndex = minOf(startIndex + pageSize, totalCount)
                        filteredItems.subList(startIndex, endIndex)
                    }

                val hasMore = (startIndex + pagedItems.size) < totalCount

                // Record search in local history asynchronously if query is not blank and first page
                if (trimmedQuery.isNotBlank() && page == 1) {
                    recordSearchQuery(trimmedQuery, filter.category, totalCount, userId)
                }

                Resource.Success(
                    GlobalSearchResult(
                        query = trimmedQuery,
                        totalCount = totalCount,
                        items = pagedItems,
                        categoryCounts = categoryCounts,
                        page = page,
                        hasMore = hasMore,
                    ),
                )
            } catch (e: Exception) {
                val error = ErrorHandler.handle(e)
                Resource.Error(error.userMessage, error)
            }
        }

    private suspend fun searchQuran(query: String): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val surahsRes = quranRepository.getSurahs()
        if (surahsRes is Resource.Success) {
            val surahs = surahsRes.data
            // Match Surahs by Arabic name, simple name, or chapter number
            surahs.forEach { surah ->
                if (query.isBlank() ||
                    ArabicSearchUtils.matches(surah.nameArabic, query) ||
                    surah.nameTranslated.contains(query, ignoreCase = true) ||
                    query == surah.chapterNumber.toString() ||
                    query == "سورة ${surah.nameArabic}"
                ) {
                    results.add(
                        SearchResultItem(
                            id = "quran_surah_${surah.chapterNumber}",
                            title = "سورة ${surah.nameArabic} (${surah.nameTranslated})",
                            snippet = "سورة ${surah.revelationPlace}، عدد آياتها ${surah.versesCount} آية. رقم السورة: ${surah.chapterNumber}.",
                            category = SearchCategory.QURAN,
                            sourceName = "المصحف الشريف (رواية حفص عن عاصم)",
                            authorOrReciter = "القرآن الكريم",
                            verificationStatus = "نص قرآني معتمد",
                            isVerified = true,
                            language = "العربية",
                            targetRoute = Screen.Surah.createRoute(surah.chapterNumber, surah.nameArabic),
                            extraMetadata =
                                mapOf(
                                    "surahId" to surah.chapterNumber.toString(),
                                    "versesCount" to surah.versesCount.toString(),
                                    "revelationPlace" to surah.revelationPlace,
                                ),
                        ),
                    )
                }
            }
        }

        // Search famous verses / Ayahs keywords (e.g. آية الكرسي, سورة الفاتحة, أواخر البقرة, خواتيم سورة الحشر)
        val famousAyahs =
            listOf(
                Triple(
                    "آية الكرسي - سورة البقرة (آية 255)",
                    "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ...",
                    2 to 255,
                ),
                Triple(
                    "سورة الإخلاص (التوحيد)",
                    "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                    112 to 1,
                ),
                Triple(
                    "سورة الفلق (المعوذات)",
                    "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ...",
                    113 to 1,
                ),
                Triple(
                    "سورة الناس (المعوذات)",
                    "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ...",
                    114 to 1,
                ),
                Triple(
                    "أواخر سورة البقرة (آية 285-286)",
                    "آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ ۚ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ...",
                    2 to 285,
                ),
                Triple(
                    "سورة الملك (المنجية من عذاب القبر)",
                    "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ۝ الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا...",
                    67 to 1,
                ),
            )

        famousAyahs.forEach { (title, text, location) ->
            if (query.isBlank() || ArabicSearchUtils.matches(title, query) || ArabicSearchUtils.matches(text, query)) {
                results.add(
                    SearchResultItem(
                        id = "quran_ayah_${location.first}_${location.second}",
                        title = title,
                        snippet = ArabicSearchUtils.extractSnippet(text, query),
                        category = SearchCategory.QURAN,
                        sourceName = "المصحف الشريف - سورة رقم ${location.first}",
                        authorOrReciter = "القرآن الكريم",
                        verificationStatus = "نص قرآني معتمد",
                        isVerified = true,
                        language = "العربية",
                        targetRoute = Screen.Surah.createRoute(location.first, title),
                        extraMetadata =
                            mapOf(
                                "surahId" to location.first.toString(),
                                "ayahNumber" to location.second.toString(),
                            ),
                    ),
                )
            }
        }

        return results
    }

    private suspend fun searchAudio(query: String): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        // Filter ONLY APPROVED public audio tracks
        val audioRes =
            audioRepository.getTracks(
                filter = AudioFilter(query = query, categoryId = "all"),
                page = 1,
                pageSize = 50,
            )
        if (audioRes is Resource.Success) {
            audioRes.data.filter { it.verificationStatus == AudioVerificationStatus.APPROVED }.forEach { track ->
                results.add(
                    SearchResultItem(
                        id = "audio_${track.id}",
                        title = track.title,
                        snippet = "تسجيل صوتي (${
                            when (track.category) {
                                "recitation" -> "تلاوة"
                                "lesson" -> "درس"
                                "lecture" -> "محاضرة"
                                "podcast" -> "بودكاست"
                                else -> track.category
                            }
                        }) للمتحدث: ${track.speaker}. المصدر: ${track.source}.",
                        category = SearchCategory.AUDIO,
                        sourceName = track.source,
                        authorOrReciter = track.speaker,
                        referenceUrl = track.rights.sourceUrl,
                        verificationStatus = "معتمد وموثق",
                        isVerified = true,
                        language = "العربية",
                        targetRoute = Screen.AudioPlayer.route,
                        durationText = String.format("%02d:%02d", track.durationSeconds / 60, track.durationSeconds % 60),
                        extraMetadata =
                            mapOf(
                                "trackId" to track.id,
                                "category" to track.category,
                                "license" to track.rights.licenseType,
                            ),
                    ),
                )
            }
        }
        return results
    }

    private fun searchFlashes(query: String): List<SearchResultItem> =
        verifiedFlashesCatalog.filter { flash ->
            query.isBlank() ||
                ArabicSearchUtils.matches(flash.title, query) ||
                ArabicSearchUtils.matches(flash.snippet, query) ||
                ArabicSearchUtils.matches(flash.sourceName, query) ||
                ArabicSearchUtils.matches(flash.authorOrReciter, query)
        }

    private suspend fun searchTemplates(query: String): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val templatesFlow = templateRepository.getActiveTemplates()
        val res = templatesFlow.firstOrNull()
        if (res is Resource.Success) {
            res.data.filter { it.status == TemplateStatus.ACTIVE }.forEach { template ->
                if (query.isBlank() ||
                    ArabicSearchUtils.matches(template.name, query) ||
                    ArabicSearchUtils.matches(template.description, query) ||
                    ArabicSearchUtils.matches(template.sceneStyle, query) ||
                    ArabicSearchUtils.matches(template.recommendedPlatform, query)
                ) {
                    results.add(
                        SearchResultItem(
                            id = "template_${template.id}",
                            title = "قالب: ${template.name}",
                            snippet =
                                template.description.ifBlank {
                                    "قالب إنتاج فيديو لمنصة ${template.recommendedPlatform} بأسلوب ${template.sceneStyle}."
                                },
                            category = SearchCategory.TEMPLATE,
                            sourceName = "منصة سراج للإنتاج",
                            authorOrReciter = "فريق سراج المعتمد",
                            verificationStatus = "قالب جاهز معتمد",
                            isVerified = true,
                            language = "العربية",
                            targetRoute = Screen.Ideation.route,
                            extraMetadata =
                                mapOf(
                                    "templateId" to template.id,
                                    "platform" to template.recommendedPlatform,
                                    "style" to template.sceneStyle,
                                ),
                        ),
                    )
                }
            }
        }
        return results
    }

    private fun searchSources(query: String): List<SearchResultItem> =
        verifiedSourcesCatalog.filter { src ->
            query.isBlank() ||
                ArabicSearchUtils.matches(src.title, query) ||
                ArabicSearchUtils.matches(src.snippet, query) ||
                ArabicSearchUtils.matches(src.authorOrReciter, query) ||
                ArabicSearchUtils.matches(src.sourceName, query)
        }

    private suspend fun searchPrivateProjects(
        query: String,
        userId: String?,
        workspaceId: String?,
    ): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val wsId = workspaceId ?: userId ?: return results

        val projectsRes =
            projectRepository.getProjects(
                workspaceId = wsId,
                limit = 30,
                offset = 0,
                query = query,
            )

        if (projectsRes is Resource.Success) {
            projectsRes.data.forEach { project ->
                results.add(
                    SearchResultItem(
                        id = "project_${project.id}",
                        title = project.title.ifBlank { "مشروع بدون عنوان" },
                        snippet = project.description.ifBlank { "مشروع فيديو خاص ضمن مساحة العمل. المشاهد: ${project.scenes.size} مشاهد." },
                        category = SearchCategory.PROJECT,
                        sourceName = "مساحة العمل الخاصة بي",
                        authorOrReciter = "أنا (مالك المشروع)",
                        verificationStatus = "خاص بي",
                        isVerified = false,
                        isPrivate = true,
                        language = "العربية",
                        timestamp = project.updatedAt,
                        targetRoute = Screen.ProjectEditor.createRoute(project.id),
                        extraMetadata = mapOf("projectId" to project.id, "scenesCount" to project.scenes.size.toString()),
                    ),
                )
            }
        }
        return results
    }

    override suspend fun getSuggestions(
        query: String,
        limit: Int,
    ): List<SearchSuggestion> =
        withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            val suggestions = mutableListOf<SearchSuggestion>()

            // Popular Islamic quick search suggestions when blank
            if (trimmed.isBlank()) {
                return@withContext listOf(
                    SearchSuggestion("سورة الكهف", SearchCategory.QURAN),
                    SearchSuggestion("أذكار الصباح والمساء", SearchCategory.AUDIO),
                    SearchSuggestion("آية الكرسي", SearchCategory.QURAN),
                    SearchSuggestion("صحيح البخاري", SearchCategory.SOURCE),
                    SearchSuggestion("تفسير ابن كثير", SearchCategory.SOURCE),
                    SearchSuggestion("فضل الصدقة", SearchCategory.FLASH),
                    SearchSuggestion("شرح الأربعين النووية", SearchCategory.AUDIO),
                    SearchSuggestion("قوالب تيك توك وريلز", SearchCategory.TEMPLATE),
                )
            }

            // Suggestions based on Surahs
            val surahsRes = quranRepository.getSurahs()
            if (surahsRes is Resource.Success) {
                val surahQuery = if (trimmed.startsWith("سورة")) trimmed.removePrefix("سورة").trim() else trimmed
                val surahMatches =
                    if (surahQuery.isBlank()) {
                        surahsRes.data
                    } else {
                        surahsRes.data.filter {
                            ArabicSearchUtils.matches(it.nameArabic, surahQuery) ||
                                it.nameTranslated.contains(surahQuery, ignoreCase = true)
                        }
                    }
                surahMatches
                    .take(3)
                    .forEach {
                        suggestions.add(SearchSuggestion("سورة ${it.nameArabic}", SearchCategory.QURAN))
                    }
            }

            // Suggestions based on Audio
            val audioRes = audioRepository.getTracks(AudioFilter(query = trimmed), page = 1, pageSize = 5)
            if (audioRes is Resource.Success) {
                audioRes.data.take(2).forEach {
                    suggestions.add(SearchSuggestion(it.title, SearchCategory.AUDIO))
                }
            }

            // Suggestions based on Sources
            verifiedSourcesCatalog
                .filter { ArabicSearchUtils.matches(it.title, trimmed) }
                .take(2)
                .forEach {
                    suggestions.add(SearchSuggestion(it.title, SearchCategory.SOURCE))
                }

            // Suggestions based on Flashes
            verifiedFlashesCatalog
                .filter { ArabicSearchUtils.matches(it.title, trimmed) }
                .take(2)
                .forEach {
                    suggestions.add(SearchSuggestion(it.title, SearchCategory.FLASH))
                }

            suggestions.distinctBy { it.text }.take(limit)
        }

    override fun getSearchHistory(userId: String?): Flow<List<SearchHistoryItem>> =
        historyDao.observeHistory(userId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun recordSearchQuery(
        query: String,
        category: SearchCategory,
        resultCount: Int,
        userId: String?,
    ) = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext
        // Delete previous occurrences to push to the top
        historyDao.deleteByQuery(query.trim(), userId)
        val entity =
            SearchHistoryEntity(
                id = UUID.randomUUID().toString(),
                query = query.trim(),
                category = category.name,
                timestamp = System.currentTimeMillis(),
                resultCount = resultCount,
                userId = userId,
            )
        historyDao.insertHistory(entity)
    }

    override suspend fun deleteHistoryItem(id: String) =
        withContext(Dispatchers.IO) {
            historyDao.deleteById(id)
        }

    override suspend fun clearAllHistory(userId: String?) =
        withContext(Dispatchers.IO) {
            historyDao.clearAll(userId)
        }
}
