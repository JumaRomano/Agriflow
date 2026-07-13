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
import com.agriflow.app.core.util.FileHelper
import javax.inject.Inject

@HiltViewModel
class FarmerUpgradeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository,
    private val walletRepository: WalletRepository,
    private val fileHelper: FileHelper
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
                            pendingBalance = pending,
                            businessProfile = result.data.businessProfile
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
            is RoleUpgradeAction.RoleSelected -> {}
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
                uploadBusinessProfile(action.uri)
            }
            RoleUpgradeAction.SubmitClicked -> submitUpgrade()
            RoleUpgradeAction.SwitchToActiveRole -> switchToActiveRole()
        }
    }

    private fun uploadBusinessProfile(uri: android.net.Uri) {
        _state.update { it.copy(isUploadingImage = true) }
        viewModelScope.launch {
            val tempFile = fileHelper.uriToFile(uri)
            if (tempFile == null) {
                _state.update { it.copy(isUploadingImage = false) }
                _events.send(RoleUpgradeEvent.ShowError("Failed to process image file."))
                return@launch
            }

            val part = fileHelper.fileToMultipartPart(tempFile)
            val uploadResult = authRepository.uploadProfileImage(part)
            
            // Clean up cache file immediately
            tempFile.delete()

            _state.update { it.copy(isUploadingImage = false) }

            when (uploadResult) {
                is Result.Success -> {
                    val url = uploadResult.data.url ?: uploadResult.data.imageUrl
                    if (!url.isNullOrBlank()) {
                        _state.update { it.copy(businessProfile = url) }
                    } else {
                        _events.send(RoleUpgradeEvent.ShowError("Failed to upload image. Server returned empty URL."))
                    }
                }
                is Result.Error -> {
                    _events.send(RoleUpgradeEvent.ShowError("Failed to upload image: ${uploadResult.error.name}"))
                }
            }
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
                    businessPhone = currentState.businessPhone,
                    county = currentState.businessCounty,
                    businessProfile = currentState.businessProfile
                )
            ) {
                is Result.Success -> {
                    val businessDetails = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            businessName = businessDetails.businessName.orEmpty(),
                            businessEmail = businessDetails.businessEmail.orEmpty(),
                            businessPhone = businessDetails.businessPhone.orEmpty(),
                            businessCounty = businessDetails.county.orEmpty(),
                            approvalStatus = businessDetails.approvalStatus,
                            businessProfile = businessDetails.businessProfile
                        )
                    }
                    tokenRepository.saveRegisteredBusinessRole(UserRole.FARMER)
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
            state.businessCounty.isBlank() -> "Farm County is required"
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
