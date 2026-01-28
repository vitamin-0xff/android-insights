package com.example.phone_checker.ui.features.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.repository.StorageInfo
import com.example.phone_checker.domain.usecase.GetStorageInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StorageUiState(
    val storageInfo: StorageInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val getStorageInfoUseCase: GetStorageInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageUiState(isLoading = true))
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    init {
        loadStorageInfo()
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            _uiState.value = StorageUiState(isLoading = true)
            getStorageInfoUseCase()
                .catch { e ->
                    _uiState.value = StorageUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = StorageUiState(storageInfo = info)
                }
        }
    }

    fun refresh() {
        loadStorageInfo()
    }
}
