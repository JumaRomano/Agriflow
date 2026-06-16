package com.agriflow.app.features.wallet

sealed interface WalletAction {
    object RefreshWallet : WalletAction
    data class ShowWithdrawDialog(val visible: Boolean) : WalletAction
    data class WithdrawAmountChanged(val amount: String) : WalletAction
    data class WithdrawMpesaNumberChanged(val mpesaNumber: String) : WalletAction
    object SubmitWithdrawal : WalletAction
    object NavigateBack : WalletAction
}