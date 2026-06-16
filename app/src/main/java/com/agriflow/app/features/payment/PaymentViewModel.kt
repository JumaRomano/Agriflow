/**
 * ViewModel managing the business logic and UI state for the Payment feature.
 */
package com.agriflow.app.features.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.agriflow.app.core.navigation.Route
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.cart.domain.CartRepository
import com.agriflow.app.features.orders.OrdersRepository
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val tokenRepository: TokenRepository,
    private val cartRepository: CartRepository,
    private val ordersRepository: OrdersRepository,
    private val paymentRepository: PaymentRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("agriflow_payment_methods_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(PaymentState())
    val state = _state.asStateFlow()

    private val _events = Channel<PaymentEvent>()
    val events = _events.receiveAsFlow()

    private var verificationJob: Job? = null

    init {
        val routeArgs = savedStateHandle.toRoute<Route.Payment>()
        _state.update { it.copy(amount = routeArgs.amount) }

        val defaultPhoneNumber = getDefaultPaymentPhoneNumber()
        if (defaultPhoneNumber != null) {
            _state.update { it.copy(phoneNumber = defaultPhoneNumber) }
        } else {
            viewModelScope.launch {
                tokenRepository.getUserFlow().collect { user ->
                    if (user?.phoneNumber != null && _state.value.phoneNumber.isEmpty()) {
                        _state.update { it.copy(phoneNumber = user.phoneNumber) }
                    }
                }
            }
        }
    }

    private fun getDefaultPaymentPhoneNumber(): String? {
        val json = sharedPreferences.getString("saved_payment_methods", null) ?: return null
        return try {
            val methods = Json.decodeFromString<List<PaymentMethod>>(json)
            methods.find { it.isDefault }?.detail
        } catch (e: Exception) {
            null
        }
    }

    fun onAction(action: PaymentAction) {
        when (action) {
            is PaymentAction.OnPhoneNumberChanged -> {
                _state.update { it.copy(phoneNumber = action.number, phoneNumberError = null) }
            }
            is PaymentAction.OnDeliveryAddressChanged -> {
                _state.update { it.copy(deliveryAddress = action.address, deliveryAddressError = null) }
            }
            is PaymentAction.OnDeliveryNotesChanged -> {
                _state.update { it.copy(deliveryNotes = action.notes) }
            }
            PaymentAction.OnInitiatePayment -> {
                initiatePaymentFlow()
            }
            PaymentAction.OnDismissSuccessDialog -> {
                _state.update { it.copy(stkPushSent = false) }
                verifyPaymentStatus(showLoading = true)
            }
            PaymentAction.OnNavigateBack -> {
                viewModelScope.launch {
                    _events.send(PaymentEvent.NavigateBack)
                }
            }
        }
    }

    private fun initiatePaymentFlow() {
        val rawPhone = _state.value.phoneNumber.trim()
        val address = _state.value.deliveryAddress.trim()
        
        val phone = formatPhoneNumberTo254(rawPhone)
        
        var hasError = false
        
        val isPhoneValid = phone.length == 12 && phone.startsWith("254")
        if (!isPhoneValid) {
            _state.update { it.copy(phoneNumberError = "Please enter a valid M-Pesa number (e.g., 0712345678)") }
            hasError = true
        }

        if (address.isEmpty()) {
            _state.update { it.copy(deliveryAddressError = "Delivery address is required") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, errorMessage = null) }

            // 1. Create order on the backend via checkout API first
            val notes = _state.value.deliveryNotes.trim().takeIf { it.isNotEmpty() }
            when (val checkoutResult = ordersRepository.checkout(address, notes)) {
                is Result.Success -> {
                    val order = checkoutResult.data
                    _state.update { it.copy(orderId = order.id) }

                    // 2. Trigger M-Pesa STK Push API using the order ID, phone number, and total amount
                    when (val paymentResult = paymentRepository.initiateStkPush(order.id.orEmpty(), phone, order.totalAmount ?: 0.0)) {
                        is Result.Success -> {
                            val response = paymentResult.data
                            _state.update {
                                it.copy(
                                    isProcessing = false,
                                    stkPushSent = true,
                                    checkoutRequestId = response.checkoutRequestId
                                )
                            }
                            verifyPaymentStatus(showLoading = false)
                        }
                        is Result.Error -> {
                            _state.update {
                                it.copy(
                                    isProcessing = false,
                                    errorMessage = "STK Push dispatch failed: ${paymentResult.error.name}. Please try again."
                                )
                            }
                            _events.send(PaymentEvent.ShowSnackbar("Payment initiation error: ${paymentResult.error.name}"))
                        }
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Order checkout failed: ${checkoutResult.error.name}. Please try again."
                        )
                    }
                    _events.send(PaymentEvent.ShowSnackbar("Checkout error: ${checkoutResult.error.name}"))
                }
            }
        }
    }

    private fun verifyPaymentStatus(showLoading: Boolean = false) {
        val checkoutRequestId = _state.value.checkoutRequestId ?: return
        
        // Avoid launching duplicate polling jobs
        if (verificationJob?.isActive == true) {
            if (showLoading) {
                _state.update { it.copy(isVerifyingPayment = true) }
            }
            return
        }
        
        verificationJob = viewModelScope.launch {
            _state.update { 
                it.copy(
                    isVerifyingPayment = showLoading, 
                    errorMessage = null
                ) 
            }
            
            var verificationSuccessful = false
            val maxAttempts = 15 // Increase to 15 attempts (45 seconds total)
            val delayMillis = 3000L // Poll every 3 seconds
            
            for (attempt in 1..maxAttempts) {
                delay(delayMillis)
                
                when (val statusResult = paymentRepository.getPaymentStatusByCheckoutRequestId(checkoutRequestId)) {
                    is Result.Success -> {
                        val payment = statusResult.data
                        val status = payment.status.uppercase()
                        if (status == "SUCCESS" || status == "PAID" || status == "COMPLETED") {
                            verificationSuccessful = true
                            break
                        } else if (status == "FAILED") {
                            _state.update {
                                it.copy(
                                    isVerifyingPayment = false,
                                    stkPushSent = false,
                                    errorMessage = payment.failureReason ?: "Transaction rejected by user."
                                )
                            }
                            _events.send(PaymentEvent.ShowSnackbar("Payment failed: ${payment.failureReason ?: "M-Pesa validation failed"}"))
                            return@launch
                        }
                    }
                    is Result.Error -> {
                        // Network error or other API issues, ignore and retry until max attempts
                    }
                }
            }
            
            if (verificationSuccessful) {
                _state.update { 
                    it.copy(
                        stkPushSent = false,
                        isVerifyingPayment = false
                    ) 
                }
                // Clear cart upon successful payment
                when (val result = cartRepository.clearCart()) {
                    is Result.Success -> {
                        _state.update { it.copy(paymentSuccess = true) }
                        _events.send(PaymentEvent.NavigateToOrders)
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                paymentSuccess = true,
                                errorMessage = "Payment authorized, but failed to sync cart: ${result.error.name}"
                            )
                        }
                        _events.send(PaymentEvent.ShowSnackbar("Sync warning: Cart could not be cleared on server."))
                        _events.send(PaymentEvent.NavigateToOrders)
                    }
                }
            } else {
                // Verification timed out or is still pending
                _state.update {
                    it.copy(
                        isVerifyingPayment = false,
                        stkPushSent = false,
                        errorMessage = "We couldn't confirm your payment automatically. Please check your Orders status in a moment."
                    )
                }
                // Allow them to navigate to orders to check status later
                _events.send(PaymentEvent.ShowSnackbar("Verification timeout. Check Orders history soon."))
                _events.send(PaymentEvent.NavigateToOrders)
            }
        }
    }

    private fun formatPhoneNumberTo254(phone: String): String {
        val clean = phone.replace(Regex("[^0-9]"), "")
        return when {
            clean.startsWith("254") && clean.length == 12 -> clean
            clean.startsWith("0") && clean.length == 10 -> "254" + clean.substring(1)
            clean.length == 9 -> "254" + clean
            else -> clean
        }
    }
}

