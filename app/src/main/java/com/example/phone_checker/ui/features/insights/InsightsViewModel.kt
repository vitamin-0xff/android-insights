package com.example.phone_checker.ui.features.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.domain.model.DeviceInsight
import com.example.phone_checker.domain.model.InsightSeverity
import com.example.phone_checker.domain.usecase.GetDeviceInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val insights: List<DeviceInsight> = emptyList(),
    val criticalCount: Int = 0,
    val warningCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getDeviceInsightsUseCase: GetDeviceInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState(isLoading = true))
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            _uiState.value = InsightsUiState(isLoading = true)
            getDeviceInsightsUseCase()
                .catch { e ->
                    _uiState.value = InsightsUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { insights ->
                    _uiState.value = InsightsUiState(
                        insights = insights.sortedByDescending { 
                            when(it.severity) {
                                InsightSeverity.CRITICAL -> 3
                                InsightSeverity.WARNING -> 2
                                InsightSeverity.INFO -> 1
                                InsightSeverity.POSITIVE -> 0
                            }
                        },
                        criticalCount = insights.count { it.severity == InsightSeverity.CRITICAL },
                        warningCount = insights.count { it.severity == InsightSeverity.WARNING }
                    )
                }
        }
    }

    fun refresh() {
        loadInsights()
    }
}
