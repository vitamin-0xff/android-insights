package com.example.phone_checker.data.monitors

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class DisplayState(
    val displayName: String = "",
    val primaryDisplayWidth: Int = 0,
    val primaryDisplayHeight: Int = 0,
    val displayCount: Int = 0,
    val refreshRate: Float = 60f,
    val suggestedFrameRate: Float = 0f,
    val supportedRefreshRates: List<Float> = emptyList(),
    val densityDpi: Int = 0,
    val rotation: Int = 0,
    val displayState: Int = Display.STATE_UNKNOWN,
    val isHdr: Boolean = false,
    val isWideColorGamut: Boolean = false,
    val xDpi: Float = 0f,
    val yDpi: Float = 0f,
    val scaledDensity: Float = 0f,
    val density: Float = 0f,
    val physicalWidth: Float = 0f,
    val physicalHeight: Float = 0f,
    val diagonalInches: Float = 0f,
    val aspectRatio: String = ""
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

            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            primaryDisplay?.getRealMetrics(metrics)

            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val refreshRate = primaryDisplay?.refreshRate ?: 60f
            val displayCount = displayManager.displays.size
            val displayName = primaryDisplay?.name ?: "Unknown"
            val rotation = primaryDisplay?.rotation ?: 0
            val displayState = primaryDisplay?.state ?: Display.STATE_UNKNOWN
            val supportedRefreshRates = primaryDisplay?.supportedModes
                ?.map { it.refreshRate }
                ?.distinct()
                ?.sorted()
                ?: emptyList()
            val suggestedFrameRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val method = Display::class.java.getMethod("getSuggestedFrameRate")
                    (method.invoke(primaryDisplay) as? Float) ?: 0f
                } catch (e: Exception) {
                    0f
                }
            } else {
                0f
            }
            val isHdr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                primaryDisplay?.isHdr == true
            } else {
                false
            }
            val isWideColorGamut = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                primaryDisplay?.isWideColorGamut == true
            } else {
                false
            }

            // Calculate DPI and physical dimensions
            val xDpi = metrics.xdpi
            val yDpi = metrics.ydpi
            val density = metrics.density
            @Suppress("DEPRECATION")
            val scaledDensity = metrics.scaledDensity
            
            // Calculate physical dimensions in inches
            val physicalWidth = width / xDpi
            val physicalHeight = height / yDpi
            val diagonalInches = kotlin.math.sqrt((physicalWidth * physicalWidth) + (physicalHeight * physicalHeight))
            
            // Calculate aspect ratio
            val gcd = gcd(width, height)
            val aspectRatioWidth = width / gcd
            val aspectRatioHeight = height / gcd
            val aspectRatio = "$aspectRatioWidth:$aspectRatioHeight"

            _displayState.value = DisplayState(
                displayName = displayName,
                primaryDisplayWidth = width,
                primaryDisplayHeight = height,
                displayCount = displayCount,
                refreshRate = refreshRate,
                suggestedFrameRate = suggestedFrameRate,
                supportedRefreshRates = supportedRefreshRates,
                densityDpi = metrics.densityDpi,
                rotation = rotation,
                displayState = displayState,
                isHdr = isHdr,
                isWideColorGamut = isWideColorGamut,
                xDpi = xDpi,
                yDpi = yDpi,
                scaledDensity = scaledDensity,
                density = density,
                physicalWidth = physicalWidth,
                physicalHeight = physicalHeight,
                diagonalInches = diagonalInches,
                aspectRatio = aspectRatio
            )

            Log.d(
                TAG,
                "Display state updated: ${width}x${height}, dpi=${metrics.densityDpi}, aspect=$aspectRatio, diagonal=${String.format("%.2f", diagonalInches)}in"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating display state", e)
        }
    }

    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }
}
