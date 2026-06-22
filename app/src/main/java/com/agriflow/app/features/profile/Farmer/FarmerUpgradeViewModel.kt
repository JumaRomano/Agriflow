/**
 * ViewModel managing the business logic and UI state for the FarmerUpgrade feature.
 */
package com.agriflow.app.features.profile.Farmer

import com.agriflow.app.features.profile.roleUpgrade.RoleUpgradeState
import com.agriflow.app.features.profile.roleUpgrade.RoleUpgradeAction
import com.agriflow.app.features.profile.roleUpgrade.RoleUpgradeEvent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthRepository
import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmerUpgradeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoleUpgradeState(selectedRole = UserRole.FARMER))
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
                    tokenRepository.saveRegisteredBusinessRole(UserRole.FARMER)
                    val isApproved = result.data.approvalStatus == "APPROVED"
                    var available = 0.0
                    var pending = 0.0
                    if (isApproved) {
                        when (val walletResult = walletRepository.getWallet()) {
                            is Result.Success -> {
                                available = walletResult.data.availableBalance ?: 0.0
                                pending = walletResult.data.pendingBalance ?: 0.0
                            }
                            is Result.Error -> {
                                available = result.data.walletBalance ?: 0.0
                                pending = result.data.pendingBalance ?: 0.0
                            }
                        }
                    } else {
                        available = result.data.walletBalance ?: 0.0
                        pending = result.data.pendingBalance ?: 0.0
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            businessName = result.data.businessName.orEmpty(),
                            businessEmail = result.data.businessEmail.orEmpty(),
                            businessPhone = result.data.businessPhone.orEmpty(),
                            approvalStatus = result.data.approvalStatus,
                            walletBalance = available,
                            availableBalance = available,
                            pendingBalance = pending
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
                    role = UserRole.FARMER,
                    businessName = currentState.businessName,
                    businessEmail = currentState.businessEmail,
                    businessPhone = currentState.businessPhone
                )
            ) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    tokenRepository.saveRegisteredBusinessRole(UserRole.FARMER)
                    _events.send(RoleUpgradeEvent.UpgradeSuccess)
                }
                is Result.Error -> {
                    val message = "Upgrade to Farmer failed. Please try again."
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                    _events.send(RoleUpgradeEvent.ShowError(message))
                }
            }
        }
    }

    private fun validateForm(state: RoleUpgradeState): String? {
        return when {
            state.businessName.isBlank() -> "Farm Name is required"
            state.businessEmail.isBlank() -> "Farm Email is required"
            !state.businessEmail.contains("@") -> "Enter a valid farm email address"
            state.businessPhone.isBlank() -> "Farm Phone Number is required"
            else -> null
        }
    }
    private fun switchToActiveRole() {
        tokenRepository.setActiveRole(UserRole.FARMER)
        viewModelScope.launch {
            _events.send(RoleUpgradeEvent.UpgradeSuccess)
        }
    }
}
