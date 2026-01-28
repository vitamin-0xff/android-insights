package com.example.phone_checker.ui.features.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.PerformanceInfo
import com.example.phone_checker.domain.usecase.GetPerformanceInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceUiState(
    val performanceInfo: PerformanceInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val getPerformanceInfoUseCase: GetPerformanceInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceUiState(isLoading = true))
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    init {
        loadPerformanceInfo()
    }

    private fun loadPerformanceInfo() {
        viewModelScope.launch {
            _uiState.value = PerformanceUiState(isLoading = true)
            getPerformanceInfoUseCase()
                .catch { e ->
                    _uiState.value = PerformanceUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = PerformanceUiState(performanceInfo = info)
                }
        }
    }

    fun refresh() {
        loadPerformanceInfo()
    }
}
