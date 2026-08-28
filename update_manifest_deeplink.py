import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

intent_filter = """            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="https" android:host="siraj.app" android:pathPrefix="/share" />
            </intent-filter>
"""

# Insert inside MainActivity
pattern = r'(<activity[^>]*android:name="\.MainActivity"[^>]*>.*?(?=</activity>))'
match = re.search(pattern, content, re.DOTALL)
if match and "android.intent.category.BROWSABLE" not in match.group(1):
    new_content = content[:match.end()] + intent_filter + content[match.end():]
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(new_content)
    print("Added deep link intent filter")
else:
    print("Already added or not found")
