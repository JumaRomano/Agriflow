package com.agriflow.app.features.wallet

sealed interface WalletAction {
    object RefreshWallet : WalletAction
    data class ShowWithdrawDialog(val visible: Boolean) : WalletAction
    data class WithdrawAmountChanged(val amount: String) : WalletAction
    data class WithdrawMpesaNumberChanged(val mpesaNumber: String) : WalletAction
    object SubmitWithdrawal : WalletAction
    object NavigateBack : WalletAction
    data class OtpCodeChanged(val code: String) : WalletAction
    object VerifyAndWithdraw : WalletAction
    object ResendOtp : WalletAction
    object GoBackToWithdrawDetails : WalletAction
    data class ToggleUseDefaultMpesa(val use: Boolean) : WalletAction
}