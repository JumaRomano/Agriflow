package com.agriflow.app.features.profile.Enterprise

import com.agriflow.app.features.profile.roleUpgrade.RoleUpgradeState
import com.agriflow.app.features.profile.roleUpgrade.RoleUpgradeAction
import com.agriflow.app.features.profile.roleUpgrade.RoleUpgradeEvent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthRepository
import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.core.security.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterpriseUpgradeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoleUpgradeState(selectedRole = UserRole.SUPPLIER))
    val state = _state.asStateFlow()

    private val _events = Channel<RoleUpgradeEvent>()
    val events = _events.receiveAsFlow()

    init {
        fetchBusinessDetails()
    }

    private fun fetchBusinessDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = authRepository.getBusinessDetails()) {
                is Result.Success -> {
                    tokenRepository.saveRegisteredBusinessRole(UserRole.SUPPLIER)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            businessName = result.data.businessName.orEmpty(),
                            businessEmail = result.data.businessEmail.orEmpty(),
                            businessPhone = result.data.businessPhone.orEmpty(),
                            approvalStatus = result.data.approvalStatus,
                            walletBalance = result.data.walletBalance ?: 0.0
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onAction(action: RoleUpgradeAction) {
        when (action) {
            is RoleUpgradeAction.BusinessNameChanged -> {
                _state.update { it.copy(businessName = action.name, errorMessage = null) }
            }
            is RoleUpgradeAction.BusinessEmailChanged -> {
                _state.update { it.copy(businessEmail = action.email, errorMessage = null) }
            }
            is RoleUpgradeAction.BusinessPhoneChanged -> {
                _state.update { it.copy(businessPhone = action.phone, errorMessage = null) }
            }
            RoleUpgradeAction.SubmitClicked -> submitUpgrade()
            RoleUpgradeAction.SwitchToActiveRole -> switchToActiveRole()
            else -> {}
        }
    }

    private fun submitUpgrade() {
        val currentState = state.value
        val validationMessage = validateForm(currentState)
        if (validationMessage != null) {
            _state.update { it.copy(errorMessage = validationMessage) }
            viewModelScope.launch {
                _events.send(RoleUpgradeEvent.ShowError(validationMessage))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = authRepository.upgradeRole(
                    role = UserRole.SUPPLIER,
                    businessName = currentState.businessName,
                    businessEmail = currentState.businessEmail,
                    businessPhone = currentState.businessPhone
                )
            ) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    tokenRepository.saveRegisteredBusinessRole(UserRole.SUPPLIER)
                    _events.send(RoleUpgradeEvent.UpgradeSuccess)
                }
                is Result.Error -> {
                    val message = "Upgrade to Enterprise failed. Please try again."
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                    _events.send(RoleUpgradeEvent.ShowError(message))
                }
            }
        }
    }

    private fun validateForm(state: RoleUpgradeState): String? {
        return when {
            state.businessName.isBlank() -> "Enterprise Name is required"
            state.businessEmail.isBlank() -> "Enterprise Email is required"
            !state.businessEmail.contains("@") -> "Enter a valid enterprise email address"
            state.businessPhone.isBlank() -> "Enterprise Phone Number is required"
            else -> null
        }
    }
    private fun switchToActiveRole() {
        tokenRepository.setActiveRole(UserRole.SUPPLIER)
        viewModelScope.launch {
            _events.send(RoleUpgradeEvent.UpgradeSuccess)
        }
    }
}
