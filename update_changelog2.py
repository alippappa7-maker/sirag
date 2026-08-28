import sys

def main():
    try:
        with open("CHANGELOG.md", "r") as f:
            content = f.read()

        new_entry = """
## [Unreleased]

### Added
- **تحليلات المحتوى (Creator Analytics)**: لوحة مخصصة لصناع المحتوى لعرض إحصائيات أداء الومضات والمشاريع بشكل تقديري لا ينتهك الخصوصية.
- **تصدير التقارير**: إمكانية تصدير تقرير أداء الومضات الخاص بصانع المحتوى بصيغة نصية قابلة للمشاركة.
"""
        content = content.replace("## [Unreleased]", new_entry)

        with open("CHANGELOG.md", "w") as f:
            f.write(content)
        print("Updated CHANGELOG.md")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
