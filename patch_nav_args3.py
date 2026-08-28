import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

import re

# Some URLDecoder usage might still be referencing java.net.URLDecoder instead of just URLDecoder if we imported it
content = content.replace("java.net.URLDecoder.decode", "URLDecoder.decode")
content = content.replace("java.net.URLEncoder.encode", "java.net.URLEncoder.encode") # URLEncoder might not be imported

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
