package com.siraj.app.features.tasbih.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TasbihUiState(
    val count: Int = 0,
    val target: Int = 33,
    val totalDhikr: Int = 0,
    val selectedDhikr: TasbihDhikr = TasbihDhikr.getDefault(),
    val dhikrList: List<TasbihDhikr> = TasbihDhikr.getList(),
    val isCompleted: Boolean = false,
    val dailyTotal: Int = 0,
    val weeklyStats: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0),
)

data class TasbihDhikr(
    val id: String,
    val text: String,
    val arabicText: String,
    val targetCount: Int = 33,
    val description: String,
) {
    companion object {
        fun getDefault() = TasbihDhikr("subhan_allah", "سبحان الله", "سبحان الله", 33, "تسبيح بعد الصلاة")

        fun getList() = listOf(
            TasbihDhikr("subhan_allah", "سبحان الله", "سبحان الله", 33, "تسبيح بعد الصلاة"),
            TasbihDhikr("alhamdulillah", "الحمد لله", "الحمد لله", 33, "تحميد بعد الصلاة"),
            TasbihDhikr("allahu_akbar", "الله أكبر", "الله أكبر", 34, "تكبير بعد الصلاة"),
            TasbihDhikr("la_ilaha_illa_allah", "لا إله إلا الله", "لا إله إلا الله", 100, "تسبيح عام"),
            TasbihDhikr("subhan_allah_wa_bihamdih", "سبحان الله وبحمده", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", 100, "تسبيح الصباح والمساء"),
            TasbihDhikr("astaghfirullah", "أستغفر الله", "أَسْتَغْفِرُ اللَّهَ", 100, "استغفار"),
            TasbihDhikr("subhan_allah_wa_bihamdih_100", "سبحان الله وبحمده 100 مرة", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", 100, "من قالها مئة مرة حُطَّت خطاياه"),
            TasbihDhikr("la_hawla", "لا حول ولا قوة إلا بالله", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", 100, "كنز من كنوز الجنة"),
        )
    }
}

class TasbihViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TasbihUiState())
    val uiState: StateFlow<TasbihUiState> = _uiState.asStateFlow()

    fun increment(context: Context) {
        val current = _uiState.value
        val newCount = current.count + 1
        val isCompleted = newCount >= current.target

        if (isCompleted) {
            vibrate(context, longDuration = true)
        } else {
            vibrate(context, longDuration = false)
        }

        _uiState.value = current.copy(
            count = newCount,
            totalDhikr = current.totalDhikr + 1,
            dailyTotal = current.dailyTotal + 1,
            isCompleted = isCompleted,
        )
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(count = 0, isCompleted = false)
    }

    fun selectDhikr(dhikr: TasbihDhikr) {
        _uiState.value = _uiState.value.copy(
            selectedDhikr = dhikr,
            count = 0,
            target = dhikr.targetCount,
            isCompleted = false,
        )
    }

    fun setTarget(target: Int) {
        _uiState.value = _uiState.value.copy(target = target, count = 0, isCompleted = false)
    }

    private fun vibrate(context: Context, longDuration: Boolean) {
        try {
            val duration = if (longDuration) 200L else 30L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.let {
                    it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {
            // تجاهل أخطاء الاهتزاز
        }
    }
}
