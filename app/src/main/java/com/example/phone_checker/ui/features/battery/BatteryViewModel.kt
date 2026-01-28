package com.example.phone_checker.ui.features.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.BatteryInfo
import com.example.phone_checker.domain.usecase.GetBatteryInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatteryUiState(
    val batteryInfo: BatteryInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BatteryViewModel @Inject constructor(
    private val getBatteryInfoUseCase: GetBatteryInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState(isLoading = true))
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()
    init {
        loadBatteryInfo()
    }
    private fun loadBatteryInfo() {
        viewModelScope.launch {
            _uiState.value = BatteryUiState(isLoading = true)
            getBatteryInfoUseCase()
                .catch { e ->
                    _uiState.value = BatteryUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = BatteryUiState(batteryInfo = info)
                }
        }
    }

    fun refresh() {
        loadBatteryInfo()
    }
}
