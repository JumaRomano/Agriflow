package com.agriflow.app.features.ratings

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RatingsModule {

    @Binds
    @Singleton
    abstract fun bindRatingsRepository(
        ratingsRepositoryImpl: RatingsRepositoryImpl
    ): RatingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideRatingsApi(retrofit: Retrofit): RatingsApi {
            return retrofit.create(RatingsApi::class.java)
        }
    }
}
