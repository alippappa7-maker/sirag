import re

# Remove UserProfile from Models.kt
with open('app/src/main/java/com/siraj/app/domain/models/Models.kt', 'r') as f:
    content = f.read()
content = re.sub(r'data class UserProfile\([^)]*\)\s*\{.*\}', '', content, flags=re.DOTALL)
content = re.sub(r'data class UserProfile\([^)]*\)', '', content, flags=re.DOTALL)
with open('app/src/main/java/com/siraj/app/domain/models/Models.kt', 'w') as f:
    f.write(content)

# Remove UserProfile from PreviewModels.kt
with open('app/src/main/java/com/siraj/app/domain/models/PreviewModels.kt', 'r') as f:
    content = f.read()
content = re.sub(r'data class UserProfile\([^)]*\)', '', content, flags=re.DOTALL)
with open('app/src/main/java/com/siraj/app/domain/models/PreviewModels.kt', 'w') as f:
    f.write(content)

