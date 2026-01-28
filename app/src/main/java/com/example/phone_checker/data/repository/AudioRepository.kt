package com.example.phone_checker.data.repository

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
    @ApplicationContext private val context: Context
) : AudioRepository {

    override fun getAudioHealthInfo(): Flow<AudioHealthInfo> = flow {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Get volume levels
        val speakerVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val speakerMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val speakerVolumePercent = (speakerVolume * 100) / speakerMaxVolume
        
        // Get microphone status
        val microphoneStatus = when {
            audioManager.isMicrophoneMute -> MicrophoneStatus.MUTED
            else -> MicrophoneStatus.AVAILABLE
        }
        
        // Check connected devices
//        val headphoneConnected = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)[0]
        val bluetoothAudioConnected = audioManager.isBluetoothScoOn
        
        // Get active streams
        val musicActive = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0
        val callActive = audioManager.mode == AudioManager.MODE_IN_CALL
        val recordingActive = false // Would need MediaRecorder to know for sure
        
        // Determine health based on volume and usage
        val speakerHealth = when {
            speakerVolumePercent > 90 -> AudioDeviceHealth.POOR // Risk of speaker damage
            speakerVolumePercent > 70 -> AudioDeviceHealth.FAIR
            speakerVolumePercent > 40 -> AudioDeviceHealth.GOOD
            else -> AudioDeviceHealth.EXCELLENT
        }
        
        val microphoneHealth = when {
            microphoneStatus == MicrophoneStatus.MUTED -> AudioDeviceHealth.FAIR
            else -> AudioDeviceHealth.EXCELLENT
        }
        
        val headphoneHealth = when {
            bluetoothAudioConnected -> AudioDeviceHealth.GOOD
            else -> AudioDeviceHealth.FAIR
        }
        
        val volumeWarnings = mutableListOf<String>()
        if (speakerVolumePercent > 90) {
            volumeWarnings.add("Speaker volume is very high (${speakerVolumePercent}%). Risk of hearing damage and speaker wear.")
        }
        if (musicActive && speakerVolumePercent > 80) {
            volumeWarnings.add("Music playing at high volume. Use headphones for hearing protection.")
        }
        if (callActive && microphoneStatus == MicrophoneStatus.MUTED) {
            volumeWarnings.add("Microphone is muted during a call. Other party may not hear you.")
        }
        
        val status = when {
            volumeWarnings.isNotEmpty() -> AudioHealthStatus.WARNING
            speakerHealth == AudioDeviceHealth.POOR -> AudioHealthStatus.CRITICAL
            else -> AudioHealthStatus.HEALTHY
        }
        
        val recommendation = when {
            volumeWarnings.isNotEmpty() -> volumeWarnings.first()
            speakerHealth == AudioDeviceHealth.POOR -> "Reduce speaker volume to prevent damage and hearing loss."
            microphoneStatus == MicrophoneStatus.MUTED -> "Microphone is muted. Unmute if you need to make calls."
            else -> "Audio system is healthy."
        }
        
        emit(
            AudioHealthInfo(
                speakerVolume = speakerVolume,
                speakerMaxVolume = speakerMaxVolume,
                microphoneStatus = microphoneStatus,
                bluetoothAudioConnected = bluetoothAudioConnected,
                musicActive = musicActive,
                callActive = callActive,
                recordingActive = recordingActive,
                speakerHealth = speakerHealth,
                microphoneHealth = microphoneHealth,
                headphoneHealth = headphoneHealth,
                volumeWarnings = volumeWarnings,
                status = status,
                recommendation = recommendation,
                headphoneConnected = false
            )
        )
    }
}
