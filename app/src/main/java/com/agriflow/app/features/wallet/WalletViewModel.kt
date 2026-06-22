package com.agriflow.app.features.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.Result
import com.agriflow.app.core.util.DataError
import com.agriflow.app.features.auth.AuthRepository
import com.agriflow.app.features.auth.otp.OtpType
import com.agriflow.app.features.payment.PaymentMethod
import com.agriflow.app.features.payment.PaymentMethodType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val tokenRepository: TokenRepository,
    private val authRepository: AuthRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val paymentSharedPreferences = context.getSharedPreferences("agriflow_payment_methods_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(WalletState())
    val state = _state.asStateFlow()

    private val _events = Channel<WalletEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeTransactions()
        loadWalletData()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            walletRepository.observeTransactions().collect { entities ->
                val txList = entities.map { entity ->
                    WalletTransaction(
                        id = entity.transactionId,
                        type = if (entity.type.equals("REVENUE", ignoreCase = true) || entity.type.equals("CREDIT", ignoreCase = true)) TransactionType.REVENUE else TransactionType.WITHDRAWAL,
                        amount = entity.amount,
                        description = entity.description.takeIf { it.isNotBlank() }
                            ?: entity.category.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }?.plus(" transaction")
                            ?: "Transaction",
                        timestamp = entity.timestamp
                    )
                }
                _state.update { it.copy(transactions = txList) }
            }
        }
    }

    fun onAction(action: WalletAction) {
        when (action) {
            WalletAction.RefreshWallet -> loadWalletData()
            is WalletAction.ShowWithdrawDialog -> {
                if (action.visible) {
                    val defaultMpesa = loadDefaultMpesaNumber()
                    _state.value = _state.value.copy(
                        isWithdrawDialogVisible = true,
                        withdrawAmount = "",
                        withdrawMpesaNumber = defaultMpesa ?: "",
                        withdrawAmountError = null,
                        withdrawMpesaNumberError = null,
                        isOtpSent = false,
                        otpCode = "",
                        otpError = null,
                        defaultMpesaNumber = defaultMpesa,
                        useDefaultMpesa = defaultMpesa != null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isWithdrawDialogVisible = false,
                        withdrawAmount = "",
                        withdrawMpesaNumber = "",
                        withdrawAmountError = null,
                        withdrawMpesaNumberError = null,
                        isOtpSent = false,
                        otpCode = "",
                        otpError = null,
                        defaultMpesaNumber = null,
                        useDefaultMpesa = false
                    )
                }
            }
            is WalletAction.WithdrawAmountChanged -> {
                _state.value = _state.value.copy(
                    withdrawAmount = action.amount,
                    withdrawAmountError = null
                )
            }
            is WalletAction.WithdrawMpesaNumberChanged -> {
                _state.value = _state.value.copy(
                    withdrawMpesaNumber = action.mpesaNumber,
                    withdrawMpesaNumberError = null
                )
            }
            WalletAction.SubmitWithdrawal -> submitWithdrawal()
            is WalletAction.OtpCodeChanged -> {
                _state.value = _state.value.copy(
                    otpCode = action.code,
                    otpError = null
                )
            }
            WalletAction.VerifyAndWithdraw -> verifyAndWithdraw()
            WalletAction.ResendOtp -> resendOtp()
            WalletAction.GoBackToWithdrawDetails -> {
                _state.value = _state.value.copy(
                    isOtpSent = false,
                    otpCode = "",
                    otpError = null
                )
            }
            is WalletAction.ToggleUseDefaultMpesa -> {
                val defaultNum = _state.value.defaultMpesaNumber
                _state.value = _state.value.copy(
                    useDefaultMpesa = action.use,
                    withdrawMpesaNumber = if (action.use) defaultNum ?: "" else "",
                    withdrawMpesaNumberError = null
                )
            }
            WalletAction.NavigateBack -> {
                viewModelScope.launch {
                    _events.send(WalletEvent.NavigateBack)
                }
            }
        }
    }

    private fun loadWalletData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = tokenRepository.getUserFlow().first()
            if (user == null) {
                _state.update { it.copy(isLoading = false) }
                _events.send(WalletEvent.ShowToast("User session not found."))
                return@launch
            }

            // Try to load the wallet
            var walletResult = walletRepository.getWallet()

            // If the wallet does not exist/error, try to create it automatically
            if (walletResult is Result.Error) {
                walletResult = walletRepository.createWallet(
                    ownerId = user.id,
                    ownerType = user.role.name,
                    currency = "KES"
                )
            }

            when (walletResult) {
                is Result.Success -> {
                    val wallet = walletResult.data
                    
                    // Fetch transactions (this updates Room DB in the background, firing the flow)
                    walletRepository.getTransactions()

                    _state.update {
                        it.copy(
                            balance = wallet.availableBalance ?: 0.0,
                            pendingBalance = wallet.pendingBalance ?: 0.0,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(WalletEvent.ShowToast("Failed to fetch wallet details."))
                }
            }
        }
    }

    private fun submitWithdrawal() {
        val currentState = _state.value
        val amount = currentState.withdrawAmount.toDoubleOrNull()
        val mpesaNumber = currentState.withdrawMpesaNumber.trim()

        var hasError = false
        var amountErr: String? = null
        var mpesaErr: String? = null

        if (amount == null || amount <= 0) {
            amountErr = "Enter a valid amount"
            hasError = true
        } else if (amount < 10.0) {
            amountErr = "Minimum withdrawal is KES 10.00"
            hasError = true
        } else if (amount > currentState.balance) {
            amountErr = "Insufficient funds"
            hasError = true
        }

        // Basic phone number format check: starts with 0 or + or 254, length between 9 and 13 digits
        val phoneRegex = "^(\\+?\\d{9,13})$".toRegex()
        if (mpesaNumber.isBlank()) {
            mpesaErr = "M-Pesa number is required"
            hasError = true
        } else if (!phoneRegex.matches(mpesaNumber)) {
            mpesaErr = "Enter a valid phone number"
            hasError = true
        }

        if (hasError) {
            _state.value = currentState.copy(
                withdrawAmountError = amountErr,
                withdrawMpesaNumberError = mpesaErr
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = tokenRepository.getUserFlow().first()
            if (user == null) {
                _state.value = _state.value.copy(isLoading = false)
                _events.send(WalletEvent.ShowToast("User session not found."))
                return@launch
            }

            when (val otpResult = authRepository.sendOtp(user.email, OtpType.WITHDRAWAL)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isOtpSent = true,
                        otpCode = "",
                        otpError = null
                    )
                    _events.send(WalletEvent.ShowToast("Verification OTP sent to ${user.email}"))
                }
                is Result.Error -> {
                    val message = otpResult.error.toMessage()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        otpError = message
                    )
                    _events.send(WalletEvent.ShowToast("Failed to send OTP: $message"))
                }
            }
        }
    }

    private fun verifyAndWithdraw() {
        val currentState = _state.value
        val otpCode = currentState.otpCode.trim()

        if (otpCode.isBlank()) {
            _state.value = currentState.copy(otpError = "OTP code is required")
            return
        }

        val amount = currentState.withdrawAmount.toDoubleOrNull()
        val mpesaNumber = currentState.withdrawMpesaNumber.trim()

        if (amount == null || amount <= 0 || mpesaNumber.isBlank()) {
            _state.value = currentState.copy(otpError = "Withdrawal details are invalid. Please restart.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = tokenRepository.getUserFlow().first()
            if (user == null) {
                _state.value = _state.value.copy(isLoading = false)
                _events.send(WalletEvent.ShowToast("User session not found."))
                return@launch
            }

            when (val verifyResult = authRepository.verifyOtp(user.email, otpCode, OtpType.WITHDRAWAL)) {
                is Result.Success -> {
                    // OTP verified, proceed with withdrawal
                    val result = walletRepository.withdrawFunds(
                        ownerId = user.id,
                        amount = amount,
                        phoneNumber = mpesaNumber,
                        accountName = user.username,
                        payoutMethod = "MPESA"
                    )
                    
                    when (result) {
                        is Result.Success -> {
                            _events.send(WalletEvent.ShowToast("Withdrawal of KES $amount processed successfully."))
                            _state.value = _state.value.copy(
                                isWithdrawDialogVisible = false,
                                isOtpSent = false,
                                otpCode = "",
                                otpError = null,
                                withdrawAmount = "",
                                withdrawMpesaNumber = ""
                            )
                            loadWalletData()
                        }
                        is Result.Error -> {
                            val errorMessage = result.error.toMessage()
                            _state.value = _state.value.copy(
                                isLoading = false,
                                otpError = errorMessage
                            )
                            _events.send(WalletEvent.ShowToast("Withdrawal failed: $errorMessage"))
                        }
                    }
                }
                is Result.Error -> {
                    val errorMessage = verifyResult.error.toMessage()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        otpError = errorMessage
                    )
                    _events.send(WalletEvent.ShowToast("Verification failed: $errorMessage"))
                }
            }
        }
    }

    private fun resendOtp() {
        val currentState = _state.value
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = tokenRepository.getUserFlow().first()
            if (user == null) {
                _state.value = _state.value.copy(isLoading = false)
                _events.send(WalletEvent.ShowToast("User session not found."))
                return@launch
            }

            when (val otpResult = authRepository.sendOtp(user.email, OtpType.WITHDRAWAL)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        otpCode = "",
                        otpError = null
                    )
                    _events.send(WalletEvent.ShowToast("A new OTP has been sent to ${user.email}"))
                }
                is Result.Error -> {
                    val message = otpResult.error.toMessage()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        otpError = message
                    )
                    _events.send(WalletEvent.ShowToast("Failed to resend OTP: $message"))
                }
            }
        }
    }

    private fun DataError.Network.toMessage(): String {
        return when (this) {
            DataError.Network.REQUEST_TIMEOUT -> "The request timed out. Check your connection and try again."
            DataError.Network.UNAUTHORIZED -> "Invalid OTP verification code or unauthorized session."
            DataError.Network.CONFLICT -> "Insufficient funds or invalid withdrawal details."
            DataError.Network.TOO_MANY_REQUESTS -> "Too many attempts. Try again shortly."
            DataError.Network.NO_INTERNET -> "No internet connection."
            DataError.Network.PAYLOAD_TOO_LARGE -> "The request is too large."
            DataError.Network.SERVER_ERROR -> "The server is unavailable. Try again later."
            DataError.Network.SERIALIZATION -> "The server response was not in the expected format."
            DataError.Network.UNKNOWN -> "Something went wrong. Try again."
        }
    }

    private fun loadDefaultMpesaNumber(): String? {
        val methodsJson = paymentSharedPreferences.getString("saved_payment_methods", null)
        if (!methodsJson.isNullOrBlank()) {
            try {
                val methods = Json.decodeFromString<List<PaymentMethod>>(methodsJson)
                return methods.find { it.isDefault && it.type == PaymentMethodType.MPESA }?.detail
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun parseCreatedAtToMillis(dateStr: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            try {
                val sdfFallback = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                sdfFallback.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (ex: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}