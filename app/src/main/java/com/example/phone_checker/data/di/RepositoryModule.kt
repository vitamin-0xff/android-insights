package com.example.phone_checker.data.di

import com.example.phone_checker.data.repository.BatteryRepository
import com.example.phone_checker.data.repository.BatteryRepositoryImpl
import com.example.phone_checker.data.repository.ThermalRepository
import com.example.phone_checker.data.repository.ThermalRepositoryImpl
import com.example.phone_checker.data.repository.StorageRepository
import com.example.phone_checker.data.repository.StorageRepositoryImpl
import com.example.phone_checker.data.repository.PerformanceRepository
import com.example.phone_checker.data.repository.PerformanceRepositoryImpl
import com.example.phone_checker.data.repository.NetworkRepository
import com.example.phone_checker.data.repository.NetworkRepositoryImpl
import com.example.phone_checker.data.repository.InsightsRepository
import com.example.phone_checker.data.repository.InsightsRepositoryImpl
import com.example.phone_checker.data.repository.ScreenHealthRepository
import com.example.phone_checker.data.repository.ScreenHealthRepositoryImpl
import com.example.phone_checker.data.repository.AppBehaviorRepository
import com.example.phone_checker.data.repository.AppBehaviorRepositoryImpl
import com.example.phone_checker.data.repository.SensorsRepository
import com.example.phone_checker.data.repository.SensorsRepositoryImpl
import com.example.phone_checker.data.repository.AudioRepository
import com.example.phone_checker.data.repository.AudioRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBatteryRepository(
        batteryRepositoryImpl: BatteryRepositoryImpl
    ): BatteryRepository

    @Binds
    @Singleton
    abstract fun bindThermalRepository(
        thermalRepositoryImpl: ThermalRepositoryImpl
    ): ThermalRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository

    @Binds
    @Singleton
    abstract fun bindPerformanceRepository(
        performanceRepositoryImpl: PerformanceRepositoryImpl
    ): PerformanceRepository

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        networkRepositoryImpl: NetworkRepositoryImpl
    ): NetworkRepository

    @Binds
    @Singleton
    abstract fun bindInsightsRepository(
        insightsRepositoryImpl: InsightsRepositoryImpl
    ): InsightsRepository

    @Binds
    @Singleton
    abstract fun bindScreenHealthRepository(
        screenHealthRepositoryImpl: ScreenHealthRepositoryImpl
    ): ScreenHealthRepository

    @Binds
    @Singleton
    abstract fun bindAppBehaviorRepository(
        appBehaviorRepositoryImpl: AppBehaviorRepositoryImpl
    ): AppBehaviorRepository

    @Binds
    @Singleton
    abstract fun bindSensorsRepository(
        sensorsRepositoryImpl: SensorsRepositoryImpl
    ): SensorsRepository

    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository
}
