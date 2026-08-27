import re

with open('app/src/main/java/com/siraj/app/domain/models/Project.kt', 'r') as f:
    content = f.read()

brief_class = """
data class ContentBrief(
    val idea: String = "",
    val contentType: String = "فيديو",
    val targetAudience: String = "عام",
    val language: String = "العربية الفصحى",
    val duration: String = "قصير (أقل من دقيقة)",
    val platform: String = "TikTok / Reels (9:16)",
    val visualStyle: String = "موشن جرافيك",
    val voiceType: String = "صوت رجالي رخيم",
    val template: String = "فارغ",
    val hasQuran: Boolean = false,
    val hasHadith: Boolean = false,
    val hasFatwa: Boolean = false
)
"""

if "ContentBrief" not in content:
    content += "\n" + brief_class

content = content.replace(
    'val currentVersionId: String? = null',
    'val currentVersionId: String? = null,\n    val brief: ContentBrief = ContentBrief()'
)

with open('app/src/main/java/com/siraj/app/domain/models/Project.kt', 'w') as f:
    f.write(content)
