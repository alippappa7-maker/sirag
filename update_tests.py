import sys

def main():
    # Update ProjectEditorViewModelTest
    with open("app/src/test/java/com/siraj/app/features/project/presentation/ProjectEditorViewModelTest.kt", "r") as f:
        content = f.read()
    
    content = content.replace("coEvery { projectRepository.getProject(any()) } returns flowOf(Resource.Success(Project(id = \"test_project\", title = \"Original Title\")))", "coEvery { projectRepository.getProject(any()) } returns Resource.Success(Project(id = \"test_project\", title = \"Original Title\"))")
    content = content.replace("coEvery { templateRepository.getAllTemplates() } returns flowOf(Resource.Success(emptyList()))", "coEvery { templateRepository.getAllTemplates() } returns Resource.Success(emptyList())")
    
    with open("app/src/test/java/com/siraj/app/features/project/presentation/ProjectEditorViewModelTest.kt", "w") as f:
        f.write(content)

    # Update AiImageGeneratorViewModelTest
    with open("app/src/test/java/com/siraj/app/features/project/presentation/ai/AiImageGeneratorViewModelTest.kt", "r") as f:
        content2 = f.read()
        
    content2 = content2.replace("coEvery { projectRepository.getProject(any()) } returns flowOf(Resource.Success(mockk(relaxed=true)))", "coEvery { projectRepository.getProject(any()) } returns Resource.Success(mockk(relaxed=true))")
    
    with open("app/src/test/java/com/siraj/app/features/project/presentation/ai/AiImageGeneratorViewModelTest.kt", "w") as f:
        f.write(content2)

if __name__ == "__main__":
    main()
