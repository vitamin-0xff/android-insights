package com.example.phone_checker.ui.features.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.ScreenHealthInfo
import com.example.phone_checker.domain.usecase.GetScreenHealthInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenHealthUiState(
    val screenHealthInfo: ScreenHealthInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ScreenHealthViewModel @Inject constructor(
    private val getScreenHealthInfoUseCase: GetScreenHealthInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenHealthUiState(isLoading = true))
    val uiState: StateFlow<ScreenHealthUiState> = _uiState.asStateFlow()

    init {
        loadScreenHealthInfo()
    }

    private fun loadScreenHealthInfo() {
        viewModelScope.launch {
            _uiState.value = ScreenHealthUiState(isLoading = true)
            getScreenHealthInfoUseCase()
                .catch { e ->
                    _uiState.value = ScreenHealthUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = ScreenHealthUiState(screenHealthInfo = info)
                }
        }
    }

    fun refresh() {
        loadScreenHealthInfo()
    }
}
