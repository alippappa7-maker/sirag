import re

with open('app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("ProjectStatus.ACTIVE", "ProjectStatus.DRAFT")
# In case it uses other statuses to hide options
content = content.replace("project.status == ProjectStatus.DRAFT", "project.status != ProjectStatus.ARCHIVED && project.status != ProjectStatus.DELETED")

with open('app/src/main/java/com/siraj/app/features/studio/presentation/StudioScreen.kt', 'w') as f:
    f.write(content)
