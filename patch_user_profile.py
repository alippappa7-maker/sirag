import re

with open('app/src/main/java/com/siraj/app/domain/models/UserProfile.kt', 'r') as f:
    content = f.read()

# Add activeWorkspaceId
if 'val activeWorkspaceId: String? = null' not in content:
    content = content.replace(
        "val appLockEnabled: Boolean = false",
        "val appLockEnabled: Boolean = false,\n    val activeWorkspaceId: String? = null"
    )

with open('app/src/main/java/com/siraj/app/domain/models/UserProfile.kt', 'w') as f:
    f.write(content)
