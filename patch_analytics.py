import re

with open("app/src/main/java/com/siraj/app/features/admin/presentation/AnalyticsDashboardScreen.kt", "r") as f:
    content = f.read()

imports = """
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
"""

content = re.sub(
    r"import androidx.compose.foundation.layout.\*",
    f"import androidx.compose.foundation.layout.*\n{imports}",
    content,
    count=1
)

chart_ui = """
            item {
                if (eventCounts.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("رسم بياني للأحداث", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                            
                            val chartEntryModel = entryModelOf(*eventCounts.values.map { it.toFloat() }.toTypedArray())
                            
                            Chart(
                                chart = columnChart(),
                                model = chartEntryModel,
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(
                                    valueFormatter = { value, _ -> 
                                        val keys = eventCounts.keys.toList()
                                        val index = value.toInt()
                                        if (index >= 0 && index < keys.size) keys[index].take(5) else ""
                                    }
                                ),
                                modifier = Modifier.height(200.dp).fillMaxWidth()
                            )
                        }
                    }
                }
            }
"""

content = re.sub(
    r"items\(eventCounts\.entries\.toList\(\)\) \{ entry ->",
    f"{chart_ui}\n            items(eventCounts.entries.toList()) {{ entry ->",
    content,
    count=1
)

with open("app/src/main/java/com/siraj/app/features/admin/presentation/AnalyticsDashboardScreen.kt", "w") as f:
    f.write(content)

