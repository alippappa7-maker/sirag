import sys

with open("app/src/main/java/com/siraj/app/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

import_statement = "import com.siraj.app.core.audio.MiniPlayer\n"

if "import com.siraj.app.core.audio.MiniPlayer" not in content:
    content = content.replace("import com.siraj.app.features.audio.presentation.AudioScreen\n", "import com.siraj.app.features.audio.presentation.AudioScreen\n" + import_statement)
    
    # We want to place the MiniPlayer just above the bottom bar in the main Scaffold
    # The Scaffold is around line 125 (depending on previous edits).
    # Find:
    #         bottomBar = {
    #             if (isLoggedIn) {
    #                 NavigationBar {
    
    # Let's insert the MiniPlayer at the bottom of the main Box/Scaffold content, or within a Column if we have one.
    # Actually, in standard Compose, it's easiest to place it inside the Scaffold's content, aligned to the bottom.
    
    # Look for:
    #         }
    #     ) { innerPadding ->
    #         NavHost(
    
    # Replace with a Box that contains NavHost and MiniPlayer
    
    old_navhost = "        ) { innerPadding ->\n            NavHost("
    new_navhost = """        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NavHost(
                    modifier = Modifier.fillMaxSize(),"""
                    
    content = content.replace(old_navhost, new_navhost)
    content = content.replace("modifier = Modifier.padding(innerPadding),", "")
    
    # Find the end of NavHost and insert MiniPlayer
    # The end of NavHost is a bit tricky to find with simple replace.
    # We can just put MiniPlayer in a Box above NavHost, since NavHost is full size.
    # Wait, the Box replaces the content lambda. We need to close the Box.
    
    # Let's write a simple sed-like script for this.
