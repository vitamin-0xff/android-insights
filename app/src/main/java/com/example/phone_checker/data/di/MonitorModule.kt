package com.example.phone_checker.data.di

import android.content.Context
import com.example.phone_checker.data.monitors.AudioMonitor
import com.example.phone_checker.data.monitors.BatteryMonitor
import com.example.phone_checker.data.monitors.DisplayMonitor
import com.example.phone_checker.data.monitors.MonitorManager
import com.example.phone_checker.data.monitors.NetworkMonitor
import com.example.phone_checker.data.monitors.ThermalMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonitorModule {

    @Provides
    @Singleton
    fun provideBatteryMonitor(
        @ApplicationContext context: Context
    ): BatteryMonitor = BatteryMonitor(context)

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideThermalMonitor(
        @ApplicationContext context: Context
    ): ThermalMonitor = ThermalMonitor(context)

    @Provides
    @Singleton
    fun provideAudioMonitor(
        @ApplicationContext context: Context
    ): AudioMonitor = AudioMonitor(context)

    @Provides
    @Singleton
    fun provideDisplayMonitor(
        @ApplicationContext context: Context
    ): DisplayMonitor = DisplayMonitor(context)

    @Provides
    @Singleton
    fun provideMonitorManager(
        batteryMonitor: BatteryMonitor,
        networkMonitor: NetworkMonitor,
        thermalMonitor: ThermalMonitor,
        audioMonitor: AudioMonitor,
        displayMonitor: DisplayMonitor
    ): MonitorManager = MonitorManager(
        batteryMonitor,
        networkMonitor,
        thermalMonitor,
        audioMonitor,
        displayMonitor
    )
}
