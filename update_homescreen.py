import re

with open('app/src/main/java/com/siraj/app/features/home/presentation/HomeScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the search banner Surface to include a subtle techno border
search_banner_old = """Surface(
                onClick = onNavigateToSearch,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_banner")
            )"""

search_banner_new = """Surface(
                onClick = onNavigateToSearch,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_banner")
            )"""

content = content.replace(search_banner_old, search_banner_new)

# Upgrade the Create Button
create_btn_old = """Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إنشاء فيديو")
                Spacer(Modifier.width(8.dp))
                Text("إنشاء فيديو جديد")
            }"""

create_btn_new = """Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إنشاء فيديو")
                Spacer(Modifier.width(8.dp))
                Text("إنشاء فيديو جديد", style = MaterialTheme.typography.labelLarge)
            }"""

content = content.replace(create_btn_old, create_btn_new)

# Upgrade ShortcutCard
shortcut_old = """@Composable
fun ShortcutCard(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}"""

shortcut_new = """@Composable
fun ShortcutCard(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}"""

content = content.replace(shortcut_old, shortcut_new)

# Upgrade ProjectCard
project_old = """@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = project.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "آخر تعديل: ${project.updatedAt}", style = MaterialTheme.typography.bodySmall)
        }
    }
}"""

project_new = """@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = project.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "آخر تعديل: ${project.updatedAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}"""

content = content.replace(project_old, project_new)


with open('app/src/main/java/com/siraj/app/features/home/presentation/HomeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
