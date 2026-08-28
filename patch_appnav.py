import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

import_statement = "import com.siraj.app.core.audio.MiniPlayer\n"
if "import com.siraj.app.core.audio.MiniPlayer" not in content:
    content = content.replace("import com.siraj.app.features.audio.presentation.AudioScreen\n", "import com.siraj.app.features.audio.presentation.AudioScreen\n" + import_statement)

# Now, we need to wrap the `content(Modifier.padding(paddingValues))` inside a Column or Box so we can put the MiniPlayer at the bottom of the content area.

# Let's find:
#    if (isMainScreen) {
#        MainShellScreen(navController = navController) { paddingValues ->
#            content(Modifier.padding(paddingValues))
#        }
#    } else {
#        Box(modifier = Modifier) {
#            content(Modifier)
#        }
#    }

old_main = """    if (isMainScreen) {
        MainShellScreen(navController = navController) { paddingValues ->
            content(Modifier.padding(paddingValues))
        }
    } else {
        Box(modifier = Modifier) {
            content(Modifier)
        }
    }"""

new_main = """    if (isMainScreen) {
        MainShellScreen(navController = navController) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                content(Modifier)
                MiniPlayer(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                    onExpand = { navController.navigate(Screen.Audio.route) }
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            content(Modifier)
            MiniPlayer(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                onExpand = { navController.navigate(Screen.Audio.route) }
            )
        }
    }"""

content = content.replace(old_main, new_main)

# Also check for modifier argument mismatch in `content(Modifier)` vs `content(Modifier.padding(...))`
# `content` is defined as `val content: @Composable (Modifier) -> Unit = { innerModifier -> ... Box(modifier = innerModifier) { NavHost(...) } }`
# Yes, `innerModifier` is applied to the NavHost wrapper.
# So passing `Modifier` is fine since we apply padding on the Box wrapping `content` now.

# We also need to add import for Alignment
import_alignment = "import androidx.compose.ui.Alignment\n"
if "import androidx.compose.ui.Alignment" not in content:
    content = content.replace("import androidx.compose.ui.Modifier\n", "import androidx.compose.ui.Modifier\n" + import_alignment)

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
