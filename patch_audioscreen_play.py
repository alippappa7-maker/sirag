import sys

with open("app/src/main/java/com/siraj/app/features/audio/presentation/AudioScreen.kt", "r") as f:
    content = f.read()

# The error was about playTrack accepting AudioTrack instead of String. Let's fix that too.
if "onPlay = { viewModel.playTrack(track.id) }" in content:
    content = content.replace("onPlay = { viewModel.playTrack(track.id) }", "onPlay = { viewModel.playTrack(track) }")
elif "onPlayClick = { viewModel.playTrack(it) }" in content:
    # Just in case we already patched something
    pass

with open("app/src/main/java/com/siraj/app/features/audio/presentation/AudioScreen.kt", "w") as f:
    f.write(content)

