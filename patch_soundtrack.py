import re

file_path = "app/src/main/java/com/siraj/app/features/project/data/repositories/FirebaseSoundtrackRepositoryImpl.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Fix the duplicate/broken staticSoundtracks initialization.
# Since lines 140-175 have broken leftover SoundtrackItem instantiations floating outside of a list declaration, we need to wipe them.

pattern = r"\s*// Background Music[\s\S]*?SoundtrackItem\([\s\S]*?\n\s*\)\s*,\s*\n\s*SoundtrackItem\([\s\S]*?\n\s*\)\s*,\s*\n\s*\)"
content = re.sub(pattern, "", content, flags=re.MULTILINE)

# Also let's just wipe anything that looks like floating SoundtrackItem(...) that might be causing syntax errors between line 17 and line 177
pattern_floating = r"\s*SoundtrackItem\([\s\S]*?\n\s*\)\s*,"
content = re.sub(pattern_floating, "", content, flags=re.MULTILINE)

pattern_trailing = r"\s*SoundtrackItem\([\s\S]*?\n\s*\)\s*\)"
content = re.sub(pattern_trailing, "", content, flags=re.MULTILINE)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)
