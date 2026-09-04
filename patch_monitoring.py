import re

file_path = "app/src/main/java/com/siraj/app/data/repository/monitoring/FirebaseMonitoringRepositoryImpl.kt"
with open(file_path, "r") as f:
    content = f.read()

imports_to_add = """
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
"""

content = content.replace("import kotlinx.coroutines.tasks.await\n", "import kotlinx.coroutines.tasks.await\n" + imports_to_add)

class_vars = """
    private val _servicesHealthFlow = MutableStateFlow(createInitialHealthChecks())
    private val _incidentsFlow = MutableStateFlow<List<ServiceIncident>>(emptyList())
    private val _alertsFlow = MutableStateFlow<List<MonitoringAlert>>(emptyList())
"""

content = content.replace("class FirebaseMonitoringRepositoryImpl(\n    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }\n) : MonitoringRepository {\n", "class FirebaseMonitoringRepositoryImpl(\n    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }\n) : MonitoringRepository {\n" + class_vars)


with open(file_path, "w") as f:
    f.write(content)
