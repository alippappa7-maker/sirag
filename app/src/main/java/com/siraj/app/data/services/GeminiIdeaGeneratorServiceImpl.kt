package com.siraj.app.data.services

import com.siraj.app.BuildConfig
import com.siraj.app.core.utils.Resource
import com.siraj.app.data.api.GeminiApiService
import com.siraj.app.data.api.GeminiContent
import com.siraj.app.data.api.GeminiGenerationConfig
import com.siraj.app.data.api.GeminiPart
import com.siraj.app.data.api.GeminiRequest
import com.siraj.app.domain.models.*
import com.siraj.app.domain.services.IdeaGeneratorService
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID

/**
 * خدمة توليد الأفكار والسيناريوهات عبر Gemini API مباشرة.
 *
 * تتبع نفس نمط CopilotRepositoryImpl:
 * - Retrofit + Moshi لاستدعاء Gemini API
 * - برومبتات عربية مخصصة لتوليد أفكار إسلامية
 * - فلترة شرعية داخل البرومبت
 */
class GeminiIdeaGeneratorServiceImpl : IdeaGeneratorService {

    private val geminiApi: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    private val apiKey: String get() = BuildConfig.GEMINI_API_KEY

    // ═══════════════════════════════════════════════════════
    // توليد الأفكار
    // ═══════════════════════════════════════════════════════

