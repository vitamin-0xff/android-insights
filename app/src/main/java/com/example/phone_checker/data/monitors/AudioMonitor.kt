package com.example.phone_checker.data.monitors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AudioDeviceCategory {
    BUILT_IN,      // Built-in speaker, earpiece, mic
    WIRED,         // 3.5mm jack, line-in/out
    BLUETOOTH,     // BT Classic (A2DP, SCO), BLE Audio
    USB,           // USB-C audio, USB headsets, DACs
    HDMI,          // HDMI, HDMI ARC
    WIRELESS,      // Wi-Fi, network audio
    AUXILIARY,     // Dock, FM, hearing aid
    UNKNOWN        // Unclassified
}

enum class AudioDeviceRole {
    OUTPUT,        // Playback devices
    INPUT,         // Recording devices
    BIDIRECTIONAL  // Both (e.g., headset with mic)
}

data class AudioDevice(
    val id: Int,
    val name: String,
    val type: Int,
    val category: AudioDeviceCategory,
    val role: AudioDeviceRole,
    val address: String,
    val isWireless: Boolean,
    val sampleRates: IntArray,
    val channelCounts: IntArray,
    val connectionTime: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioDevice
        return id == other.id
    }

    override fun hashCode(): Int = id
}

data class AudioDeviceState(
    val connectedDevices: List<AudioDevice> = emptyList(),
    val outputDevices: List<AudioDevice> = emptyList(),
    val inputDevices: List<AudioDevice> = emptyList(),
    val hasWiredHeadset: Boolean = false,
    val hasBluetoothAudio: Boolean = false,
    val hasUsbAudio: Boolean = false,
    val activeOutputDevice: AudioDevice? = null,
    val activeInputDevice: AudioDevice? = null,
    val audioBecomingNoisy: Boolean = false,
    val lastUpdateTime: Long = 0L
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
    private var audioDeviceCallback: AudioDeviceCallback? = null

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
                            Log.d(TAG, "Headset plug event")
                            handleHeadsetPlugIntent(intent)
                        }
                        "android.media.AUDIO_BECOMING_NOISY" -> {
                            Log.d(TAG, "Audio becoming noisy - audio output disconnected")
                            _audioDeviceState.value = _audioDeviceState.value.copy(
                                audioBecomingNoisy = true,
                                lastUpdateTime = System.currentTimeMillis()
                            )
                        }
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(AudioManager.ACTION_HEADSET_PLUG)
                addAction("android.media.AUDIO_BECOMING_NOISY")
            }

            // ContextCompat.registerReceiver handles API level differences internally
            // For API 33+, it uses RECEIVER_EXPORTED by default (appropriate for audio intents)
            ContextCompat.registerReceiver(context, audioDeviceReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
            
            // Register AudioDeviceCallback for comprehensive device tracking (API 23+, minSdk is 24)
            audioDeviceCallback = object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                    Log.d(TAG, "${addedDevices.size} audio device(s) added")
                    queryAndUpdateDevices()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                    Log.d(TAG, "${removedDevices.size} audio device(s) removed")
                    queryAndUpdateDevices()
                }
            }
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
            
            _isMonitoring.value = true
            Log.d(TAG, "Audio monitoring started")

            // Query initial device state
            queryAndUpdateDevices()
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
            audioDeviceCallback?.let {
                audioManager.unregisterAudioDeviceCallback(it)
                Log.d(TAG, "Audio device callback unregistered")
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

    private fun queryAndUpdateDevices() {
        try {

            val inputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            val outputDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val audioDevices = arrayOf(*inputDevices, *outputDevices).map { deviceInfo ->
                createAudioDevice(deviceInfo)
            }
            
            // Compute convenience flags
            val hasWired = audioDevices.any { it.category == AudioDeviceCategory.WIRED }
            val hasBluetooth = audioDevices.any { it.category == AudioDeviceCategory.BLUETOOTH }
            val hasUsb = audioDevices.any { it.category == AudioDeviceCategory.USB }
            
            // Determine active devices (heuristic: prioritize external devices)
            val activeOutput = determineActiveDevice(audioDevices, isOutput = true)
            val activeInput = determineActiveDevice(audioDevices, isOutput = false)
            
            _audioDeviceState.value = _audioDeviceState.value.copy(
                connectedDevices = audioDevices,
                outputDevices = outputDevices.map { createAudioDevice(it) },
                inputDevices = inputDevices.map { createAudioDevice(it) },
                hasWiredHeadset = hasWired,
                hasBluetoothAudio = hasBluetooth,
                hasUsbAudio = hasUsb,
                activeOutputDevice = activeOutput,
                activeInputDevice = activeInput,
                audioBecomingNoisy = false, // Reset on device change
                lastUpdateTime = System.currentTimeMillis()
            )
            
            Log.d(TAG, "Audio devices updated: ${audioDevices.size} total, ${outputDevices.size} outputs, ${inputDevices.size} inputs")
        } catch (e: Exception) {
            Log.e(TAG, "Error querying audio devices", e)
        }
    }
    
    private fun createAudioDevice(deviceInfo: AudioDeviceInfo): AudioDevice {
        val category = categorizeDevice(deviceInfo)
        val role = determineDeviceRole(deviceInfo)
        val isWireless = category == AudioDeviceCategory.BLUETOOTH || 
                        category == AudioDeviceCategory.WIRELESS


        val address = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            deviceInfo.address
        }else {
            "QUERYING_ADDRESS_NOT_SUPPORTED"
        }

        return AudioDevice(
            id = deviceInfo.id,
            name = deviceInfo.productName?.toString() ?: "Unknown Device",
            type = deviceInfo.type,
            category = category,
            role = role,
            address = address,
            isWireless = isWireless,
            sampleRates = deviceInfo.sampleRates,
            channelCounts = deviceInfo.channelCounts,
            connectionTime = System.currentTimeMillis()
        )
    }
    
    private fun categorizeDevice(device: AudioDeviceInfo): AudioDeviceCategory {
        return when (device.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> AudioDeviceCategory.BUILT_IN
            
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_LINE_ANALOG,
            AudioDeviceInfo.TYPE_LINE_DIGITAL,
            AudioDeviceInfo.TYPE_AUX_LINE -> AudioDeviceCategory.WIRED
            
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> AudioDeviceCategory.BLUETOOTH
            
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_ACCESSORY -> AudioDeviceCategory.USB
            
            AudioDeviceInfo.TYPE_HDMI -> AudioDeviceCategory.HDMI
            
            AudioDeviceInfo.TYPE_TELEPHONY -> AudioDeviceCategory.AUXILIARY
            AudioDeviceInfo.TYPE_DOCK -> AudioDeviceCategory.AUXILIARY
            AudioDeviceInfo.TYPE_FM -> AudioDeviceCategory.AUXILIARY
            
            else -> {
                // Handle newer device types with API level checks
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && 
                        device.type == AudioDeviceInfo.TYPE_USB_HEADSET -> AudioDeviceCategory.USB
                    
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && 
                        device.type == AudioDeviceInfo.TYPE_HEARING_AID -> AudioDeviceCategory.AUXILIARY
                    
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
                        device.type == AudioDeviceInfo.TYPE_HDMI_ARC -> AudioDeviceCategory.HDMI
                    
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
                        device.type == AudioDeviceInfo.TYPE_IP -> AudioDeviceCategory.WIRELESS
                    
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                        device.type == AudioDeviceInfo.TYPE_BLE_HEADSET -> AudioDeviceCategory.BLUETOOTH
                    
                    else -> {
                        Log.w(TAG, "Unknown device type: ${device.type}, name: ${device.productName}")
                        AudioDeviceCategory.UNKNOWN
                    }
                }
            }
        }
    }
    
    private fun determineDeviceRole(device: AudioDeviceInfo): AudioDeviceRole {
        val isSource = device.isSource
        val isSink = device.isSink
        
        return when {
            isSource && isSink -> AudioDeviceRole.BIDIRECTIONAL
            isSink -> AudioDeviceRole.OUTPUT
            isSource -> AudioDeviceRole.INPUT
            else -> AudioDeviceRole.OUTPUT // Default fallback
        }
    }
    
    private fun determineActiveDevice(devices: List<AudioDevice>, isOutput: Boolean): AudioDevice? {
        // Priority: Wired > Bluetooth > USB > Built-in
        return devices.firstOrNull { it.category == AudioDeviceCategory.WIRED }
            ?: devices.firstOrNull { it.category == AudioDeviceCategory.BLUETOOTH }
            ?: devices.firstOrNull { it.category == AudioDeviceCategory.USB }
            ?: devices.firstOrNull { it.category == AudioDeviceCategory.HDMI }
            ?: devices.firstOrNull { it.category == AudioDeviceCategory.BUILT_IN }
    }
    
    private fun handleHeadsetPlugIntent(intent: Intent) {
        // ACTION_HEADSET_PLUG extras:
        // state: 0 = unplugged, 1 = plugged
        // name: headset name
        // microphone: 1 if headset has microphone
        val state = intent.getIntExtra("state", -1)
        val isConnected = state == 1
        
        Log.d(TAG, "Wired headset ${if (isConnected) "connected" else "disconnected"}")
        
        // Trigger full device query to update state
        queryAndUpdateDevices()
    }
}
