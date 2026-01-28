package com.example.phone_checker.data.repository

import android.content.Context
import android.media.AudioManager
import com.example.phone_checker.data.monitors.AudioMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class AudioHealthInfo(
    val speakerVolume: Int, // 0-100%
    val speakerMaxVolume: Int,
    val microphoneStatus: MicrophoneStatus,
    val headphoneConnected: Boolean,
    val bluetoothAudioConnected: Boolean,
    val musicActive: Boolean,
    val callActive: Boolean,
    val recordingActive: Boolean,
    val speakerHealth: AudioDeviceHealth,
    val microphoneHealth: AudioDeviceHealth,
    val headphoneHealth: AudioDeviceHealth,
    val volumeWarnings: List<String>,
    val status: AudioHealthStatus,
    val recommendation: String
)

enum class MicrophoneStatus {
    AVAILABLE, MUTED, UNAVAILABLE
}

enum class AudioDeviceHealth {
    EXCELLENT, GOOD, FAIR, POOR
}

enum class AudioHealthStatus {
    HEALTHY, WARNING, CRITICAL
}

interface AudioRepository {
    fun getAudioHealthInfo(): Flow<AudioHealthInfo>
}

@Singleton
class AudioRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioMonitor: AudioMonitor
) : AudioRepository {

    override fun getAudioHealthInfo(): Flow<AudioHealthInfo> = combine(
        flow { emit(context.getSystemService(Context.AUDIO_SERVICE) as AudioManager) },
        audioMonitor.audioDeviceState
    ) { audioManager, deviceState ->
        
        // Get volume levels
        val speakerVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val speakerMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val speakerVolumePercent = (speakerVolume * 100) / speakerMaxVolume
        
        // Get microphone status
        val microphoneStatus = when {
            audioManager.isMicrophoneMute -> MicrophoneStatus.MUTED
            deviceState.inputAvailable -> MicrophoneStatus.AVAILABLE
            else -> MicrophoneStatus.UNAVAILABLE
        }
        
        // Get active streams
        val musicActive = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0
        val callActive = audioManager.mode == AudioManager.MODE_IN_CALL
        
        // Determine health based on volume and usage
        val speakerHealth = when {
            speakerVolumePercent > 90 -> AudioDeviceHealth.POOR
            speakerVolumePercent > 70 -> AudioDeviceHealth.FAIR
            speakerVolumePercent > 40 -> AudioDeviceHealth.GOOD
            else -> AudioDeviceHealth.EXCELLENT
        }
        
        val microphoneHealth = when {
            microphoneStatus == MicrophoneStatus.MUTED -> AudioDeviceHealth.FAIR
            microphoneStatus == MicrophoneStatus.AVAILABLE -> AudioDeviceHealth.EXCELLENT
            else -> AudioDeviceHealth.POOR
        }
        
        val volumeWarnings = mutableListOf<String>()
        if (speakerVolumePercent > 90) {
            volumeWarnings.add("Speaker volume is very high (${speakerVolumePercent}%). Risk of hearing damage.")
        }
        if (musicActive && speakerVolumePercent > 80) {
            volumeWarnings.add("Music playing at high volume. Use headphones for hearing protection.")
        }
        
        val status = when {
            volumeWarnings.isNotEmpty() -> AudioHealthStatus.WARNING
            !deviceState.isHealthy -> AudioHealthStatus.CRITICAL
            else -> AudioHealthStatus.HEALTHY
        }
        
        val recommendation = when {
            volumeWarnings.isNotEmpty() -> volumeWarnings.first()
            !deviceState.isHealthy -> "Audio device issue detected. Check connections."
            else -> "Audio system is healthy."
        }
        
        AudioHealthInfo(
            speakerVolume = speakerVolume,
            speakerMaxVolume = speakerMaxVolume,
            microphoneStatus = microphoneStatus,
            headphoneConnected = deviceState.outputAvailable,
            bluetoothAudioConnected = audioManager.isBluetoothScoOn,
            musicActive = musicActive,
            callActive = callActive,
            recordingActive = false,
            speakerHealth = speakerHealth,
            microphoneHealth = microphoneHealth,
            headphoneHealth = AudioDeviceHealth.GOOD,
            volumeWarnings = volumeWarnings,
            status = status,
            recommendation = recommendation
        )
    }
}
