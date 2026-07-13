/**
 * ViewModel managing the business logic and UI state for the RoleUpgrade feature.
 */
package com.agriflow.app.features.profile.roleUpgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthRepository
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
class RoleUpgradeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoleUpgradeState())
    val state = _state.asStateFlow()

    private val _events = Channel<RoleUpgradeEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: RoleUpgradeAction) {
        when (action) {
            is RoleUpgradeAction.RoleSelected -> {
                _state.update { it.copy(selectedRole = action.role, errorMessage = null) }
            }
            is RoleUpgradeAction.BusinessNameChanged -> {
                _state.update { it.copy(businessName = action.name, errorMessage = null) }
            }
            is RoleUpgradeAction.BusinessEmailChanged -> {
                _state.update { it.copy(businessEmail = action.email, errorMessage = null) }
            }
            is RoleUpgradeAction.BusinessPhoneChanged -> {
                _state.update { it.copy(businessPhone = action.phone, errorMessage = null) }
            }
            is RoleUpgradeAction.BusinessCountyChanged -> {
                _state.update { it.copy(businessCounty = action.county, errorMessage = null) }
            }
            is RoleUpgradeAction.BusinessProfileSelected -> {
                _state.update { it.copy(businessProfile = action.uri.toString(), errorMessage = null) }
            }
            RoleUpgradeAction.SubmitClicked -> submitUpgrade()
            RoleUpgradeAction.SwitchToActiveRole -> {}
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
                    role = currentState.selectedRole,
                    businessName = currentState.businessName,
                    businessEmail = currentState.businessEmail,
                    businessPhone = currentState.businessPhone,
                    county = currentState.businessCounty,
                    businessProfile = currentState.businessProfile
                )
            ) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(RoleUpgradeEvent.UpgradeSuccess)
                }
                is Result.Error -> {
                    val message = "Upgrade failed. Please try again."
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                    _events.send(RoleUpgradeEvent.ShowError(message))
                }
            }
        }
    }

    private fun validateForm(state: RoleUpgradeState): String? {
        return when {
            state.selectedRole != UserRole.FARMER && state.selectedRole != UserRole.SUPPLIER -> {
                "Please select a valid role to upgrade to"
            }
            state.businessName.isBlank() -> "Business name is required"
            state.businessEmail.isBlank() -> "Business email is required"
            !state.businessEmail.contains("@") -> "Enter a valid business email address"
            state.businessPhone.isBlank() -> "Business phone number is required"
            state.businessCounty.isBlank() -> "County is required"
            else -> null
        }
    }
}