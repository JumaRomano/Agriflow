/**
 * Dagger Hilt dependency injection module providing component bindings and providers.
 */
package com.agriflow.app.features.orders

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrdersModule {

    @Binds
    @Singleton
    abstract fun bindOrdersRepository(
        ordersRepositoryImpl: OrdersRepositoryImpl
    ): OrdersRepository

    companion object {
        @Provides
        @Singleton
        fun provideOrdersApi(retrofit: Retrofit): OrdersApi {
            return retrofit.create(OrdersApi::class.java)
        }
    }
}
