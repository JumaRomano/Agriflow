package com.agriflow.app.core.network

import com.agriflow.app.core.security.TokenRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenRepository: TokenRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        // Interceptors are synchronous, so TokenRepository exposes a synchronous token read.
        val accessToken = tokenRepository.getAccessToken()

        if (
            accessToken.isNullOrBlank() ||
            // If a request already set Authorization manually, do not overwrite it.
            originalRequest.header(AUTHORIZATION_HEADER) != null ||
            // Login/register are public endpoints; sending an old token here can cause confusing bugs.
            originalRequest.url.encodedPath in PUBLIC_AUTH_PATHS
        ) {
            return chain.proceed(originalRequest)
        }

        // Every protected API call automatically receives the Bearer token here.
        val authenticatedRequest = originalRequest.newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer"
        val PUBLIC_AUTH_PATHS = setOf(
            "/api/auth/login",
            "/api/auth/register"
        )
    }
}
