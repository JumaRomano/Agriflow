package com.agriflow.app.features.wallet

data class WalletState(
    val balance: Double = 0.0, // Available balance
    val pendingBalance: Double = 0.0, // Pending balance
    val transactions: List<WalletTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isWithdrawDialogVisible: Boolean = false,
    val withdrawAmount: String = "",
    val withdrawMpesaNumber: String = "",
    val withdrawAmountError: String? = null,
    val withdrawMpesaNumberError: String? = null
)

data class WalletTransaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val timestamp: Long
)

enum class TransactionType {
    REVENUE,
    WITHDRAWAL
}
