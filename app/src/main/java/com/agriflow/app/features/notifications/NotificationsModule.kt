package com.agriflow.app.features.notifications

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(
        notificationsRepositoryImpl: NotificationsRepositoryImpl
    ): NotificationsRepository

    companion object {
        @Provides
        @Singleton
        fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi {
            return retrofit.create(NotificationsApi::class.java)
        }
    }
}
