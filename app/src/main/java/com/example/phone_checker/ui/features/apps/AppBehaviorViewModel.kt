package com.example.phone_checker.ui.features.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.AppBehaviorInfo
import com.example.phone_checker.domain.usecase.GetAppBehaviorInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppBehaviorUiState(
    val appBehaviorInfo: AppBehaviorInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AppBehaviorViewModel @Inject constructor(
    private val getAppBehaviorInfoUseCase: GetAppBehaviorInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppBehaviorUiState(isLoading = true))
    val uiState: StateFlow<AppBehaviorUiState> = _uiState.asStateFlow()

    init {
        loadAppBehaviorInfo()
    }

    private fun loadAppBehaviorInfo() {
        viewModelScope.launch {
            _uiState.value = AppBehaviorUiState(isLoading = true)
            getAppBehaviorInfoUseCase()
                .catch { e ->
                    _uiState.value = AppBehaviorUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = AppBehaviorUiState(appBehaviorInfo = info)
                }
        }
    }

    fun refresh() {
        loadAppBehaviorInfo()
    }
}
