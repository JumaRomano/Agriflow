package com.agriflow.app.features.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state = _state.asStateFlow()

    private val _events = Channel<WalletEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadWalletData()
    }

    fun onAction(action: WalletAction) {
        when (action) {
            WalletAction.RefreshWallet -> loadWalletData()
            is WalletAction.ShowWithdrawDialog -> {
                _state.value = _state.value.copy(
                    isWithdrawDialogVisible = action.visible,
                    withdrawAmount = "",
                    withdrawMpesaNumber = "",
                    withdrawAmountError = null,
                    withdrawMpesaNumberError = null
                )
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
            WalletAction.NavigateBack -> {
                viewModelScope.launch {
                    _events.send(WalletEvent.NavigateBack)
                }
            }
        }
    }

    private fun loadWalletData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = tokenRepository.getUserFlow().first()
            if (user == null) {
                _state.value = _state.value.copy(isLoading = false)
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
                    
                    // Fetch transactions
                    val transactionsResult = walletRepository.getTransactions()
                    val txList = when (transactionsResult) {
                        is Result.Success -> {
                            transactionsResult.data.map { dto ->
                                WalletTransaction(
                                    id = dto.id ?: UUID.randomUUID().toString(),
                                    type = if (dto.type?.equals("REVENUE", ignoreCase = true) == true) TransactionType.REVENUE else TransactionType.WITHDRAWAL,
                                    amount = dto.amount ?: 0.0,
                                    description = dto.description?.takeIf { it.isNotBlank() }
                                        ?: dto.category?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }?.plus(" transaction")
                                        ?: "Transaction",
                                    timestamp = parseCreatedAtToMillis(dto.createdAt ?: "")
                                )
                            }
                        }
                        is Result.Error -> emptyList()
                    }

                    _state.value = _state.value.copy(
                        balance = wallet.availableBalance ?: 0.0,
                        pendingBalance = wallet.pendingBalance ?: 0.0,
                        transactions = txList,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false)
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

            val result = walletRepository.withdrawFunds(
                ownerId = user.id,
                amount = amount!!,
                phoneNumber = mpesaNumber,
                accountName = user.username,
                payoutMethod = "MPESA"
            )
            
            when (result) {
                is Result.Success -> {
                    _events.send(WalletEvent.ShowToast("Withdrawal of KES $amount processed successfully."))
                    _state.value = _state.value.copy(
                        isWithdrawDialogVisible = false
                    )
                    loadWalletData()
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _events.send(WalletEvent.ShowToast("Withdrawal failed. Please check network/balance."))
                }
            }
        }
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