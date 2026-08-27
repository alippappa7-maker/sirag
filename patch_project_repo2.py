import re

with open('app/src/main/java/com/siraj/app/data/repository/FirebaseProjectRepositoryImpl.kt', 'r') as f:
    content = f.read()

content = content.replace('.whereEqualTo("ownerId", userId)', '.whereEqualTo("workspaceId", userId)')
# Wait, let's replace the variable name `userId` with `workspaceId` in interface and impl
with open('app/src/main/java/com/siraj/app/data/repository/FirebaseProjectRepositoryImpl.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/siraj/app/domain/repository/ProjectRepository.kt', 'r') as f:
    content_repo = f.read()

content_repo = content_repo.replace('userId: String', 'workspaceId: String')
with open('app/src/main/java/com/siraj/app/domain/repository/ProjectRepository.kt', 'w') as f:
    f.write(content_repo)
    
