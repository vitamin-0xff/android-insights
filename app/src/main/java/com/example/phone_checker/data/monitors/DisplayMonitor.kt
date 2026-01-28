package com.example.phone_checker.data.monitors

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class DisplayState(
    val primaryDisplayWidth: Int = 0,
    val primaryDisplayHeight: Int = 0,
    val displayCount: Int = 0,
    val refreshRate: Float = 60f,
    val isHealthy: Boolean = true
)

@Singleton
class DisplayMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceMonitor {

    companion object {
        private const val TAG = "DisplayMonitor"
    }

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _displayState = MutableStateFlow(DisplayState())
    val displayState: StateFlow<DisplayState> = _displayState.asStateFlow()

    private val displayManager by lazy {
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    private var displayListener: DisplayManager.DisplayListener? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun startMonitoring() {
        if (_isMonitoring.value) {
            Log.d(TAG, "Display monitoring already active")
            return
        }

        try {
            displayListener = object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {
                    Log.d(TAG, "Display added: $displayId")
                    updateDisplayState()
                }

                override fun onDisplayRemoved(displayId: Int) {
                    Log.d(TAG, "Display removed: $displayId")
                    updateDisplayState()
                }

                override fun onDisplayChanged(displayId: Int) {
                    Log.d(TAG, "Display changed: $displayId")
                    updateDisplayState()
                }
            }

            displayListener?.let {
                displayManager.registerDisplayListener(it, handler)
                _isMonitoring.value = true
                Log.d(TAG, "Display monitoring started")

                // Get initial state
                updateDisplayState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting display monitoring", e)
            _isMonitoring.value = false
        }
    }

    override fun stopMonitoring() {
        if (!_isMonitoring.value) {
            Log.d(TAG, "Display monitoring already stopped")
            return
        }

        try {
            displayListener?.let {
                displayManager.unregisterDisplayListener(it)
                Log.d(TAG, "Display listener unregistered")
            }
            _isMonitoring.value = false
            Log.d(TAG, "Display monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping display monitoring", e)
        }
    }

    override fun cleanup() {
        stopMonitoring()
        _displayState.value = DisplayState()
    }

    private fun updateDisplayState() {
        try {
            val primaryDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

            val width = primaryDisplay?.width ?: 0
            val height = primaryDisplay?.height ?: 0
            val refreshRate = primaryDisplay?.refreshRate ?: 60f
            val displayCount = displayManager.displays.size
            val isHealthy = width > 0 && height > 0

            _displayState.value = DisplayState(
                primaryDisplayWidth = width,
                primaryDisplayHeight = height,
                displayCount = displayCount,
                refreshRate = refreshRate,
                isHealthy = isHealthy
            )

            Log.d(TAG, "Display state updated: ${width}x${height}, refreshRate=${refreshRate}Hz, count=$displayCount")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating display state", e)
        }
    }
}
