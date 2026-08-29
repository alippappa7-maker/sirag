import os
import re

for root, _, files in os.walk("app/src/main/java/com/siraj/app/data/repository"):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r") as f:
                content = f.read()

            if "catch (e: Exception)" in content and "ErrorHandler" not in content:
                # add import
                content = content.replace("import com.siraj.app.core.utils.Resource", "import com.siraj.app.core.utils.Resource\nimport com.siraj.app.core.error.ErrorHandler")
                
                def replace_catch(match):
                    return "catch (e: Exception) {\n            val error = ErrorHandler.handle(e)\n            Resource.Error(error.userMessage, error)\n        }"
                
                content = re.sub(r'catch \(e: Exception\) \{\s*Resource\.Error\([^\)]+\)\s*\}', replace_catch, content)

                with open(path, "w") as f:
                    f.write(content)
