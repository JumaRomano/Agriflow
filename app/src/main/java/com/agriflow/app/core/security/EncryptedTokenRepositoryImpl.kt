/**
 * Repository implementation of [EncryptedTokenRepository] managing remote and local data operations.
 */
package com.agriflow.app.core.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.agriflow.app.features.auth.User
import com.agriflow.app.features.auth.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedTokenRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : TokenRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private fun createSharedPreferences(context: Context, masterKey: MasterKey) =
        EncryptedSharedPreferences.create(
            context,
            TOKEN_PREFERENCES_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    private val sharedPreferences = try {
        createSharedPreferences(context, masterKey)
    } catch (e: Exception) {
        try {
            context.deleteSharedPreferences(TOKEN_PREFERENCES_NAME)
        } catch (ignored: Exception) {}
        createSharedPreferences(context, masterKey)
    }

    private var actualRole: UserRole = UserRole.UNKNOWN
    private val _userRoleFlow = MutableStateFlow<UserRole>(UserRole.UNKNOWN)
    private val _userFlow = MutableStateFlow<User?>(null)

    init {
        // Initialize state reactively on repository launch using current stored tokens
        val initialUser = decodeUserFromToken(getAccessToken())
        actualRole = initialUser?.role ?: UserRole.UNKNOWN
        val activeRole = determineActiveRole(actualRole)
        _userFlow.value = initialUser?.copy(role = activeRole)
        _userRoleFlow.value = activeRole
    }

    private fun determineActiveRole(actual: UserRole): UserRole {
        val savedRoleStr = sharedPreferences.getString(KEY_ACTIVE_ROLE, null) ?: return actual
        val savedRole = try {
            UserRole.valueOf(savedRoleStr)
        } catch (e: Exception) {
            actual
        }
        
        // A user can switch to a business role only if their actual role matches it
        return when {
            actual == UserRole.FARMER && (savedRole == UserRole.FARMER || savedRole == UserRole.BUYER) -> savedRole
            actual == UserRole.SUPPLIER && (savedRole == UserRole.SUPPLIER || savedRole == UserRole.BUYER) -> savedRole
            else -> actual
        }
    }

    override fun saveTokens(accessToken: String, refreshToken: String, email: String?) {
        val editor = sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
        if (email != null) {
            editor.putString(KEY_USER_EMAIL, email)
        }
        editor.apply()

        val user = decodeUserFromToken(accessToken)
        actualRole = user?.role ?: UserRole.UNKNOWN
        
        if (actualRole == UserRole.FARMER || actualRole == UserRole.SUPPLIER) {
            saveRegisteredBusinessRole(actualRole)
        }
        
        val activeRole = determineActiveRole(actualRole)
        _userFlow.value = user?.copy(role = activeRole)
        _userRoleFlow.value = activeRole
    }

    override fun getAccessToken(): String? =
        sharedPreferences.getString(KEY_ACCESS_TOKEN, null)?.takeIf(String::isNotBlank)

    override fun getRefreshToken(): String? =
        sharedPreferences.getString(KEY_REFRESH_TOKEN, null)?.takeIf(String::isNotBlank)

    override fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ACTIVE_ROLE)
            .remove(KEY_REGISTERED_BUSINESS_ROLE)
            .remove(KEY_USER_EMAIL)
            .apply()
        actualRole = UserRole.UNKNOWN
        _userFlow.value = null
        _userRoleFlow.value = UserRole.UNKNOWN
    }

    override fun getUserRoleFlow(): Flow<UserRole> = _userRoleFlow.asStateFlow()

    override fun getUserFlow(): Flow<User?> = _userFlow.asStateFlow()

    override fun getActualRole(): UserRole = actualRole

    override fun getActiveRole(): UserRole = _userRoleFlow.value

    override fun setActiveRole(role: UserRole) {
        sharedPreferences.edit()
            .putString(KEY_ACTIVE_ROLE, role.name)
            .apply()
        
        val user = decodeUserFromToken(getAccessToken())
        val validatedRole = determineActiveRole(actualRole)
        _userFlow.value = user?.copy(role = validatedRole)
        _userRoleFlow.value = validatedRole
    }

    override fun getRegisteredBusinessRole(): UserRole {
        val roleStr = sharedPreferences.getString(KEY_REGISTERED_BUSINESS_ROLE, null) ?: return UserRole.UNKNOWN
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.UNKNOWN
        }
    }

    override fun saveRegisteredBusinessRole(role: UserRole) {
        sharedPreferences.edit()
            .putString(KEY_REGISTERED_BUSINESS_ROLE, role.name)
            .apply()
    }

    private fun decodeUserFromToken(token: String?): User? {
        if (token.isNullOrBlank()) return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val jsonString = String(decodedBytes, StandardCharsets.UTF_8)
            android.util.Log.d("AgriflowJWT", "Decoded JWT payload: $jsonString")
            val jsonObject = JSONObject(jsonString)
            
            val userId = jsonObject.optString("sub") ?: ""
            val username = jsonObject.optString("username").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("name") ?: "User"
            val email = jsonObject.optString("email").takeIf { it.isNotBlank() }
                ?: sharedPreferences.getString(KEY_USER_EMAIL, null)?.takeIf { it.isNotBlank() }
                ?: jsonObject.optString("sub").takeIf { it.contains("@") }
                ?: jsonObject.optString("username").takeIf { it.contains("@") }
                ?: jsonObject.optString("userEmail").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("user_email").takeIf { it.isNotBlank() }
                ?: ""
            val phone = jsonObject.optString("phone").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("phoneNumber")
            val roleStr = jsonObject.optString("role").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("roles") ?: jsonObject.optString("authorities") ?: ""
            
            val role = when (roleStr.trim().removePrefix("ROLE_").removePrefix("[").removeSuffix("]").uppercase()) {
                "FARMER" -> UserRole.FARMER
                "SUPPLIER", "SELLER" -> UserRole.SUPPLIER
                "BUYER" -> UserRole.BUYER
                "ADMIN" -> UserRole.ADMIN
                else -> UserRole.UNKNOWN
            }

            User(
                id = userId,
                username = username,
                email = email,
                phoneNumber = phone,
                role = role
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private companion object {
        const val TOKEN_PREFERENCES_NAME = "agriflow_secure_tokens"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ACTIVE_ROLE = "active_role"
        const val KEY_REGISTERED_BUSINESS_ROLE = "registered_business_role"
        const val KEY_USER_EMAIL = "user_email"
    }
}
