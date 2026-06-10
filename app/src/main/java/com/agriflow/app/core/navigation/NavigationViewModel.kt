package com.agriflow.app.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val tokenRepository: TokenRepository
) : ViewModel() {

    // Expose the clean UserRole reactively to the NavHost shell.
    val userRole: StateFlow<UserRole> = tokenRepository.getUserRoleFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserRole.UNKNOWN
        )

    fun isUserLoggedIn(): Boolean {
        return !tokenRepository.getAccessToken().isNullOrBlank()
    }

    fun getUpgradeDestination(): Route {
        val actualRole = tokenRepository.getActualRole()
        val registeredRole = tokenRepository.getRegisteredBusinessRole()
        return when {
            actualRole == UserRole.FARMER || registeredRole == UserRole.FARMER -> Route.FarmerUpgrade
            actualRole == UserRole.SUPPLIER || registeredRole == UserRole.SUPPLIER -> Route.EnterpriseUpgrade
            else -> Route.RoleUpgrade
        }
    }
}
