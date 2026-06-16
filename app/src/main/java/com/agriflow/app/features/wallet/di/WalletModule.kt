package com.agriflow.app.features.wallet.di

import com.agriflow.app.features.wallet.WalletApi
import com.agriflow.app.features.wallet.WalletRepository
import com.agriflow.app.features.wallet.WalletRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    companion object {
        @Provides
        @Singleton
        fun provideWalletApi(retrofit: Retrofit): WalletApi {
            return retrofit.create(WalletApi::class.java)
        }
    }
}
