import re

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.siraj.app.domain.models.Project", "import com.siraj.app.domain.models.Project\nimport com.siraj.app.domain.models.ProjectActivity\nimport com.siraj.app.domain.models.ActivityType")

new_save_logic = """                    val result = projectRepository.updateProject(projectToSave)
                    if (result is Resource.Success) {
                        _saveState.value = SaveState.Saved
                        projectRepository.logActivity(
                            ProjectActivity(
                                projectId = projectToSave.id,
                                userId = projectToSave.ownerId,
                                type = ActivityType.EDITED,
                                details = "تم تحديث المشروع تلقائياً"
                            )
                        )
                    } else if (result is Resource.Error) {"""

content = content.replace("""                    val result = projectRepository.updateProject(projectToSave)
                    if (result is Resource.Success) {
                        _saveState.value = SaveState.Saved
                    } else if (result is Resource.Error) {""", new_save_logic)

with open('app/src/main/java/com/siraj/app/features/project/presentation/ProjectEditorViewModel.kt', 'w') as f:
    f.write(content)
