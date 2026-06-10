package com.agriflow.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            tokenRepository.getUserFlow().collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            name = user.username,
                            email = user.email,
                            role = user.role
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnLogoutClicked -> logout()
            ProfileAction.OnSwitchAccountClicked -> {
                viewModelScope.launch {
                    val activeRole = tokenRepository.getActiveRole()
                    if (activeRole == UserRole.FARMER || activeRole == UserRole.SUPPLIER) {
                        tokenRepository.setActiveRole(UserRole.BUYER)
                    } else {
                        _events.send(ProfileEvent.NavigateToRoleUpgrade(state.value.role))
                    }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            tokenRepository.clearTokens()
            _state.update { it.copy(isLoading = false) }
            _events.send(ProfileEvent.MapsToLogin)
        }
    }
}
