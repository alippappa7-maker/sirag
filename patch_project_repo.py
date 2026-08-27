with open('app/src/main/java/com/siraj/app/data/repository/FirebaseProjectRepositoryImpl.kt', 'r') as f:
    content = f.read()

# Replace ACTIVE with DRAFT where applicable
content = content.replace("ProjectStatus.ACTIVE.name", "ProjectStatus.DRAFT.name")
content = content.replace("ProjectStatus.ACTIVE", "ProjectStatus.DRAFT")

# Fix version increment
content = content.replace("version = project.version + 1", "/* versioning handled via ProjectVersion entity */")
content = content.replace("version = 1,", "")
content = content.replace("sharedWith = emptyList()", "")

with open('app/src/main/java/com/siraj/app/data/repository/FirebaseProjectRepositoryImpl.kt', 'w') as f:
    f.write(content)
