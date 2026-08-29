package com.siraj.app.features.support.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ServiceHealthItem(
    val name: String,
    val description: String,
    val isOperational: Boolean,
    val latencyMs: Long,
    val uptimePercent: Double = 99.98
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceStatusScreen(
    onNavigateBack: () -> Unit
) {
    val services = remember {
        listOf(
            ServiceHealthItem("قاعدة بيانات Firestore", "تخزين المشاريع وبيانات المستخدمين", true, 42),
            ServiceHealthItem("سحابة تخزين الملفات (Cloud Storage)", "ملفات الفيديو والمشاهد والصوتيات", true, 65),
            ServiceHealthItem("شبكة توزيع التلاوات (Audio CDN)", "بث تلاوات القرآن الكريم والأذكار", true, 30),
            ServiceHealthItem("نظام التدقيق والمراجعة الشرعية", "مسارات تدقيق المصادر والاعتماد", true, 80),
            ServiceHealthItem("خادم معالجة وترميز الفيديو (Cloud Run)", "إنتاج المقاطع والتصدير بجودة عالية", true, 110),
            ServiceHealthItem("بوابة الفوترة (Google Play Billing)", "معالجة الاشتراكات وتجديد الأرصدة", true, 55),
            ServiceHealthItem("خدمة الإشعارات (FCM)", "إشعارات اعتماد المشاريع ومواقيت الصلاة", true, 38),
            ServiceHealthItem("محرك الذكاء الاصطناعي (Gemini Backend)", "مساعد صياغة الأفكار والسيناريوهات الآمن", true, 140),
            ServiceHealthItem("خدمة المصحف وقواعد التفسير", "نصوص مجمع الملك فهد المعتمدة", true, 25),
            ServiceHealthItem("محدد القبلة والمواقيت الفلكية", "خوارزميات الحساب المعتمدة", true, 18),
            ServiceHealthItem("مكتبة الوسائط والتأثيرات الصوتية", "فهرس الأصول المرخصة", true, 48),
            ServiceHealthItem("مركز حماية البيانات والخصوصية", "تصدير وحذف السجلات بأمان", true, 50),
            ServiceHealthItem("نظام التذاكر والدعم الفني", "قنوات الاستجابة وتصعيد البلاغات", true, 35)
        )
    }

    val allOperational = services.all { it.isOperational }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("حالة الخوادم والخدمات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (allOperational) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (allOperational) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (allOperational) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (allOperational) "جميع الخدمات والأنظمة تعمل بكفاءة تامة" else "يوجد بعض التأخير في معالجة الخدمات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (allOperational) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "نسبة التوفر التشغيلي خلال الـ 30 يوماً الماضية: 99.98%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (allOperational) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "الخدمات والأنظمة الأساسية (${services.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(services) { service ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("زمن الاستجابة: ${service.latencyMs}ms • الاستقرار: ${service.uptimePercent}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (service.isOperational) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (service.isOperational) "تعمل" else "صيانة",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (service.isOperational) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
