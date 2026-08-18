package com.agriflow.app.features.payment

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethodType {
    MPESA
}

@Serializable
data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val detail: String,
    val isDefault: Boolean,
    val providerName: String
)
