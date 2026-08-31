package com.siraj.app.features.support.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPolicyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAppeal: () -> Unit,
    onNavigateToSourceCorrection: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سياسة المحتوى والاستخدام", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "مبادئ مجتمع سراج",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "نهدف في سراج إلى توفير بيئة إسلامية موثوقة وآمنة. القواعد التالية تضمن جودة المحتوى وحماية الجميع من التضليل أو الانتهاكات.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                PolicySection(
                    title = "المحتوى الشرعي الموثق",
                    icon = Icons.Default.AutoStories,
                    content = "يجب مطابقة النصوص القرآنية للرسم العثماني. تُرفض التلاوات غير المجازة. يُمنع نشر الأحاديث دون توثيق مصدرها، ولا يُعتد بالأحاديث المكذوبة.",
                )
                PolicySection(
                    title = "التكفير وخطاب الكراهية",
                    icon = Icons.Default.Gavel,
                    content = "يُمنع قطعياً إطلاق أحكام التكفير، أو التحريض على العنف، أو نشر خطاب الكراهية ضد أي عرق أو مجتمع. الانتهاك يؤدي للحظر الفوري.",
                )
                PolicySection(
                    title = "انتحال العلماء والتضليل",
                    icon = Icons.Default.PersonOff,
                    content = "يُمنع انتحال شخصيات العلماء والدعاة لترويج فتاوى أو آراء. كما يُمنع التلاعب بالنصوص الشرعية وتوظيفها خارج سياقها.",
                )
                PolicySection(
                    title = "الذكاء الاصطناعي والإفتاء",
                    icon = Icons.Default.SmartToy,
                    content = "لا يعتبر الذكاء الاصطناعي في سراج جهة إفتاء. استخدامه يقتصر على الصياغة، الترجمة، واستلهام الأفكار. يجب تمييز المحتوى المولد آلياً.",
                )
                PolicySection(
                    title = "الخلاف الفقهي",
                    icon = Icons.Default.Lightbulb,
                    content = "يُسمح بالخلاف الفقهي المعتبر طالما نُسب لأهله بموضوعية وعلم، بعيداً عن التعصب أو الطعن.",
                )
                PolicySection(
                    title = "حقوق النشر والخصوصية",
                    icon = Icons.Default.Copyright,
                    content = "يجب احترام حقوق الملكية الفكرية للتلاوات والمقاطع. يُمنع نشر محتوى يمس خصوصية الأفراد أو يستغل القاصرين بأي شكل.",
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "الإبلاغ والاعتراضات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onNavigateToAppeal,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("الاعتراض على قرار أو الإبلاغ عن مخالفة")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateToSourceCorrection,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("طلب تصحيح مصدر شرعي أو حديث")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PolicySection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
