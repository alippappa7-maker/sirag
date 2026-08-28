import re

with open('app/src/main/java/com/siraj/app/data/repository/subscription/FirebaseSubscriptionRepositoryImpl.kt', 'r', encoding='utf-8') as f:
    content = f.read()

new_plans = """            Plan(
                id = "plan_free",
                name = "مجاني",
                description = "ميزات سراج الأساسية، القرآن دائماً مجاني",
                interval = BillingInterval.MONTHLY,
                price = 0.0,
                currency = "USD",
                features = listOf("تلاوة القرآن وقراءته", "مواقيت الصلاة والأذكار", "توليد صور بالذكاء الاصطناعي (محدود)"),
                limits = listOf(
                    UsageLimit("AI_IMAGE_GENERATION", 5, 0, null),
                    UsageLimit("AUDIO_GENERATION", 2, 0, null)
                ),
                active = true,
                platformProductIds = emptyMap()
            ),
            Plan(
                id = "plan_pro_monthly",
                name = "سراج برو (شهري)",
                description = "لمنشئي المحتوى المتقدمين",
                interval = BillingInterval.MONTHLY,
                price = 9.99,
                currency = "USD",
                features = listOf("كل ميزات المجاني", "تصدير متقدم بدون علامة مائية", "دعم فني أسرع", "توليد مشاهد متقدمة"),
                limits = listOf(
                    UsageLimit("AI_IMAGE_GENERATION", 100, 0, null),
                    UsageLimit("AUDIO_GENERATION", 50, 0, null)
                ),
                active = true,
                platformProductIds = mapOf("android" to "siraj_pro_monthly", "ios" to "siraj_pro_monthly")
            ),
            Plan(
                id = "plan_pro_yearly",
                name = "سراج برو (سنوي)",
                description = "توفير أكبر لمنشئي المحتوى",
                interval = BillingInterval.YEARLY,
                price = 99.99,
                currency = "USD",
                features = listOf("كل ميزات المجاني", "تصدير متقدم بدون علامة مائية", "دعم فني أسرع", "توليد مشاهد متقدمة"),
                limits = listOf(
                    UsageLimit("AI_IMAGE_GENERATION", 1200, 0, null),
                    UsageLimit("AUDIO_GENERATION", 600, 0, null)
                ),
                active = true,
                platformProductIds = mapOf("android" to "siraj_pro_yearly", "ios" to "siraj_pro_yearly")
            ),
            Plan(
                id = "plan_enterprise",
                name = "المؤسسات (Workspace)",
                description = "للفرق والمؤسسات الإعلامية والدعوية",
                interval = BillingInterval.MONTHLY,
                price = 49.99,
                currency = "USD",
                features = listOf("كل ميزات برو", "إدارة الفريق والأعضاء", "مساحة تخزين مشتركة", "دعم فني مخصص 24/7", "حدود مفتوحة للذكاء الاصطناعي"),
                limits = emptyList(),
                active = true,
                platformProductIds = mapOf("android" to "siraj_enterprise_monthly", "ios" to "siraj_enterprise_monthly")
            )"""

pattern = r'Plan\(\s*id = "plan_free".*?platformProductIds = mapOf\("android" to "siraj_pro_monthly", "ios" to "siraj_pro_monthly"\)\s*\)'
content = re.sub(pattern, new_plans, content, flags=re.DOTALL)

with open('app/src/main/java/com/siraj/app/data/repository/subscription/FirebaseSubscriptionRepositoryImpl.kt', 'w', encoding='utf-8') as f:
    f.write(content)
