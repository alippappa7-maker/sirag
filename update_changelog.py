import sys
import datetime

if len(sys.argv) < 3:
    print("Usage: python3 update_changelog.py '[PROMPT XXX] - Name' 'Description'")
    sys.exit(1)

prompt_title = sys.argv[1]
description = sys.argv[2]
date_str = datetime.datetime.now().strftime("%Y-%m-%d")

new_entry = f"## {prompt_title} - {date_str}\n### أُضيف (Added)\n- {description}\n\n"

with open("CHANGELOG.md", "r") as f:
    old_content = f.read()

with open("CHANGELOG.md", "w") as f:
    f.write(new_entry + old_content)
