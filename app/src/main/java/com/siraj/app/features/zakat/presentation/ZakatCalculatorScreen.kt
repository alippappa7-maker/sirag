package com.siraj.app.features.zakat.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siraj.app.domain.models.zakat.ZakatResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ZakatViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("حاسبة الزكاة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab,
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("زكاة المال") },
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("زكاة الفطر") },
                )
            }

            when (state.selectedTab) {
                0 -> ZakatMalContent(state = state, viewModel = viewModel)
                1 -> ZakatFitrContent(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ZakatMalContent(
    state: ZakatUiState,
    viewModel: ZakatViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ZakatInputField(
            label = "سعر جرام الذهب (بالعملة المحلية)",
            value = state.goldPrice,
            onValueChange = viewModel::updateGoldPrice,
            placeholder = "مثال: 250",
        )
        ZakatInputField(
            label = "المبالغ النقدية والمدخرات",
            value = state.cashAmount,
            onValueChange = viewModel::updateCashAmount,
            placeholder = "0.00",
        )
        ZakatInputField(
            label = "قيمة عروض التجارة",
            value = state.tradeGoodsValue,
            onValueChange = viewModel::updateTradeGoods,
            placeholder = "0.00",
        )
        ZakatInputField(
            label = "الديون المستحقة عليك (تُخصم)",
            value = state.debtsOwed,
            onValueChange = viewModel::updateDebts,
            placeholder = "0.00",
        )

        Button(
            onClick = { viewModel.calculateZakat() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("احسب الزكاة", modifier = Modifier.padding(vertical = 4.dp))
        }

        state.zakatResult?.let { result ->
            ZakatResultCard(result = result)
        }
    }
}

@Composable
private fun ZakatFitrContent(
    state: ZakatUiState,
    viewModel: ZakatViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ZakatInputField(
            label = "عدد أفراد الأسرة",
            value = state.fitrHouseholdMembers,
            onValueChange = viewModel::updateFitrMembers,
            placeholder = "1",
        )
        ZakatInputField(
            label = "مبلغ الفطرة لكل فرد",
            value = state.fitrPerPerson,
            onValueChange = viewModel::updateFitrPerPerson,
            placeholder = "20",
        )
        Button(
            onClick = { viewModel.calculateFitr() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("احسب زكاة الفطر", modifier = Modifier.padding(vertical = 4.dp))
        }
        state.fitrResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("إجمالي زكاة الفطر", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = String.format("%.2f %s", result, state.fitrCurrency),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "زكاة الفطر: صاع من الطعام لكل فرد من أفراد الأسرة، تخرج قبل صلاة العيد. وتُقوم بالعملة المحلية لتسهيل الإخراج.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ZakatInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun ZakatResultCard(result: ZakatResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isEligible)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("إجمالي الممتلكات", style = MaterialTheme.typography.bodyMedium)
                Text(String.format("%.2f", result.totalAssetsValue), fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("النصاب (85 جرام ذهب)", style = MaterialTheme.typography.bodyMedium)
                Text(String.format("%.2f", result.nisabThreshold), fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
            if (result.isEligible) {
                Text("الزكاة مستحقة (2.5%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = String.format("%.2f", result.totalZakatDue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    "الممتلكات أقل من النصاب، الزكاة غير مستحقة",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
