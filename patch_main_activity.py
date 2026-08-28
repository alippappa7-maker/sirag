import sys

with open("app/src/main/java/com/siraj/app/MainActivity.kt", "r") as f:
    content = f.read()

import_statement = "import com.siraj.app.core.audio.AudioController\n"

if "AudioController" not in content:
    content = content.replace("import com.siraj.app.ui.theme.MyApplicationTheme\n", "import com.siraj.app.ui.theme.MyApplicationTheme\n" + import_statement)
    
    init_code = """
        // Initialize Audio Controller for background playback
        AudioController.initialize(this)
"""
    content = content.replace("override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)", "override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)" + init_code)
    
    destroy_code = """
    override fun onDestroy() {
        super.onDestroy()
        AudioController.release()
    }
"""
    content = content.replace("}\n", destroy_code + "}\n")
    
    with open("app/src/main/java/com/siraj/app/MainActivity.kt", "w") as f:
        f.write(content)
