import re

with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('    ignoreList.add("APP_STORE_")\n', '')
content = content.replace('    ignoreList.add("GOOGLE_PLAY_")\n', '')
content = content.replace('    ignoreList.add("FIREBASE_")\n', '')

with open('app/build.gradle.kts', 'w', encoding='utf-8') as f:
    f.write(content)
