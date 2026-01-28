package com.example.phone_checker.ui.features.thermal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.ThermalInfo
import com.example.phone_checker.domain.usecase.GetThermalInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThermalUiState(
    val thermalInfo: ThermalInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ThermalViewModel @Inject constructor(
    private val getThermalInfoUseCase: GetThermalInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThermalUiState(isLoading = true))
    val uiState: StateFlow<ThermalUiState> = _uiState.asStateFlow()

    init {
        loadThermalInfo()
    }

    private fun loadThermalInfo() {
        viewModelScope.launch {
            _uiState.value = ThermalUiState(isLoading = true)
            getThermalInfoUseCase()
                .catch { e ->
                    _uiState.value = ThermalUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = ThermalUiState(thermalInfo = info)
                }
        }
    }

    fun refresh() {
        loadThermalInfo()
    }
}
