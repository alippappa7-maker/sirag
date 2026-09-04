import re

file_path = "app/src/main/java/com/siraj/app/features/project/data/repositories/FirebaseAudioRepositoryImpl.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

pattern = r"override fun getAvailableVoices\(\): List<VoiceOption> = emptyList\(\)[\s\S]*?\n        \)"
content = re.sub(pattern, "override fun getAvailableVoices(): List<VoiceOption> = emptyList()", content, flags=re.MULTILINE)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)
