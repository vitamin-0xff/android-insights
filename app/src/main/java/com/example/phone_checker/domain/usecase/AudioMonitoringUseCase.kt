package com.example.phone_checker.domain.usecase

import com.example.phone_checker.data.monitors.AudioMonitor
import com.example.phone_checker.data.monitors.AudioDeviceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartAudioMonitoringUseCase @Inject constructor(
    private val audioMonitor: AudioMonitor
) {
    operator fun invoke() {
        audioMonitor.startMonitoring()
    }
}

class StopAudioMonitoringUseCase @Inject constructor(
    private val audioMonitor: AudioMonitor
) {
    operator fun invoke() {
        audioMonitor.stopMonitoring()
    }
}

class ObserveAudioDeviceStateUseCase @Inject constructor(
    private val audioMonitor: AudioMonitor
) {
    operator fun invoke(): Flow<AudioDeviceState> = audioMonitor.audioDeviceState
}
