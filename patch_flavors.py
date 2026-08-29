import re

with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    content = f.read()

flavors_block = """
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Siraj (Dev)")
            buildConfigField("String", "ENVIRONMENT", "\\"development\\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            resValue("string", "app_name", "Siraj (Staging)")
            buildConfigField("String", "ENVIRONMENT", "\\"staging\\"")
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "سراج")
            buildConfigField("String", "ENVIRONMENT", "\\"production\\"")
        }
    }
"""

if "flavorDimensions" not in content:
    content = content.replace('    buildTypes {', flavors_block + '\n    buildTypes {')

with open('app/build.gradle.kts', 'w', encoding='utf-8') as f:
    f.write(content)
