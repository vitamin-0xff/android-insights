package com.example.phone_checker.ui.features.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.SensorsHealthInfo
import com.example.phone_checker.domain.usecase.GetSensorsHealthInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SensorsUiState(
    val sensorsInfo: SensorsHealthInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SensorsViewModel @Inject constructor(
    private val getSensorsHealthInfoUseCase: GetSensorsHealthInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorsUiState(isLoading = true))
    val uiState: StateFlow<SensorsUiState> = _uiState.asStateFlow()

    init {
        loadSensorsInfo()
    }

    private fun loadSensorsInfo() {
        viewModelScope.launch {
            _uiState.value = SensorsUiState(isLoading = true)
            getSensorsHealthInfoUseCase()
                .catch { e ->
                    _uiState.value = SensorsUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = SensorsUiState(sensorsInfo = info)
                }
        }
    }

    fun refresh() {
        loadSensorsInfo()
    }
}
