/**
 * UI State definition representing the screen state for PaymentMethods.
 */
package com.agriflow.app.features.payment

data class PaymentMethodsState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
