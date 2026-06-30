package com.agriflow.app.features.staff.auth

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StaffModule {

    @Binds
    @Singleton
    abstract fun bindStaffAuthRepository(
        staffAuthRepositoryImpl: StaffAuthRepositoryImpl
    ): StaffAuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideStaffAuthApi(retrofit: Retrofit): StaffAuthApi {
            return retrofit.create(StaffAuthApi::class.java)
        }
    }
}
