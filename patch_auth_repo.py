import re

with open("app/src/main/java/com/siraj/app/data/repository/FirebaseAuthRepositoryImpl.kt", "r") as f:
    content = f.read()

new_imports = """
import com.siraj.app.core.error.ErrorHandler
import com.siraj.app.core.error.AppError
"""

content = content.replace("import com.siraj.app.core.utils.Resource", "import com.siraj.app.core.utils.Resource\n" + new_imports.strip())

# We can replace all generic Resource.Error(e.localizedMessage ...) with ErrorHandler calls.
def replace_catch(match):
    return "catch (e: Exception) {\n            val error = ErrorHandler.handle(e)\n            Resource.Error(error.userMessage, error)\n        }"

# Just replace `catch (e: Exception) { Resource.Error(...) }`
content = re.sub(r'catch \(e: Exception\) \{\s*Resource\.Error\([^\)]+\)\s*\}', replace_catch, content)

# Also handle FirebaseAuthException
def replace_auth_catch(match):
    return "catch (e: FirebaseAuthException) {\n            val error = ErrorHandler.handle(e)\n            Resource.Error(error.userMessage, error)\n        }"
content = re.sub(r'catch \(e: FirebaseAuthException\) \{\s*Resource\.Error\([^\)]+\)\s*\}', replace_auth_catch, content)

with open("app/src/main/java/com/siraj/app/data/repository/FirebaseAuthRepositoryImpl.kt", "w") as f:
    f.write(content)
