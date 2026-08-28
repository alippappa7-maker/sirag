import sys

def main():
    try:
        with open("PROJECT_CONTEXT.md", "r") as f:
            content = f.read()

        content = content.replace("PROMPT 057 (تحليلات الاستخدام)", "PROMPT 058 (تحليلات المحتوى)")
        content = content.replace("قيد التنفيذ (مرحلة تحليلات الاستخدام اكتملت)", "قيد التنفيذ (مرحلة تحليلات المحتوى اكتملت)")
        
        insert_text = "- تصميم شاشة تحليلات المحتوى (Creator Analytics) توضح الأداء الفني للومضات دون المساس بالخصوصية.\n"
        if "المرحلة الحالية" in content:
            parts = content.split("المرحلة الحالية\nفي انتظار استلام المرحلة القادمة. تم الانتهاء مؤخراً من:\n")
            if len(parts) == 2:
                content = parts[0] + "المرحلة الحالية\nفي انتظار استلام المرحلة القادمة. تم الانتهاء مؤخراً من:\n" + insert_text + parts[1]

        with open("PROJECT_CONTEXT.md", "w") as f:
            f.write(content)
        print("Updated PROJECT_CONTEXT.md")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
