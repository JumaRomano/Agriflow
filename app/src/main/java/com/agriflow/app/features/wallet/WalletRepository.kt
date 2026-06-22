package com.agriflow.app.features.wallet

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    suspend fun getWallet(): Result<WalletResponseDto, DataError.Network>
    
    suspend fun createWallet(
        ownerId: String,
        ownerType: String,
        currency: String
    ): Result<WalletResponseDto, DataError.Network>

    suspend fun withdrawFunds(
        ownerId: String,
        amount: Double,
        phoneNumber: String,
        accountName: String,
        payoutMethod: String
    ): Result<WithdrawResponseDto, DataError.Network>

    suspend fun getTransactions(): Result<List<WalletTransactionDto>, DataError.Network>

    fun observeTransactions(): Flow<List<TransactionEntity>>
}
