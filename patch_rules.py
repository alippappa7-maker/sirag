with open('firestore.rules', 'r') as f:
    content = f.read()

replacement = """
    match /templates/{templateId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && isAdminOrOwner();
    }
    match /template_favorites/{favId} {
      allow read, delete: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
    }
"""

import re
content = re.sub(r'match /\{document=\*\*\} \{', replacement.strip() + "\n    match /{document=**} {", content)

with open('firestore.rules', 'w') as f:
    f.write(content)
