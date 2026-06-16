/**
 * Dagger Hilt dependency injection module providing component bindings and providers.
 */
package com.agriflow.app.features.cart.di

import com.agriflow.app.features.cart.data.CartApi
import com.agriflow.app.features.cart.data.CartRepositoryImpl
import com.agriflow.app.features.cart.domain.CartRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CartModule {

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository

    companion object {
        @Provides
        @Singleton
        fun provideCartApi(retrofit: Retrofit): CartApi {
            return retrofit.create(CartApi::class.java)
        }
    }
}
