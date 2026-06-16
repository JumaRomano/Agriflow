/**
 * Dagger Hilt dependency injection module providing component bindings and providers.
 */
package com.agriflow.app.features.marketplace

import com.agriflow.app.features.marketplace.MarketplaceApi
import com.agriflow.app.features.marketplace.OfflineFirstMarketplaceRepositoryImpl
import com.agriflow.app.features.marketplace.MarketplaceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MarketplaceModule {

    @Binds
    @Singleton
    abstract fun bindMarketplaceRepository(
        offlineFirstMarketplaceRepositoryImpl: OfflineFirstMarketplaceRepositoryImpl
    ): MarketplaceRepository

    companion object {
        @Provides
        @Singleton
        fun provideMarketplaceApi(retrofit: Retrofit): MarketplaceApi {
            return retrofit.create(MarketplaceApi::class.java)
        }
    }
}
