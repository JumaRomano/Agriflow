/**
 * ViewModel managing the business logic and UI state for the Navigation feature.
 */
package com.agriflow.app.core.navigation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NavigationState(
    val isLoading: Boolean = true,
    val startDestination: Route = Route.Splash
)

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // Expose the clean UserRole reactively to the NavHost shell.
    val userRole: StateFlow<UserRole> = tokenRepository.getUserRoleFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserRole.UNKNOWN
        )

    init {
        loadNavigationState()
    }

    private fun loadNavigationState() {
        viewModelScope.launch {
            val isLoggedIn = !tokenRepository.getAccessToken().isNullOrBlank()
            val actualRole = tokenRepository.getActualRole()
            
            Log.d("RouteDebug", "Loaded Role: $actualRole")

            val startDest = when {
                !isLoggedIn -> Route.AuthGraph
                actualRole == UserRole.STAFF -> Route.StaffGraph
                else -> Route.MainGraph
            }

            _navigationState.value = NavigationState(
                isLoading = false,
                startDestination = startDest
            )
        }
    }

    fun isUserLoggedIn(): Boolean {
        return !tokenRepository.getAccessToken().isNullOrBlank()
    }

    fun isUserStaff(): Boolean {
        return tokenRepository.getActualRole() == UserRole.STAFF
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
