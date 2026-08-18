package com.agriflow.app.core.di

import com.agriflow.app.core.util.DefaultTimeProvider
import com.agriflow.app.core.util.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    @Singleton
    abstract fun bindTimeProvider(
        defaultTimeProvider: DefaultTimeProvider
    ): TimeProvider
}
