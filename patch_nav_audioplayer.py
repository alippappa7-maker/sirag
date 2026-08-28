import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

import_statement = "import com.siraj.app.features.audio.presentation.AudioPlayerScreen\n"

if "AudioPlayerScreen" not in content:
    content = content.replace("import com.siraj.app.features.audio.presentation.AudioScreen\n", "import com.siraj.app.features.audio.presentation.AudioScreen\n" + import_statement)
    
    player_route = """            composable(Screen.AudioPlayer.route) {
                AudioPlayerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
"""
    
    # insert before "composable(Screen.Ideation.route)"
    content = content.replace("            composable(Screen.Ideation.route) {", player_route + "            composable(Screen.Ideation.route) {")
    
    # Update MiniPlayer onExpand to navigate to AudioPlayer
    content = content.replace("onExpand = { navController.navigate(Screen.Audio.route) }", "onExpand = { navController.navigate(Screen.AudioPlayer.route) }")

    with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
        f.write(content)
