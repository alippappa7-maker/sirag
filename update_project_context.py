import sys

with open("PROJECT_CONTEXT.md", "r") as f:
    content = f.read()

# Update Last Prompt
content = content.replace("034 (بناء قسم المحراب)", "035 (قارئ القرآن والتلاوة)\n034 (بناء قسم المحراب)")

with open("PROJECT_CONTEXT.md", "w") as f:
    f.write(content)
