import sys

with open("CHANGELOG.md", "r") as f:
    content = f.read()

new_content = "## [PROMPT 034] - 2026-08-27 (بناء قسم المحراب)\n\n" + content

with open("CHANGELOG.md", "w") as f:
    f.write(new_content)
