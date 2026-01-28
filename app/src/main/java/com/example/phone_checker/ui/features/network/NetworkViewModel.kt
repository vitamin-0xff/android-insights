package com.example.phone_checker.ui.features.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.NetworkInfo
import com.example.phone_checker.domain.usecase.GetNetworkInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NetworkUiState(
    val networkInfo: NetworkInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val getNetworkInfoUseCase: GetNetworkInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkUiState(isLoading = true))
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    init {
        loadNetworkInfo()
    }

    private fun loadNetworkInfo() {
        viewModelScope.launch {
            _uiState.value = NetworkUiState(isLoading = true)
            getNetworkInfoUseCase()
                .catch { e ->
                    _uiState.value = NetworkUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = NetworkUiState(networkInfo = info)
                }
        }
    }

    fun refresh() {
        loadNetworkInfo()
    }
}
