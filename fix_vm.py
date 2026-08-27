import re

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'private fun loadProject()\n        loadTemplates() {',
    'private fun loadProject() {'
)

imports = "import com.siraj.app.domain.repository.TemplateRepository\nimport com.siraj.app.domain.models.ContentTemplate\nimport com.siraj.app.domain.repository.AuthRepository\n"
content = content.replace('import com.siraj.app.domain.repository.ProjectRepository', imports + 'import com.siraj.app.domain.repository.ProjectRepository')

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorViewModel.kt', 'w') as f:
    f.write(content)
