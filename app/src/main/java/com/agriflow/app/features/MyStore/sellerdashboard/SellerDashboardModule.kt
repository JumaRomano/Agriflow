package com.agriflow.app.features.MyStore.sellerdashboard

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SellerDashboardModule {

    @Binds
    @Singleton
    abstract fun bindSellerDashboardRepository(
        sellerDashboardRepositoryImpl: SellerDashboardRepositoryImpl
    ): SellerDashboardRepository

    companion object {
        @Provides
        @Singleton
        fun provideSellerDashboardApi(retrofit: Retrofit): SellerDashboardApi {
            return retrofit.create(SellerDashboardApi::class.java)
        }
    }
}
