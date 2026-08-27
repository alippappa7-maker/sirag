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
