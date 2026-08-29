import re

with open("app/src/main/java/com/siraj/app/core/ui/components/StateScreens.kt", "r") as f:
    content = f.read()

new_error_screen = """
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "حدث خطأ", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            SirajButton(text = "إعادة المحاولة", onClick = onRetry)
        }
    }
}

@Composable
fun AppErrorScreen(error: AppError, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = error.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error.userMessage, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "رمز الخطأ: ${error.referenceId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            if (error.isRetryable && onRetry != null) {
                SirajButton(text = "إعادة المحاولة", onClick = onRetry)
            }
        }
    }
}
"""

content = re.sub(r"@Composable\s*fun ErrorScreen\(.*?\)\s*\{.*?\s*\}\s*\}", new_error_screen.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/siraj/app/core/ui/components/StateScreens.kt", "w") as f:
    f.write(content)