    override suspend fun generateIdeas(request: IdeaGenerationRequest): Resource<List<GeneratedIdea>> {
        if (apiKey.isBlank()) {
            return Resource.Error("مفتاح Gemini API غير مُعدّ. يُرجى إضافته في BuildConfig.")
        }

        return try {
            val systemPrompt = buildIdeaSystemPrompt()
            val userPrompt = buildIdeaUserPrompt(request)

            val response = callGemini(systemPrompt, userPrompt, temperature = 0.8, maxTokens = 3000)
                ?: return Resource.Error("فشل الاتصال بـ Gemini. تأكد من اتصالك بالإنترنت.")

            val ideas = parseIdeasResponse(response)
            Resource.Success(ideas)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "حدث خطأ غير متوقع أثناء توليد الأفكار.")
        }
    }

    // ═══════════════════════════════════════════════════════
    // توليد السيناريو
    // ═══════════════════════════════════════════════════════

    override suspend fun generateScenario(
        idea: GeneratedIdea,
        request: IdeaGenerationRequest,
    ): Resource<ScenarioPlan> {
        if (apiKey.isBlank()) {
            return Resource.Error("مفتاح Gemini API غير مُعدّ.")
        }

        return try {
            val systemPrompt = buildScenarioSystemPrompt()
            val userPrompt = buildScenarioUserPrompt(idea, request)

            val response = callGemini(systemPrompt, userPrompt, temperature = 0.6, maxTokens = 4000)
                ?: return Resource.Error("فشل توليد السيناريو. تأكد من اتصالك بالإنترنت.")

            val plan = parseScenarioResponse(response, idea)
            Resource.Success(plan)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "حدث خطأ أثناء توليد السيناريو.")
        }
    }

    // ═══════════════════════════════════════════════════════
    // الإبلاغ
    // ═══════════════════════════════════════════════════════

    override suspend fun reportIdea(ideaId: String, reason: String): Resource<Unit> {
        // الإبلاغ يبقى عبر Firebase (لا يخص Gemini)
        return try {
            val firebaseService = FirebaseIdeaGeneratorServiceImpl()
            firebaseService.reportIdea(ideaId, reason)
        } catch (e: Exception) {
            Resource.Success(Unit) // لا نمنع المستخدم إذا فشل الإبلاغ
        }
    }

    // ═══════════════════════════════════════════════════════
    // برومبتات توليد الأفكار
    // ═══════════════════════════════════════════════════════

    private fun buildIdeaSystemPrompt(): String = """
أنت متخصص في إبداع المحتوى الإسلامي الرقمي. مهمتك توليد أفكار إسلامية إبداعية.

قواعد صارمة:
1. لا تلدف فتاوى أو أحكاماً شرعية محددة — وجّه لأهل العلم.
2. لا تستخدم صور أو أصوات م之乡unaشرة.
3. كل معلومة دينية يجب أن يكون لها مصدر موثّق (قرآن + رقم آية، حديث + مرجع).
4. أفكار يجب أن تكون مناسبة لجميع الأعمار.
5. كن إبداعياً مع الحفاظ على الأصالة الإسلامية.

الإجابة يجب أن تكون JSON فقط، بدون أي نص إضافي.

التنسيق المطلوب:
[
  {
    "id": "فريد",
    "title": "عنوان الفكرة",
    "hook": "خطاف البداية (أول 3 ثواني) — جملة تجذب المشاهد فوراً",
    "summary": "ملخص مختصر للفكرة (2-3 جمل)",
    "audience": "الجمهور المستهدف",
    "platform": "المنصة المناسبة",
    "tone": "النبرة",
    "estimatedDuration": "المدة المقترحة",
    "tags": ["وسم1", "وسم2"],
    "suggestedScenes": [
      {
        "order": 1,
        "title": "عنوان المشهد",
        "description": "وصف مختصر",
        "durationSeconds": 15,
        "type": "NARRATION"
      }
    ],
    "requiredSources": ["المصادر المطلوبة"],
    "riskLevel": "LOW",
    "needsReview": false,
    "disclaimer": null
  }
]

أنواع المشاهد: NARRATION, VISUAL, QURAN, TEXT, TRANSITION
مستويات المخاطرة: LOW, MEDIUM, HIGH
ولّد 3 إلى 5 أفكار مختلفة ومتنوعة.
    """.trimIndent()

    private fun buildIdeaUserPrompt(request: IdeaGenerationRequest): String = """
الموضوع: ${request.subject}
الجمهور: ${request.audience}
المنصة: ${request.platform}
المدة: ${request.duration}
النبرة: ${request.tone}
الهدف: ${request.goal}
${if (request.hasReligiousElement) "يتضمن عنصراً دينياً: نعم" else ""}

ولّد أفكاراً إسلامية إبداعية لهذا الموضوع بالتنسيق JSON المطلوب.
    """.trimIndent()

    // ═══════════════════════════════════════════════════════
    // برومبتات توليد السيناريو
    // ═══════════════════════════════════════════════════════

    private fun buildScenarioSystemPrompt(): String = """
أنت كاتب سيناريوهات محترف متخصص في المحتوى الإسلامي الرقمي. مهمتك تحويل فكرة إلى سيناريو إنتاج كامل.

قواعد صارمة:
1. كل آية قرآنية: اذكر السورة ورقم الآية.
2. كل حديث: اذكر المصدر (صحيح البخاري، صحيح مسلم، etc).
3. لا تفتِ — المحتوى تعليمي/تحفيزي فقط.
4. المشاهد يجب أن تكون واقعية القيل للإنتاج (تصوير فعلي أو موشن جرافيك).
5. كل مشهد: وصف بصري واضح يساعد المصور/المصمم على التنفيذ.

الإجابة JSON فقط:

{
  "title": "عنوان السيناريو",
  "overview": "نظرة عامة على السيناريو (3-4 جمل)",
  "openingHook": "الخطاف الذي يفتح به الفيديو",
  "closingCTA": "دعوة للفعل في النهاية (لا ت pozivaj على شيء مخالف)",
  "visualStyle": "النمط البصري المقترح",
  "musicMood": "مزاج الموسيقى التصويرية",
  "totalDurationSeconds": 60,
  "scenes": [
    {
      "id": "scene_001",
      "order": 1,
      "title": "عنوان المشهد",
      "narration": "نص السرد الكامل الذي يقرأه الراوي",
      "visualDescription": "وصف بصري تفصيلي: ماذا يظهر على الشاشة، الألوان، الحركة، النصوص",
      "durationSeconds": 15,
      "type": "NARRATION",
      "transitions": "كيف ينتقل للمشهد التالي",
      "overlayText": "نص يظهر على الشاشة (إن وجد)",
      "arabicText": "نص قرآني أو حديثي (إن وجد)",
      "arabicSource": "المصدر (سورة + آية / حديث + مرجع)"
    }
  ]
}

أنواع المشاهد: NARRATION, VISUAL, QURAN, TEXT, TRANSITION
أنشئ من 5 إلى 10 مشاهد حسب طول المحتوى.
المدة الإجمالية يجب أن تناسب المنصة المحددة.
    """.trimIndent()

    private fun buildScenarioUserPrompt(idea: GeneratedIdea, request: IdeaGenerationRequest): String = """
الفكرة المختارة:
- العنوان: ${idea.title}
- الخطاف: ${idea.hook}
- الملخص: ${idea.summary}
- الجمهور: ${idea.audience}
- المنصة: ${idea.platform.ifBlank { request.platform }}
- النبرة: ${idea.tone.ifBlank { request.tone }}
- المدة: ${idea.estimatedDuration.ifBlank { request.duration }}

حوّل هذه الفكرة إلى سيناريو إنتاج كامل بالتنسيق JSON المطلوب.
    """.trimIndent()

    // ═══════════════════════════════════════════════════════
    // استدعاء Gemini API
    // ═══════════════════════════════════════════════════════

    private suspend fun callGemini(
        systemPrompt: String,
        userPrompt: String,
        temperature: Double = 0.7,
        maxTokens: Int = 3000,
    ): String? {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = userPrompt))),
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(
                temperature = temperature,
                topP = 0.9,
                topK = 40,
                maxOutputTokens = maxTokens,
            ),
        )

        val response = geminiApi.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }

    // ═══════════════════════════════════════════════════════
    // تحليل الردود
    // ═══════════════════════════════════════════════════════

    private fun parseIdeasResponse(raw: String): List<GeneratedIdea> {
        val json = extractJsonArray(raw)
        val ideas = mutableListOf<GeneratedIdea>()

        for (i in 0 until json.length()) {
            val obj = json.optJSONObject(i) ?: continue
            ideas.add(
                GeneratedIdea(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    title = obj.optString("title", ""),
                    hook = obj.optString("hook", ""),
                    summary = obj.optString("summary", ""),
                    audience = obj.optString("audience", ""),
                    platform = obj.optString("platform", ""),
                    tone = obj.optString("tone", ""),
                    estimatedDuration = obj.optString("estimatedDuration", ""),
                    tags = obj.optJSONArray("tags")?.let { arr ->
                        (0 until arr.length()).mapNotNull { arr.optString(it) }
                    } ?: emptyList(),
                    suggestedScenes = obj.optJSONArray("suggestedScenes")?.let { arr ->
                        (0 until arr.length()).mapNotNull { sIdx ->
                            val s = arr.optJSONObject(sIdx) ?: return@mapNotNull null
                            SuggestedScene(
                                order = s.optInt("order", sIdx + 1),
                                title = s.optString("title", ""),
                                description = s.optString("description", ""),
                                durationSeconds = s.optInt("durationSeconds", 10),
                                type = parseSceneType(s.optString("type", "NARRATION")),
                            )
                        }
                    } ?: emptyList(),
                    requiredSources = obj.optJSONArray("requiredSources")?.let { arr ->
                        (0 until arr.length()).mapNotNull { arr.optString(it) }
                    } ?: emptyList(),
                    riskLevel = parseRiskLevel(obj.optString("riskLevel", "LOW")),
                    needsReview = obj.optBoolean("needsReview", false),
                    disclaimer = obj.optString("disclaimer", "").ifBlank { null },
                ),
            )
        }
        return ideas
    }

    private fun parseScenarioResponse(raw: String, idea: GeneratedIdea): ScenarioPlan {
        val obj = extractJsonObject(raw)

        val scenes = obj.optJSONArray("scenes")?.let { arr ->
            (0 until arr.length()).mapNotNull { idx ->
                val s = arr.optJSONObject(idx) ?: return@mapNotNull null
                ScenarioScene(
                    id = s.optString("id", "scene_${idx + 1}"),
                    order = s.optInt("order", idx + 1),
                    title = s.optString("title", ""),
                    narration = s.optString("narration", ""),
                    visualDescription = s.optString("visualDescription", ""),
                    durationSeconds = s.optInt("durationSeconds", 10),
                    type = parseSceneType(s.optString("type", "NARRATION")),
                    transitions = s.optString("transitions", ""),
                    overlayText = s.optString("overlayText", "").ifBlank { null },
                    arabicText = s.optString("arabicText", "").ifBlank { null },
                    arabicSource = s.optString("arabicSource", "").ifBlank { null },
                )
            }
        } ?: emptyList()

        return ScenarioPlan(
            ideaId = idea.id,
            title = obj.optString("title", idea.title),
            overview = obj.optString("overview", idea.summary),
            openingHook = obj.optString("openingHook", idea.hook),
            closingCTA = obj.optString("closingCTA", ""),
            visualStyle = obj.optString("visualStyle", ""),
            musicMood = obj.optString("musicMood", ""),
            totalDurationSeconds = obj.optInt("totalDurationSeconds", scenes.sumOf { it.durationSeconds }),
            scenes = scenes,
        )
    }

    // ═══════════════════════════════════════════════════════
    // مساعدات تحليل JSON
    // ═══════════════════════════════════════════════════════

    private fun extractJsonArray(raw: String): JSONArray {
        // أحياناً Gemini يلف JSON بـ ```json ... ```
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        // محاولة إيجاد مصفوفة
        val startIdx = cleaned.indexOf('[')
        val endIdx = cleaned.lastIndexOf(']')
        if (startIdx >= 0 && endIdx > startIdx) {
            return JSONArray(cleaned.substring(startIdx, endIdx + 1))
        }
        // إذا كان JSON واحد، حوّله لمصفوفة
        return JSONArray().put(extractJsonObject(cleaned))
    }

    private fun extractJsonObject(raw: String): JSONObject {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        val startIdx = cleaned.indexOf('{')
        val endIdx = cleaned.lastIndexOf('}')
        if (startIdx >= 0 && endIdx > startIdx) {
            return JSONObject(cleaned.substring(startIdx, endIdx + 1))
        }
        return JSONObject()
    }

    private fun parseSceneType(type: String): SceneType = when (type.uppercase()) {
        "NARRATION" -> SceneType.NARRATION
        "VISUAL" -> SceneType.VISUAL
        "QURAN" -> SceneType.QURAN
        "TEXT" -> SceneType.TEXT
        "TRANSITION" -> SceneType.TRANSITION
        else -> SceneType.NARRATION
    }

    private fun parseRiskLevel(level: String): RiskLevel = when (level.uppercase()) {
        "LOW" -> RiskLevel.LOW
        "MEDIUM" -> RiskLevel.MEDIUM
        "HIGH" -> RiskLevel.HIGH
        else -> RiskLevel.LOW
    }
}
