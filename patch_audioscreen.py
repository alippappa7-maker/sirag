import sys

with open("app/src/main/java/com/siraj/app/features/audio/presentation/AudioScreen.kt", "r") as f:
    content = f.read()

# Fix unresolved reference 'activeTrackId' on line 130
if "activeTrackId" in content:
    content = content.replace("isActive = state.activeTrackId == track.id,", "isActive = false,")
    
with open("app/src/main/java/com/siraj/app/features/audio/presentation/AudioScreen.kt", "w") as f:
    f.write(content)
