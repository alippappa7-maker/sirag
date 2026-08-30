package com.siraj.app.features.ai.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سياسة الذكاء الاصطناعي") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "سياسة الذكاء الاصطناعي في سراج",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "يستخدم سراج الذكاء الاصطناعي كمساعد إنتاج فقط، وتُطبق المعايير التالية على جميع المحتويات المولدة:",
                style = MaterialTheme.typography.bodyLarge
            )
            
            PolicySection(
                title = "1. الشفافية والإفصاح",
                content = "يتم وسم أي محتوى (نص، صورة، صوت) يتم إنشاؤه بالكامل أو تعديله بشكل كبير بواسطة الذكاء الاصطناعي بعلامة (مُوَلَّد بالذكاء الاصطناعي)."
            )
            
            PolicySection(
                title = "2. القيود الشرعية",
                content = "يُمنع استخدام الذكاء الاصطناعي لاستنباط الأحكام الشرعية أو الإفتاء. لا يتم عرض أي نص مولد على أنه نص قرآني أو حديث شريف إلا بعد توثيقه بمصدر معتمد ومراجعته بشرياً. يُمنع تصوير الأنبياء والصحابة."
            )
            
            PolicySection(
                title = "3. منع التضليل والانتحال",
                content = "يُمنع استخدام الذكاء الاصطناعي لإنشاء محتوى مضلل، أو أخبار كاذبة، أو انتحال شخصيات حقيقية، أو استنساخ صوت أي شخص دون إذن صريح."
            )
            
            PolicySection(
                title = "4. المراجعة والإبلاغ",
                content = "يحق للمستخدمين الإبلاغ عن أي محتوى مولد يخالف هذه السياسة، وسيتم مراجعته وإزالته إذا ثبتت المخالفة. المحتوى ذو الخطورة العالية يخضع لمراجعة بشرية قبل النشر."
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
