import re

with open("app/src/main/java/com/siraj/app/data/repository/FirebaseProjectRepositoryImpl.kt", "r") as f:
    content = f.read()

new_imports = """
import com.siraj.app.core.error.ErrorHandler
"""

content = content.replace("import com.siraj.app.core.utils.Resource", "import com.siraj.app.core.utils.Resource\n" + new_imports.strip())

def replace_catch(match):
    return "catch (e: Exception) {\n            val error = ErrorHandler.handle(e)\n            Resource.Error(error.userMessage, error)\n        }"

content = re.sub(r'catch \(e: Exception\) \{\s*Resource\.Error\([^\)]+\)\s*\}', replace_catch, content)

with open("app/src/main/java/com/siraj/app/data/repository/FirebaseProjectRepositoryImpl.kt", "w") as f:
    f.write(content)
