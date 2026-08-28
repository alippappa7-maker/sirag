import sys

def main():
    try:
        with open("gradle/libs.versions.toml", "r") as f:
            content = f.read()

        if "mockk = " not in content:
            content = content.replace("[versions]\n", "[versions]\nmockk = \"1.13.10\"\n")
            content = content.replace("[libraries]\n", "[libraries]\nmockk = { group = \"io.mockk\", name = \"mockk\", version.ref = \"mockk\" }\nmockk-android = { group = \"io.mockk\", name = \"mockk-android\", version.ref = \"mockk\" }\n")
        
        with open("gradle/libs.versions.toml", "w") as f:
            f.write(content)
            
        with open("app/build.gradle.kts", "r") as f:
            app_content = f.read()
            
        if "libs.mockk" not in app_content:
            app_content = app_content.replace("testImplementation(libs.junit)", "testImplementation(libs.junit)\n  testImplementation(libs.mockk)")
            
        with open("app/build.gradle.kts", "w") as f:
            f.write(app_content)
        print("Added mockk")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
