import re

with open('app/src/main/java/com/siraj/app/data/repository/FirebaseAuthRepositoryImpl.kt', 'r') as f:
    content = f.read()

# Add activeWorkspaceId parsing
if 'activeWorkspaceId = prefMap["activeWorkspaceId"] as? String' not in content:
    content = content.replace(
        'appLockEnabled = prefMap["appLockEnabled"] as? Boolean ?: false',
        'appLockEnabled = prefMap["appLockEnabled"] as? Boolean ?: false,\n                                    activeWorkspaceId = prefMap["activeWorkspaceId"] as? String'
    )
    
    content = content.replace(
        '"appLockEnabled" to preferences.appLockEnabled',
        '"appLockEnabled" to preferences.appLockEnabled,\n                "activeWorkspaceId" to preferences.activeWorkspaceId'
    )

with open('app/src/main/java/com/siraj/app/data/repository/FirebaseAuthRepositoryImpl.kt', 'w') as f:
    f.write(content)
