package com.example.phone_checker.ui.features.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phone_checker.data.monitors.AudioDevice
import com.example.phone_checker.data.repository.AudioHealthInfo
import com.example.phone_checker.domain.usecase.GetAudioHealthInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioUiState(
    val audioInfo: AudioHealthInfo? = null,
    val inputDevices: List<AudioDevice> = emptyList(),
    val outputDevices: List<AudioDevice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val getAudioHealthInfoUseCase: GetAudioHealthInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState(isLoading = true))
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    init {
        loadAudioInfo()
    }

    private fun loadAudioInfo() {
        viewModelScope.launch {
            _uiState.value = AudioUiState(isLoading = true)
            getAudioHealthInfoUseCase()
                .catch { e ->
                    _uiState.value = AudioUiState(
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { info ->
                    _uiState.value = AudioUiState(
                        audioInfo = info,
                        inputDevices = info.inputDevices,
                        outputDevices = info.outputDevices
                    )
                }
        }
    }

    fun refresh() {
        loadAudioInfo()
    }
}
