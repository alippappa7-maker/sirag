import sys

with open("app/src/main/java/com/siraj/app/features/audio/presentation/AudioScreen.kt", "r") as f:
    content = f.read()

# Remove the incorrect property
if "val activeTrackId" in content:
    lines = content.split('\n')
    new_lines = []
    for line in lines:
        if "val activeTrackId =" in line:
            new_lines.append(line.replace("val activeTrackId =", "// val activeTrackId ="))
        elif "onPlayClick = { viewModel.playTrack(it.id) }" in line:
            new_lines.append(line.replace("it.id", "it"))
        elif "activeTrackId = activeTrackId" in line:
            new_lines.append(line.replace("activeTrackId = activeTrackId", "activeTrackId = \"\""))
        else:
            new_lines.append(line)
    
    content = '\n'.join(new_lines)
    with open("app/src/main/java/com/siraj/app/features/audio/presentation/AudioScreen.kt", "w") as f:
        f.write(content)
