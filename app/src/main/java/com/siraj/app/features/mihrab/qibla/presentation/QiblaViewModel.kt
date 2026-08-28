package com.siraj.app.features.mihrab.qibla.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

data class QiblaState(
    val azimuth: Float = 0f,
    val qiblaDirection: Float = 0f,
    val distanceKm: Float = 0f,
    val isSensorAvailable: Boolean = true,
    val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE,
    val needsCalibration: Boolean = true,
    val hasLocationPermission: Boolean = false
)

class QiblaViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    
    private val _state = MutableStateFlow(QiblaState(isSensorAvailable = rotationSensor != null))
    val state = _state.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    
    // Makkah Coordinates
    private val makkahLat = 21.422487
    private val makkahLng = 39.826206

    init {
        startSensor()
        // Default mock location for now (e.g. Riyadh)
        updateLocation(24.7136, 46.6753)
    }

    private fun startSensor() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            
            // Convert azimuth from radians to degrees
            var azimuthInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360f
            }
            
            _state.value = _state.value.copy(
                azimuth = azimuthInDegrees
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _state.value = _state.value.copy(
            accuracy = accuracy,
            needsCalibration = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
        )
    }
    
    fun setLocationPermissionGranted(granted: Boolean) {
        _state.value = _state.value.copy(hasLocationPermission = granted)
        if (granted) {
            // In a real app, request location updates from FusedLocationProviderClient here
            // Mocking update:
            updateLocation(24.7136, 46.6753) // Riyadh
        }
    }

    private fun updateLocation(userLat: Double, userLng: Double) {
        val qiblaDir = calculateQiblaDirection(userLat, userLng)
        val dist = calculateDistance(userLat, userLng)
        
        _state.value = _state.value.copy(
            qiblaDirection = qiblaDir.toFloat(),
            distanceKm = dist.toFloat()
        )
    }

    private fun calculateQiblaDirection(lat: Double, lng: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(makkahLat)
        val deltaLambda = Math.toRadians(makkahLng - lng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        
        var bearing = Math.toDegrees(atan2(y, x))
        if (bearing < 0) {
            bearing += 360.0
        }
        return bearing
    }

    private fun calculateDistance(lat: Double, lng: Double): Double {
        val r = 6371.0 // Earth radius in km
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(makkahLat)
        val deltaPhi = Math.toRadians(makkahLat - lat)
        val deltaLambda = Math.toRadians(makkahLng - lng)

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }
}
