package com.agriflow.app.core.network

import com.agriflow.app.BuildConfig
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.auth.AuthResponseDto
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenRepository: TokenRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenRepository.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            return null
        }

        synchronized(this) {
            val currentAccessToken = tokenRepository.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

            val finalAccessToken = if (currentAccessToken != requestToken && !currentAccessToken.isNullOrBlank()) {
                currentAccessToken
            } else {
                val newAccessToken = refreshAccessToken(refreshToken)
                if (newAccessToken.isNullOrBlank()) {
                    tokenRepository.clearTokens()
                    return null
                }
                newAccessToken
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer $finalAccessToken")
                .build()
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val jsonBody = Gson().toJson(mapOf("refreshToken" to refreshToken))
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val baseUrl = BuildConfig.BASE_URL
            val refreshUrl = if (baseUrl.endsWith("/")) {
                "${baseUrl}auth/refresh"
            } else {
                "$baseUrl/auth/refresh"
            }

            val request = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .build()

            val client = OkHttpClient.Builder()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    val authResponse = Gson().fromJson(bodyString, AuthResponseDto::class.java)
                    val newAccess = authResponse?.accessToken
                    val newRefresh = authResponse?.refreshToken ?: refreshToken

                    if (!newAccess.isNullOrBlank()) {
                        tokenRepository.saveTokens(
                            accessToken = newAccess,
                            refreshToken = newRefresh,
                            email = null,
                            role = tokenRepository.getActiveRole()
                        )
                        return newAccess
                    }
                }
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
