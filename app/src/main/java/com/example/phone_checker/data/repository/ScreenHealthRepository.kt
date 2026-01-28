package com.example.phone_checker.data.repository

import com.example.phone_checker.data.monitors.DisplayMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class ScreenHealthInfo(
    val displayName: String,
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val refreshRate: Float,
    val suggestedFrameRate: Float,
    val supportedRefreshRates: List<Float>,
    val rotation: Int,
    val displayState: Int,
    val isHdr: Boolean,
    val isWideColorGamut: Boolean,
    val displayCount: Int,
    val xDpi: Float,
    val yDpi: Float,
    val scaledDensity: Float,
    val density: Float,
    val physicalWidth: Float,
    val physicalHeight: Float,
    val diagonalInches: Float,
    val aspectRatio: String
)

interface ScreenHealthRepository {
    fun getScreenHealthInfo(): Flow<ScreenHealthInfo>
}

@Singleton
class ScreenHealthRepositoryImpl @Inject constructor(
    private val displayMonitor: DisplayMonitor
) : ScreenHealthRepository {

    override fun getScreenHealthInfo(): Flow<ScreenHealthInfo> = displayMonitor.displayState.map { displayState ->
        ScreenHealthInfo(
            displayName = displayState.displayName,
            widthPixels = displayState.primaryDisplayWidth,
            heightPixels = displayState.primaryDisplayHeight,
            densityDpi = displayState.densityDpi,
            refreshRate = displayState.refreshRate,
            suggestedFrameRate = displayState.suggestedFrameRate,
            supportedRefreshRates = displayState.supportedRefreshRates,
            rotation = displayState.rotation,
            displayState = displayState.displayState,
            isHdr = displayState.isHdr,
            isWideColorGamut = displayState.isWideColorGamut,
            displayCount = displayState.displayCount,
            xDpi = displayState.xDpi,
            yDpi = displayState.yDpi,
            scaledDensity = displayState.scaledDensity,
            density = displayState.density,
            physicalWidth = displayState.physicalWidth,
            physicalHeight = displayState.physicalHeight,
            diagonalInches = displayState.diagonalInches,
            aspectRatio = displayState.aspectRatio
        )
    }
}
