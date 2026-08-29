import re

with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    content = f.read()

fields = """        buildConfigField("String", "FIREBASE_API_KEY", "\\\"dummy\\\"")
        buildConfigField("String", "GOOGLE_PLAY_PACKAGE_NAME", "\\\"dummy\\\"")
        buildConfigField("String", "GOOGLE_PLAY_PUBSUB_TOPIC_NAME", "\\\"dummy\\\"")
        buildConfigField("String", "GOOGLE_PLAY_SERVICE_ACCOUNT_JSON", "\\\"dummy\\\"")
        buildConfigField("String", "APP_STORE_BUNDLE_ID", "\\\"dummy\\\"")
        buildConfigField("String", "APP_STORE_ENVIRONMENT", "\\\"dummy\\\"")
        buildConfigField("String", "APP_STORE_ISSUER_ID", "\\\"dummy\\\"")
        buildConfigField("String", "APP_STORE_KEY_ID", "\\\"dummy\\\"")
        buildConfigField("String", "APP_STORE_PRIVATE_KEY", "\\\"dummy\\\"")"""

content = content.replace('        vectorDrawables {', fields + '\n        vectorDrawables {')

with open('app/build.gradle.kts', 'w', encoding='utf-8') as f:
    f.write(content)
