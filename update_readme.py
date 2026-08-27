import sys
with open("README.md", "r") as f:
    content = f.read()

new_content = content.replace("- مشغل الفيديو الموحد (SirajVideoPlayer).", "- مشغل الفيديو الموحد (SirajVideoPlayer).\n- قسم المحراب: شامل للقرآن، التفاسير، الأذكار، ومواقيت الصلاة.")

with open("README.md", "w") as f:
    f.write(new_content)
