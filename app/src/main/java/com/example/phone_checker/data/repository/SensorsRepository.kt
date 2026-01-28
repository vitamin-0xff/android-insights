package com.example.phone_checker.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class SensorInfo(
    val name: String,
    val type: String,
    val vendor: String,
    val power: Float, // mA
    val maxRange: Float,
    val resolution: Float,
    val isAvailable: Boolean,
    val status: SensorStatus
)

data class SensorsHealthInfo(
    val totalSensors: Int,
    val activeSensors: Int,
    val accelerometer: SensorInfo?,
    val gyroscope: SensorInfo?,
    val magnetometer: SensorInfo?,
    val proximity: SensorInfo?,
    val ambientLight: SensorInfo?,
    val pressure: SensorInfo?,
    val humidity: SensorInfo?,
    val temperature: SensorInfo?,
    val allSensors: List<SensorInfo>,
    val status: SensorsHealthStatus,
    val recommendation: String
)

enum class SensorStatus {
    AVAILABLE, UNAVAILABLE, CALIBRATION_NEEDED
}

enum class SensorsHealthStatus {
    EXCELLENT, GOOD, POOR, UNAVAILABLE
}

interface SensorsRepository {
    fun getSensorsHealthInfo(): Flow<SensorsHealthInfo>
}

@Singleton
class SensorsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorsRepository {

    override fun getSensorsHealthInfo(): Flow<SensorsHealthInfo> = flow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        val allSensors = try {
            sensorManager.getSensorList(Sensor.TYPE_ALL)
        } catch (e: Exception) {
            emptyList()
        }
        
        val sensorsByType = mutableMapOf<Int, Sensor>()
        val sensorsList = mutableListOf<SensorInfo>()
        
        for (sensor in allSensors) {
            sensorsByType[sensor.type] = sensor
            val sensorInfo = SensorInfo(
                name = sensor.name,
                type = getSensorTypeName(sensor.type, sensor.name),
                vendor = sensor.vendor,
                power = sensor.power,
                maxRange = sensor.maximumRange,
                resolution = sensor.resolution,
                isAvailable = true,
                status = getSensorStatus(sensor)
            )
            sensorsList.add(sensorInfo)
        }
        
        val accelerometer = sensorsByType[Sensor.TYPE_ACCELEROMETER]?.let { convertToSensorInfo(it) }
        val gyroscope = sensorsByType[Sensor.TYPE_GYROSCOPE]?.let { convertToSensorInfo(it) }
        val magnetometer = sensorsByType[Sensor.TYPE_MAGNETIC_FIELD]?.let { convertToSensorInfo(it) }
        val proximity = sensorsByType[Sensor.TYPE_PROXIMITY]?.let { convertToSensorInfo(it) }
        val ambientLight = sensorsByType[Sensor.TYPE_LIGHT]?.let { convertToSensorInfo(it) }
        val pressure = sensorsByType[Sensor.TYPE_PRESSURE]?.let { convertToSensorInfo(it) }
        val humidity = sensorsByType[Sensor.TYPE_RELATIVE_HUMIDITY]?.let { convertToSensorInfo(it) }
        val temperature = sensorsByType[Sensor.TYPE_AMBIENT_TEMPERATURE]?.let { convertToSensorInfo(it) }
        
        val activeSensors = sensorsList.count { it.isAvailable }
        val calibrationNeeded = sensorsList.count { it.status == SensorStatus.CALIBRATION_NEEDED }
        
        val status = when {
            activeSensors == 0 -> SensorsHealthStatus.UNAVAILABLE
            calibrationNeeded > 2 -> SensorsHealthStatus.POOR
            activeSensors < 5 -> SensorsHealthStatus.GOOD
            else -> SensorsHealthStatus.EXCELLENT
        }
        
        val recommendation = when {
            activeSensors == 0 -> "No sensors available on this device. Some features may not work properly."
            calibrationNeeded > 2 -> "Multiple sensors need calibration. Go to Settings > Apps > Sensors and recalibrate."
            ambientLight == null && proximity == null -> "Light and proximity sensors missing. Auto-brightness may not work optimally."
            gyroscope == null -> "Gyroscope not available. Motion-based features disabled."
            else -> "All critical sensors are functioning properly."
        }
        
        emit(
            SensorsHealthInfo(
                totalSensors = allSensors.size,
                activeSensors = activeSensors,
                accelerometer = accelerometer,
                gyroscope = gyroscope,
                magnetometer = magnetometer,
                proximity = proximity,
                ambientLight = ambientLight,
                pressure = pressure,
                humidity = humidity,
                temperature = temperature,
                allSensors = sensorsList,
                status = status,
                recommendation = recommendation
            )
        )
    }
    
    private fun convertToSensorInfo(sensor: Sensor): SensorInfo {
        return SensorInfo(
            name = sensor.name,
            type = getSensorTypeName(sensor.type, sensor.name),
            vendor = sensor.vendor,
            power = sensor.power,
            maxRange = sensor.maximumRange,
            resolution = sensor.resolution,
            isAvailable = true,
            status = getSensorStatus(sensor)
        )
    }

    private fun getSensorStatus(sensor: Sensor): SensorStatus {
        return when {
            sensor.type == Sensor.TYPE_ACCELEROMETER && sensor.power > 5f -> SensorStatus.CALIBRATION_NEEDED
            sensor.type == Sensor.TYPE_MAGNETIC_FIELD && sensor.power > 6f -> SensorStatus.CALIBRATION_NEEDED
            else -> SensorStatus.AVAILABLE
        }
    }

    @Suppress("DEPRECATION")
    private fun getSensorTypeName(type: Int, fallbackName: String? = null): String {
        val standardName = when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
            Sensor.TYPE_ORIENTATION -> "Orientation"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_LIGHT -> "Light"
            Sensor.TYPE_PRESSURE -> "Barometer"
            Sensor.TYPE_TEMPERATURE -> "Temperature"
            Sensor.TYPE_PROXIMITY -> "Proximity"
            Sensor.TYPE_GRAVITY -> "Gravity"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient Temperature"
            Sensor.TYPE_STEP_COUNTER -> "Step Counter"
            Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
            Sensor.TYPE_SIGNIFICANT_MOTION -> "Significant Motion"
            Sensor.TYPE_HEART_RATE -> "Heart Rate"
            else -> null
        }
        return standardName ?: fallbackName?.ifBlank { null } ?: "Sensor $type"
    }
}
