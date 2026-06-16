/**
 * Dagger Hilt dependency injection module providing component bindings and providers.
 */
package com.agriflow.app.features.auth

import com.agriflow.app.core.security.EncryptedTokenRepositoryImpl
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.auth.AuthApi
import com.agriflow.app.features.auth.AuthRepositoryImpl
import com.agriflow.app.features.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTokenRepository(
        encryptedTokenRepositoryImpl: EncryptedTokenRepositoryImpl
    ): TokenRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi {
            return retrofit.create(AuthApi::class.java)
        }
    }
}
