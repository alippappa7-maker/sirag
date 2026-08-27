import re

with open('app/src/main/java/com/siraj/app/features/studio/presentation/StudioViewModel.kt', 'r') as f:
    content = f.read()

# Replace ACTIVE with DRAFT
content = content.replace('"ACTIVE"', '"DRAFT"')
content = content.replace("ProjectStatus.ACTIVE", "ProjectStatus.DRAFT")

with open('app/src/main/java/com/siraj/app/features/studio/presentation/StudioViewModel.kt', 'w') as f:
    f.write(content)
