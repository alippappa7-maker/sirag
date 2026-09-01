package com.siraj.app.features.zakat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.app.domain.models.zakat.ZakatType
import com.siraj.app.domain.models.zakat.ZakatResult
import com.siraj.app.domain.models.zakat.ZakatCalculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ZakatUiState(
    val goldPrice: String = "",
    val silverPrice: String = "",
    val cashAmount: String = "",
    val tradeGoodsValue: String = "",
    val debtsOwed: String = "",
    val zakatResult: ZakatResult? = null,
    val fitrHouseholdMembers: String = "1",
    val fitrPerPerson: String = "20",
    val fitrCurrency: String = "SAR",
    val fitrResult: Double? = null,
    val selectedTab: Int = 0,
)

class ZakatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ZakatUiState())
    val uiState: StateFlow<ZakatUiState> = _uiState.asStateFlow()

    fun updateGoldPrice(value: String) { _uiState.value = _uiState.value.copy(goldPrice = value) }
    fun updateSilverPrice(value: String) { _uiState.value = _uiState.value.copy(silverPrice = value) }
    fun updateCashAmount(value: String) { _uiState.value = _uiState.value.copy(cashAmount = value) }
    fun updateTradeGoods(value: String) { _uiState.value = _uiState.value.copy(tradeGoodsValue = value) }
    fun updateDebts(value: String) { _uiState.value = _uiState.value.copy(debtsOwed = value) }
    fun updateFitrMembers(value: String) { _uiState.value = _uiState.value.copy(fitrHouseholdMembers = value) }
    fun updateFitrPerPerson(value: String) { _uiState.value = _uiState.value.copy(fitrPerPerson = value) }
    fun selectTab(tab: Int) { _uiState.value = _uiState.value.copy(selectedTab = tab) }

    fun calculateZakat() {
        val goldPrice = _uiState.value.goldPrice.toDoubleOrNull() ?: 0.0
        val silverPrice = _uiState.value.silverPrice.toDoubleOrNull() ?: 0.0
        val cash = _uiState.value.cashAmount.toDoubleOrNull() ?: 0.0
        val trade = _uiState.value.tradeGoodsValue.toDoubleOrNull() ?: 0.0
        val debts = _uiState.value.debtsOwed.toDoubleOrNull() ?: 0.0

        // النصاب: 85 جرام ذهب × سعر الجرام
        val nisabThreshold = 85.0 * goldPrice

        // المبلغ الخاضع للزكاة
        val netAssets = (cash + trade + (silverPrice * 595.0)) - debts
        val isEligible = netAssets >= nisabThreshold && netAssets > 0

        val calculations = mutableListOf<ZakatCalculation>()
        if (goldPrice > 0) {
            calculations.add(ZakatCalculation(ZakatType.GOLD, goldPrice * 85.0, nisabThreshold, goldPrice * 85.0 * 0.025, true))
        }
        if (cash > 0) {
            calculations.add(ZakatCalculation(ZakatType.CASH, cash, nisabThreshold, if (isEligible) cash * 0.025 else 0.0, isEligible))
        }
        if (trade > 0) {
            calculations.add(ZakatCalculation(ZakatType.TRADE_GOODS, trade, nisabThreshold, if (isEligible) trade * 0.025 else 0.0, isEligible))
        }

        val totalZakat = if (isEligible) netAssets * 0.025 else 0.0

        _uiState.value = _uiState.value.copy(
            zakatResult = ZakatResult(
                totalAssetsValue = netAssets,
                totalZakatDue = totalZakat,
                nisabThreshold = nisabThreshold,
                isEligible = isEligible,
                calculations = calculations,
            )
        )
    }

    fun calculateFitr() {
        val members = _uiState.value.fitrHouseholdMembers.toIntOrNull() ?: 1
        val perPerson = _uiState.value.fitrPerPerson.toDoubleOrNull() ?: 0.0
        _uiState.value = _uiState.value.copy(fitrResult = members * perPerson)
    }
}
