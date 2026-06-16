package com.agriflow.app.features.wallet

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WalletApi {
    @GET("wallet")
    suspend fun getWallet(): Response<WalletResponseDto>

    @POST("wallet")
    suspend fun createWallet(
        @Body request: CreateWalletRequestDto
    ): Response<WalletResponseDto>

    @POST("wallet/withdraw")
    suspend fun withdraw(
        @Body request: WalletWithdrawRequestDto
    ): Response<WithdrawResponseDto>
    @GET("wallet/transactions")
    suspend fun getTransactions(): Response<List<WalletTransactionDto>>
}

data class CreateWalletRequestDto(
    @SerializedName("ownerId") val ownerId: String,
    @SerializedName("ownerType") val ownerType: String,
    @SerializedName("currency") val currency: String
)

data class WalletResponseDto(
    @SerializedName("id") val id: String?,
    @SerializedName("ownerId") val ownerId: String?,
    @SerializedName("ownerType") val ownerType: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("availableBalance") val availableBalance: Double?,
    @SerializedName("pendingBalance") val pendingBalance: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class WalletWithdrawRequestDto(
    @SerializedName("ownerId") val ownerId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("payoutMethod") val payoutMethod: String
)

data class WithdrawResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("newBalance") val newBalance: Double?
)

data class WalletTransactionDto(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("balanceAfter") val balanceAfter: Double?,
    @SerializedName("category") val category: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("createdAt") val createdAt: String?
)

