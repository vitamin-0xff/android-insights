package com.example.phone_checker.data.monitors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AudioDeviceState(
    val outputAvailable: Boolean = false,
    val inputAvailable: Boolean = false,
    val isHealthy: Boolean = true
)

@Singleton
class AudioMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceMonitor {

    companion object {
        private const val TAG = "AudioMonitor"
    }

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _audioDeviceState = MutableStateFlow(AudioDeviceState())
    val audioDeviceState: StateFlow<AudioDeviceState> = _audioDeviceState.asStateFlow()

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var audioDeviceReceiver: BroadcastReceiver? = null

    override fun startMonitoring() {
        if (_isMonitoring.value) {
            Log.d(TAG, "Audio monitoring already active")
            return
        }

        try {
            audioDeviceReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        AudioManager.ACTION_HEADSET_PLUG -> {
                            Log.d(TAG, "Headset state changed")
                            updateAudioDeviceState()
                        }
                        "android.media.AUDIO_BECOMING_NOISY" -> {
                            Log.d(TAG, "Audio becoming noisy")
                            updateAudioDeviceState()
                        }
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(AudioManager.ACTION_HEADSET_PLUG)
                addAction("android.media.AUDIO_BECOMING_NOISY")
            }

            context.registerReceiver(audioDeviceReceiver, filter, Context.RECEIVER_EXPORTED)
            _isMonitoring.value = true
            Log.d(TAG, "Audio monitoring started")

            // Get initial state
            updateAudioDeviceState()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio monitoring", e)
            _isMonitoring.value = false
        }
    }

    override fun stopMonitoring() {
        if (!_isMonitoring.value) {
            Log.d(TAG, "Audio monitoring already stopped")
            return
        }

        try {
            audioDeviceReceiver?.let {
                context.unregisterReceiver(it)
                Log.d(TAG, "Audio receiver unregistered")
            }
            _isMonitoring.value = false
            Log.d(TAG, "Audio monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio monitoring", e)
        }
    }

    override fun cleanup() {
        stopMonitoring()
        _audioDeviceState.value = AudioDeviceState()
    }

    private fun updateAudioDeviceState() {
        try {
            val isSpeakerphoneOn = audioManager.isSpeakerphoneOn
            val isBluetoothScoOn = audioManager.isBluetoothScoOn
            val isWiredHeadsetOn = audioManager.isWiredHeadsetOn

            val outputAvailable = isSpeakerphoneOn || isBluetoothScoOn || isWiredHeadsetOn
            val inputAvailable = audioManager.isMicrophoneMute == false
            val isHealthy = outputAvailable || inputAvailable

            _audioDeviceState.value = AudioDeviceState(
                outputAvailable = outputAvailable,
                inputAvailable = inputAvailable,
                isHealthy = isHealthy
            )

            Log.d(TAG, "Audio device state updated: output=$outputAvailable, input=$inputAvailable")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating audio device state", e)
        }
    }
}
