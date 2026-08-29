import re

with open("app/src/main/java/com/siraj/app/features/auth/presentation/AuthViewModel.kt", "r") as f:
    content = f.read()

new_imports = """
import com.siraj.app.core.error.ErrorHandler
"""

content = content.replace("import com.siraj.app.core.utils.Resource", "import com.siraj.app.core.utils.Resource\n" + new_imports.strip())

# Find occurrences of Resource.Error in the catch blocks or anywhere and replace if we can, but since the exception isn't always available in these viewmodels, let's look at FirebaseAuthRepositoryImpl instead.
