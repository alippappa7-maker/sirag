import re

with open("app/src/main/java/com/siraj/app/features/admin/presentation/AnalyticsDashboardScreen.kt", "r") as f:
    content = f.read()

# Make sure Vico is properly imported, remove it if not needed or add it if missing
# Wait, compilation was successful! BUILD SUCCESSFUL.

