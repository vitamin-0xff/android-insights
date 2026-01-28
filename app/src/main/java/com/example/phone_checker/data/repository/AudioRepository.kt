package com.example.phone_checker.data.repository

import android.content.Context
import android.media.AudioManager
import com.example.phone_checker.data.monitors.AudioDeviceCategory
import com.example.phone_checker.data.monitors.AudioDevice
import com.example.phone_checker.data.monitors.AudioDeviceRole
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
    val recommendation: String,
    val inputDevices: List<AudioDevice> = emptyList(),
    val outputDevices: List<AudioDevice> = emptyList()
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
        
        // Compute external devices
        val externalDevices = deviceState.connectedDevices.filter { 
            it.category != AudioDeviceCategory.BUILT_IN 
        }
        val hasExternalOutput = externalDevices.any { 
            it.role == com.example.phone_checker.data.monitors.AudioDeviceRole.OUTPUT || 
            it.role == com.example.phone_checker.data.monitors.AudioDeviceRole.BIDIRECTIONAL 
        }
        
        // Get microphone status
        val microphoneStatus = when {
            audioManager.isMicrophoneMute -> MicrophoneStatus.MUTED
            deviceState.hasWiredHeadset || deviceState.hasBluetoothAudio -> MicrophoneStatus.AVAILABLE
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
        
        val microphoneHealth = when (microphoneStatus) {
            MicrophoneStatus.MUTED -> AudioDeviceHealth.FAIR
            MicrophoneStatus.AVAILABLE -> AudioDeviceHealth.EXCELLENT
            else -> AudioDeviceHealth.POOR
        }
        
        // Evaluate headphone health based on audio quality capabilities
        val headphoneHealth = deviceState.activeOutputDevice?.let { device ->
            when {
                device.category == AudioDeviceCategory.BUILT_IN -> AudioDeviceHealth.FAIR
                device.sampleRates.any { it >= 48000 } -> AudioDeviceHealth.EXCELLENT
                device.sampleRates.any { it >= 44100 } -> AudioDeviceHealth.GOOD
                else -> AudioDeviceHealth.FAIR
            }
        } ?: AudioDeviceHealth.GOOD
        
        val volumeWarnings = mutableListOf<String>()
        if (speakerVolumePercent > 90) {
            volumeWarnings.add("Speaker volume is very high (${speakerVolumePercent}%). Risk of hearing damage.")
        }
        if (musicActive && speakerVolumePercent > 80 && hasExternalOutput) {
            volumeWarnings.add("Music playing at high volume through headphones. Reduce volume for hearing protection.")
        }
        
        val status = when {
            volumeWarnings.isNotEmpty() -> AudioHealthStatus.WARNING
            deviceState.audioBecomingNoisy -> AudioHealthStatus.WARNING
            else -> AudioHealthStatus.HEALTHY
        }
        
        val recommendation = when {
            volumeWarnings.isNotEmpty() -> volumeWarnings.first()
            deviceState.audioBecomingNoisy -> "Audio output disconnected. Check connections."
            externalDevices.isEmpty() -> "Connect headphones for better audio quality."
            else -> "Audio system is healthy."
        }
        
        AudioHealthInfo(
            speakerVolume = speakerVolume,
            speakerMaxVolume = speakerMaxVolume,
            microphoneStatus = microphoneStatus,
            headphoneConnected = deviceState.hasWiredHeadset,
            bluetoothAudioConnected = deviceState.hasBluetoothAudio,
            musicActive = musicActive,
            callActive = callActive,
            recordingActive = false,
            speakerHealth = speakerHealth,
            microphoneHealth = microphoneHealth,
            headphoneHealth = headphoneHealth,
            volumeWarnings = volumeWarnings,
            status = status,
            recommendation = recommendation,
            inputDevices = deviceState.inputDevices,
            outputDevices = deviceState.outputDevices
        )
    }
}
