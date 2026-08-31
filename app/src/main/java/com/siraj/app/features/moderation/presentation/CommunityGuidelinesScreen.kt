package com.siraj.app.features.moderation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.siraj.app.core.ui.components.SirajButton
import com.siraj.app.ui.theme.statusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityGuidelinesScreen(
    onNavigateBack: () -> Unit,
    onContactSafety: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قواعد المجتمع وإرشادات المحتوى") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Intro Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ميثاق مجتمع سراج",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يهدف سراج إلى توفير بيئة إسلامية إبداعية آمنة وموثوقة لإنتاج ومشاركة المحتوى المرئي والمسموع. يلتزم كل صانع محتوى ومستخدم بهذه القواعد لضمان سلامة المجتمع وصون قدسية المحتوى الشرعي وحقوق الآخرين.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section 1: Islamic & Religious Integrity
            GuidelineCard(
                icon = Icons.Default.MenuBook,
                title = "1. قدسية المحتوى الشرعي والتوثيق المعتمد",
                color = MaterialTheme.colorScheme.primary,
                items = listOf(
                    "يمنع منعاً باتاً تحريف أو تعديل النص القرآني الكريم بأي شكل.",
                    "يجب عزل القرآن عن الترجمة والتفسير، وفصل الأحاديث الشريفة عن الشروح.",
                    "عدم نسبة الأقوال أو الفتاوى لعلماء أو مؤسسات دون إسناد وتوثيق معتمد.",
                    "عدم استخدام الذكاء الاصطناعي كمفتٍ أو مصدر للتشريع أو لاستنباط أحكام دينية نهائية."
                )
            )

            // Section 2: Anti-Spam & Fraud
            GuidelineCard(
                icon = Icons.Default.Warning,
                title = "2. مكافحة المحتوى المزعج والاحتيال (Anti-Spam)",
                color = MaterialTheme.statusColors.warningFg,
                items = listOf(
                    "حظر الروابط الترويجية المشبوهة، الربح السريع، والاحتيال المالي.",
                    "منع إغراق المنصة بمقاطع مكررة أو مولدة آلياً دون قيمة مضافة.",
                    "منع انتحال الشخصيات، المشايخ، المؤسسات الرسمية أو صناع المحتوى الآخرين."
                )
            )

            // Section 3: Copyright & Intellectual Property
            GuidelineCard(
                icon = Icons.Default.Copyright,
                title = "3. حقوق الملكية الفكرية والتراخيص",
                color = Color(0xFF00695C),
                items = listOf(
                    "احترام حقوق المؤلفين، المصورين، والمنشدين والجهات الإعلامية.",
                    "يجب إرفاق بيانات الترخيص والمصدر لكل مادة مستخدمة خارج الملكية العامة.",
                    "يتم فوراً تقييد أو حذف أي مادة يتم الإبلاغ عنها بانتهاك حقوق الملكية الفكرية."
                )
            )

            // Section 4: Safety, Respect & Privacy
            GuidelineCard(
                icon = Icons.Default.Shield,
                title = "4. الأمان، الاحترام، وحماية الخصوصية",
                color = Color(0xFF1565C0),
                items = listOf(
                    "منع خطابات الكراهية، التكفير، التحريض على العنف أو الإساءة للأفراد والشعوب.",
                    "حظر نشر البيانات الشخصية للآخرين (Doxxing) أو تصويرهم دون إذنهم.",
                    "حماية خاصة للقاصرين وتوفير بيئة نظيفة خالية من الإساءات."
                )
            )

            // Section 5: Moderation Workflow & SLA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5. آلية الإشراف وزمن الاستجابة (SLA)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• فحص آلي استباقي: يخضع كل محتوى مرفوع للفحص الفوري للكشف عن الـ Spam والمحتوى الضار.\n• مراجعة بشرية متخصصة: يتم تحويل البلاغات الشرعية لمراجعين مؤهلين، وبلاغات الملكية للمسار القانوني.\n• الالتزام بالاستجابة: نلتزم بمراجعة البلاغات والبت فيها خلال 24 ساعة كحد أقصى.\n• سرية الإبلاغ: تبقى هوية المُبلّغ سرية تماماً ولا تظهر لصاحب المحتوى المبلّغ عنه.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Section 6: Escalation & Appeals
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "6. مصفوفة الجزاءات وحق الاستئناف",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تتدرج الإجراءات من تقييد الوصول، ثم تعليق المحتوى، وصولاً إلى إيقاف الحساب نهائياً في الانتهاكات الجسيمة.\n\nيحق لأي مستخدم تعرّض محتواه للتقييد أو الحذف تقديم طلب استئناف مع ذكر الأسباب عبر شاشة المحتوى، وتتم إعادة دراسته بموضوعية وسرعة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Contact & Support Footer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "للتواصل مع فريق سلامة المجتمع والإشراف",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "البريد المعتمد للبلاغات والأمان: safety@siraj.app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SirajButton(
                        text = "العودة للتطبيق",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidelineCard(
    icon: ImageVector,
    title: String,
    color: Color,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", color = color, fontWeight = FontWeight.Bold)
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
