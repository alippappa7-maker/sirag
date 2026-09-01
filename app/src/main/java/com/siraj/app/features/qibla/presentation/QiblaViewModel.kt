package com.siraj.app.features.qibla.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class QiblaUiState(
    val qiblaDirection: Float = 0f,      // اتجاه القبلة بالدرجات
    val currentHeading: Float = 0f,       // اتجاه الهاتف الحالي
    val distanceToKaaba: Float = 0f,      // المسافة إلى الكعبة بالمتر
    val isCalibrated: Boolean = false,
    val compassAccuracy: Int = 0,        // دقة البوصلة
    val kaabaLatitude: Double = 21.4225, // خط عرض الكعبة
    val kaabaLongitude: Double = 39.8262, // خط طول الكعبة
    val userLatitude: Double = 0.0,
    val userLongitude: Double = 0.0,
    val arAvailable: Boolean = false,
    val isPointingToQibla: Boolean = false,
)

class QiblaViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var isAccelerometerSet = false
    private var isMagnetometerSet = false
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    init {
        sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun startSensors() {
        accelerometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        magnetometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stopSensors() {
        sensorManager?.unregisterListener(this)
    }

    fun setUserLocation(lat: Double, lng: Double) {
        val qiblaDirection = calculateQiblaDirection(lat, lng, 21.4225, 39.8262)
        val distance = calculateDistance(lat, lng, 21.4225, 39.8262)
        _uiState.value = _uiState.value.copy(
            userLatitude = lat,
            userLongitude = lng,
            qiblaDirection = qiblaDirection,
            distanceToKaaba = distance,
            isCalibrated = true,
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                isAccelerometerSet = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                isMagnetometerSet = true
            }
        }

        if (isAccelerometerSet && isMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // زاوية azimuth بالدرجات
            val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat().let {
                (it + 360) % 360
            }

            _uiState.value = _uiState.value.copy(
                currentHeading = azimuthInDegrees,
                compassAccuracy = if (Math.abs(azimuthInDegrees - _uiState.value.qiblaDirection) < 5) 1 else 0,
                isPointingToQibla = Math.abs(azimuthInDegrees - _uiState.value.qiblaDirection) < 5,
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _uiState.value = _uiState.value.copy(compassAccuracy = accuracy)
    }

    /**
     * حساب اتجاه القبلة باستخدام صيغة bearing
     */
    private fun calculateQiblaDirection(
        userLat: Double, userLng: Double,
        kaabaLat: Double, kaabaLng: Double,
    ): Float {
        val userLatRad = Math.toRadians(userLat)
        val kaabaLatRad = Math.toRadians(kaabaLat)
        val deltaLng = Math.toRadians(kaabaLng - userLng)

        val y = sin(deltaLng) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLng)

        val bearing = atan2(y, x)
        return ((Math.toDegrees(bearing) + 360) % 360).toFloat()
    }

    /**
     * حساب المسافة باستخدام صيغة Haversine
     */
    private fun calculateDistance(
        userLat: Double, userLng: Double,
        kaabaLat: Double, kaabaLng: Double,
    ): Float {
        val r = 6371000.0 // نصف قطر الأرض بالمتر
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(kaabaLat)
        val deltaPhi = Math.toRadians(kaabaLat - userLat)
        val deltaLambda = Math.toRadians(kaabaLng - userLng)

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
            cos(phi1) * cos(phi2) * sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return (r * c).toFloat()
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}
