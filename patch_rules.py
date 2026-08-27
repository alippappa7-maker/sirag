with open('firestore.rules', 'r') as f:
    content = f.read()

new_rules = """    match /projects/{projectId} {
      function isOwner() {
        return request.auth.uid == resource.data.ownerId;
      }
      function isSharedWith() {
        return request.auth.uid in resource.data.sharedWith;
      }
      function willBeOwner() {
        return request.auth.uid == request.resource.data.ownerId;
      }
      
      allow read: if request.auth != null && (isOwner() || isSharedWith() || isAdminOrOwner());
      allow create: if request.auth != null && willBeOwner();
      allow update: if request.auth != null && (isOwner() || isSharedWith() || isAdminOrOwner());
      allow delete: if request.auth != null && (isOwner() || isAdminOrOwner());
    }
    
    match /project_activities/{activityId} {
      allow read, create: if request.auth != null;
    }
    
    match /project_versions/{versionId} {
      allow read, create: if request.auth != null;
    }"""

content = content.replace("    match /projects/{projectId} {\n      function isOwner() {\n        return request.auth.uid == resource.data.ownerId;\n      }\n      function isSharedWith() {\n        return request.auth.uid in resource.data.sharedWith;\n      }\n      function willBeOwner() {\n        return request.auth.uid == request.resource.data.ownerId;\n      }\n      \n      allow read: if request.auth != null && (isOwner() || isSharedWith() || isAdminOrOwner());\n      allow create: if request.auth != null && willBeOwner();\n      allow update: if request.auth != null && (isOwner() || isSharedWith() || isAdminOrOwner());\n      allow delete: if request.auth != null && (isOwner() || isAdminOrOwner());\n    }", new_rules)

with open('firestore.rules', 'w') as f:
    f.write(content)
