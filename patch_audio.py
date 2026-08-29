import re

with open("app/src/main/java/com/siraj/app/core/audio/SirajAudioService.kt", "r") as f:
    content = f.read()

content = content.replace("class SirajAudioService", "@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)\nclass SirajAudioService")

imports = """
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
"""
content = content.replace("import androidx.media3.exoplayer.ExoPlayer", imports.strip())

builder_replacement = """
        // Optimize LoadControl for slow networks and lower memory usage
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                1000, // Reduced buffer for playback to start sooner
                2000  // Reduced buffer after rebuffer
            )
            .build()

        val renderersFactory = DefaultRenderersFactory(this)
            .setEnableDecoderFallback(true) // Helps on low-end devices
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        exoPlayer = ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
"""
content = content.replace("exoPlayer = ExoPlayer.Builder(this)", builder_replacement.strip())

with open("app/src/main/java/com/siraj/app/core/audio/SirajAudioService.kt", "w") as f:
    f.write(content)
