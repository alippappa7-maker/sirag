package com.siraj.app.domain.models.zakat

/**
 * نماذج حساب الزكاة
 * المرجع: الأحكام الفقهية المعتمدة في الزكاة
 */

enum class ZakatType(val arabicName: String, val nisab: Double) {
    GOLD("زكاة الذهب (بالحلق أو النقدي)", 85.0),        // 85 جرام ذهب
    SILVER("زكاة الفضة", 595.0),                        // 595 جرام فضة
    CASH("زكاة النقد والمدخرات", 0.0),                 // محسوب بقيمة الذهب
    TRADE_GOODS("زكاة عروض التجارة", 0.0),              // محسوب بقيمة الذهب
    FITR("زكاة الفطر", 0.0),                           // صاع من الطعام لكل فرد
}

data class ZakatCalculation(
    val type: ZakatType,
    val totalAmount: Double,
    val nisabValue: Double,
    val zakatDue: Double,
    val isAboveNisab: Boolean,
    val currency: String = "SAR",
)

data class ZakatAsset(
    val id: String,
    val type: ZakatType,
    val amount: Double,
    val unit: String,
    val valueInCurrency: Double,
)

data class ZakatResult(
    val totalAssetsValue: Double,
    val totalZakatDue: Double,
    val nisabThreshold: Double,
    val isEligible: Boolean,
    val zakatPercentage: Double = 0.025,  // 2.5%
    val calculations: List<ZakatCalculation>,
)

data class ZakatFitrCalculation(
    val householdMembers: Int,
    val perPersonAmount: Double,
    val currency: String,
    val totalDue: Double,
)
