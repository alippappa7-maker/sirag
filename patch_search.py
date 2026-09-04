import re

file_path = "app/src/main/java/com/siraj/app/data/repository/search/UnifiedSearchRepositoryImpl.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Fix verifiedSourcesCatalog
pattern_sources = r"private val verifiedSourcesCatalog = emptyList<SearchResultItem>\(\)[\s\S]*?\n        \)"
content = re.sub(pattern_sources, "private val verifiedSourcesCatalog = emptyList<SearchResultItem>()", content, flags=re.MULTILINE)

# Fix verifiedFlashesCatalog
pattern_flashes = r"private val verifiedFlashesCatalog =\s*listOf\([\s\S]*?\n        \)"
content = re.sub(pattern_flashes, "private val verifiedFlashesCatalog = emptyList<SearchResultItem>()", content, flags=re.MULTILINE)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)
