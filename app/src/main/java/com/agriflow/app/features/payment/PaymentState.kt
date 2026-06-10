package com.agriflow.app.features.payment

data class PaymentState(
    val amount: Double = 0.0,
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val deliveryAddress: String = "",
    val deliveryNotes: String = "",
    val deliveryAddressError: String? = null,
    val isProcessing: Boolean = false,
    val stkPushSent: Boolean = false,
    val paymentSuccess: Boolean = false,
    val errorMessage: String? = null,
    val orderId: String? = null,
    val checkoutRequestId: String? = null,
    val isVerifyingPayment: Boolean = false
)

