package com.agriflow.app.features.payment

import com.google.gson.annotations.SerializedName

data class StkPushRequestDto(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("amount") val amount: Double
)

data class StkPushResponseDto(
    @SerializedName("message") val message: String? = null,
    @SerializedName(value = "checkoutRequestID", alternate = ["checkoutRequestId", "mpesaCheckoutRequestId"]) val checkoutRequestId: String? = null,
    @SerializedName("responseCode") val responseCode: String? = null,
    @SerializedName("responseDescription") val responseDescription: String? = null
)

data class PaymentDto(
    @SerializedName("id") val id: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("status") val status: String,
    @SerializedName("mpesaCheckoutRequestId") val mpesaCheckoutRequestId: String?,
    @SerializedName("mpesaReceiptNumber") val mpesaReceiptNumber: String?,
    @SerializedName("failureReason") val failureReason: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)
data class CallbackDto(
    @SerializedName("Body") val body: CallbackBody
)

data class CallbackBody(
    @SerializedName("stkCallback") val stkCallback: StkCallback
)

data class StkCallback(
    @SerializedName("MerchantRequestID") val merchantRequestId: String,
    @SerializedName("CheckoutRequestID") val checkoutRequestId: String,
    @SerializedName("ResultCode") val resultCode: Int,
    @SerializedName("ResultDesc") val resultDesc: String,
    @SerializedName("CallbackMetadata") val callbackMetadata: CallbackMetadata? = null
)

data class CallbackMetadata(
    @SerializedName("Item") val items: List<CallbackMetadataItem>
)

data class CallbackMetadataItem(
    @SerializedName("Name") val name: String,
    @SerializedName("Value") val value: com.google.gson.JsonElement? = null
)

// Safaricom expects a simple response code acknowledging receipt of callback
data class CallbackResponseDto(
    @SerializedName("ResponseCode") val responseCode: String? = null,
    @SerializedName("ResponseDescription") val responseDescription: String? = null
)

