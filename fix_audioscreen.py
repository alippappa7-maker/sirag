import sys

with open("app/src/main/java/com/siraj/app/features/audio/presentation/AudioScreen.kt", "r") as f:
    content = f.read()

# Make sure there is NO `activeTrackId` at all.
if "activeTrackId" in content:
    print("Found activeTrackId in AudioScreen.kt. Removing...")
    
