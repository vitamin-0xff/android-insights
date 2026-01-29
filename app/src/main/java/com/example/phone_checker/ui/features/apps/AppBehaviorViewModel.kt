package com.example.phone_checker.ui.features.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.AppBehaviorInfo
import com.example.phone_checker.data.repository.UsageInterval
import com.example.phone_checker.domain.usecase.GetAppBehaviorInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppBehaviorUiState(
    val appBehaviorInfo: AppBehaviorInfo? = null,
    val selectedInterval: UsageInterval = UsageInterval.TODAY,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AppBehaviorViewModel @Inject constructor(
    private val getAppBehaviorInfoUseCase: GetAppBehaviorInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppBehaviorUiState(isLoading = true))
    val uiState: StateFlow<AppBehaviorUiState> = _uiState.asStateFlow()
    
    private var loadingJob: Job? = null

    init {
        loadAppBehaviorInfo()
    }

    private fun loadAppBehaviorInfo(interval: UsageInterval = UsageInterval.TODAY) {
        // Cancel previous loading job
        loadingJob?.cancel()
        
        loadingJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedInterval = interval, error = null)
            getAppBehaviorInfoUseCase(interval)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = AppBehaviorUiState(
                        appBehaviorInfo = info,
                        selectedInterval = interval,
                        isLoading = false
                    )
                }
        }
    }

    fun changeInterval(interval: UsageInterval) {
        loadAppBehaviorInfo(interval)
    }

    fun refresh() {
        loadAppBehaviorInfo(_uiState.value.selectedInterval)
    }
}
