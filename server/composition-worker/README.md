# محرك تركيب وتصيير الفيديو السحابي (Siraj Video Composition Engine)

## الهيكلية ونظرة عامة
محرك معالجة وتصيير الفيديو هو خدمة خلفية غير متزامنة مبنية على **Cloud Run** و **FFmpeg**، مصممة لمعالجة وتجميع جميع عناصر مشاريع سراج خارج جهاز العميل (Client App) لضمان الأداء، السرعة، وتوفير استهلاك البطارية والذاكرة.

### دورة حياة معالجة المهمة (Job Lifecycle)
1. **QUEUED (في الطابور):** استلام مهمة الإنتاج عبر Cloud Tasks والتحقق من عدم التكرار (`idempotencyKey`) وحجز الرصيد.
2. **PROCESSING (تجهيز الموارد):** تدقيق تراخيص الوسائط والصور المعتمدة وتحميل الـ assets.
3. **COMPOSING (تركيب المشاهد):** ترتيب المشاهد تصاعدياً حسب `orderIndex` وتطبيق المؤثرات الانتقالية (Dissolve, Fade, Slide) والدمج الصوتي (Cross-fading بين التعليق الصوتي، التلاوة، والمؤثرات SFX).
4. **ENCODING (ترميز الفيديو):** دمج الترجمة العربية RTL (Subtitle Burning) وتطبيق شارة التوثيق الشرعي للمحتوى المعتمد وتصيير الفيديو بجودة H.264/AAC.
5. **UPLOADING (رفع إلى التخزين):** رفع الناتج النهائي والصورة المصغرة المولدة تلقائياً إلى Cloud Storage وتوليد روابط موقعة مؤقتة (Signed URLs).
6. **COMPLETED (اكتمل بنجاح):** تسجيل مدة التنفيذ الفعلية، حجم الملف، وتحديث حالة المشروع والمهمة.
7. **FAILED / CANCELLED:** استرداد الرصيد المحجوز آلياً وتسجيل سبب الخطأ بأمان.

### المقاسات المدعومة:
- **9:16:** (1080x1920) ملائم لـ Shorts / Reels / TikTok.
- **1:1:** (1080x1080) ملائم للمنشورات المربعة.
- **16:9:** (1920x1080) ملائم لمنصة YouTube والشاشات العريضة.

### نشر الخدمة على Cloud Run:
```bash
gcloud run deploy siraj-video-composer \
  --image gcr.io/$PROJECT_ID/siraj-video-composer:latest \
  --platform managed \
  --region europe-west1 \
  --memory 4Gi \
  --cpu 2 \
  --timeout 900s \
  --set-env-vars OUTPUT_BUCKET=siraj-app-render-outputs
```
