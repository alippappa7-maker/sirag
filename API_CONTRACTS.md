# API Contracts

هذا الملف يحتوي على تعريفات البيانات (Contracts) للخدمات الخارجية أو الوهمية لضمان التوافق التام عند تبديل المزود (مثلاً إلى Gemini API).

## Idea Generator Service

### IdeaGenerationRequest
```kotlin
data class IdeaGenerationRequest(
    val subject: String,            // الموضوع الأساسي
    val audience: String,           // الجمهور (عام، شباب، أطفال، متخصصون)
    val platform: String,           // المنصة (TikTok, YouTube...)
    val duration: String,           // المدة (قصير، متوسط، طويل)
    val tone: String,               // النبرة (تحفيزي، أكاديمي، قصصي)
    val goal: String,               // الهدف (توعية، تفاعل، تعليم)
    val hasReligiousElement: Boolean// هل يحتوي على ادعاء شرعي أو نص ديني؟
)
```

### GeneratedIdea (Response Item)
```kotlin
data class GeneratedIdea(
    val id: String,                 // معرف فريد للفكرة
    val title: String,              // عنوان مقترح
    val hook: String,               // الخطاف (الجملة الجاذبة)
    val summary: String,            // ملخص الفكرة
    val audience: String,           // الجمهور المستهدف (تأكيد)
    val suggestedScenes: Int,       // عدد المشاهد المقترحة
    val requiredSources: List<String>,// المصادر المطلوبة إن وجدت
    val riskLevel: RiskLevel,       // مستوى المخاطرة (LOW, MEDIUM, HIGH)
    val needsReview: Boolean,       // هل تحتاج الفكرة لمراجعة بشرية صارمة؟
    val disclaimer: String?         // إخلاء مسؤولية أو تنبيه شرعي
)
```

## AI Image Generator Service (Imagen 3 / Vertex AI)

### AiImageGenerationRequest
```kotlin
data class AiImageGenerationRequest(
    val requestId: String,          // معرف الطلب لتتبع الحالة وتفادي التكرار
    val projectId: String,          // معرف المشروع
    val sceneId: String? = null,    // المشهد المرتبط اختياريًا
    val promptText: String,         // وصف المشهد البصري
    val negativePrompt: String?,    // نص الاستبعاد
    val style: AiImageStyle,        // الأسلوب الفني (إسلامي، فوتوغرافي، سينمائي، إلخ)
    val aspectRatio: AiImageAspectRatio, // أبعاد الصورة (16:9, 9:16, 1:1, 4:5)
    val count: Int = 1,             // عدد النتائج (1-4)
    val seed: Long? = null,         // البذرة لإعادة التوليد
    val model: String = "imagen-3.0-generate-002",
    val costUnits: Int = 2          // التكلفة بالرصيد
)
```

### GeneratedImageItem (Response Item)
```kotlin
data class GeneratedImageItem(
    val id: String,                 // معرف الصورة في النظام
    val requestId: String,          // معرف الطلب
    val projectId: String,
    val sceneId: String? = null,
    val imageUrl: String,           // رابط الصورة في Cloud Storage
    val thumbnailUrl: String,       // رابط المصغرة
    val promptText: String,
    val negativePrompt: String?,
    val style: AiImageStyle,
    val model: String,
    val provider: String,
    val width: Int,
    val height: Int,
    val seed: Long?,
    val status: AiImageStatus,      // QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED
    val costUnits: Int,
    val sourceType: String = "ai_generated",
    val isAiGenerated: Boolean = true,
    val licenseNotice: String = "مولد بالذكاء الاصطناعي - لا يعتبر دليلاً شرعياً",
    val generatedAt: Long
)
```

## 5. Audio Studio & Voiceover Contracts

### AudioSourceType
- `GENERATED_VOICE`: صوت مولد آلياً (AI) عبر محرك النطق الفصيح.
- `USER_RECORDING`: تسجيل صوتي مباشر من قبل المستخدم.
- `LICENSED_AUDIO`: مؤثر أو صوت خارجي مرخص.
- `QURAN_RECITATION`: تلاوة قرآنية موثقة ومعتمدة.

### AudioItem Contract
```kotlin
data class AudioItem(
    val id: String,
    val projectId: String,
    val sceneId: String?,
    val title: String,
    val textContent: String,
    val audioUrl: String,
    val storagePath: String,
    val sourceType: AudioSourceType,
    val voiceId: String,
    val voiceName: String,
    val languageCode: String,
    val speed: Float,
    val pitch: Float,
    val startTrimMs: Long,
    val endTrimMs: Long,
    val originalDurationMs: Long,
    val trimmedDurationMs: Long,
    val fileSize: Long,
    val mimeType: String,
    val isAiGenerated: Boolean,
    val licenseNotice: String,
    val reciterName: String?,
    val createdAt: Long
)
```

### Cloud Function: `generateVoiceover`
- **Trigger**: HTTPS Callable (OnCall)
- **Input**:
  - `requestId`: string (uuid)
  - `projectId`: string
  - `sceneId`: string?
  - `text`: string
  - `language`: string ("ar-SA", "ar-XA", "ar-EG", etc.)
  - `voiceId`: string
  - `speed`: float (0.7 - 1.4)
  - `pitch`: float (0.8 - 1.2)
- **Output**:
  - `status`: "completed"
  - `audio`: AudioItem document


