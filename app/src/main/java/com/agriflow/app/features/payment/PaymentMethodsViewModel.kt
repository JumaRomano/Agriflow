package com.agriflow.app.features.payment

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(PaymentMethodsState())
    val state = _state.asStateFlow()

    private val _events = Channel<PaymentMethodsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadPaymentMethods()
    }

    fun onAction(action: PaymentMethodsAction) {
        when (action) {
            PaymentMethodsAction.OnNavigateBack -> {
                viewModelScope.launch {
                    _events.send(PaymentMethodsEvent.NavigateBack)
                }
            }
            is PaymentMethodsAction.OnAddPaymentMethod -> {
                addPaymentMethod(action.detail)
            }
            is PaymentMethodsAction.OnDeletePaymentMethod -> {
                deletePaymentMethod(action.methodId)
            }
            is PaymentMethodsAction.OnSetDefaultPaymentMethod -> {
                setDefaultPaymentMethod(action.methodId)
            }
            is PaymentMethodsAction.OnShowAddDialog -> {
                _state.update { it.copy(showAddDialog = action.show) }
            }
            PaymentMethodsAction.OnClearMessages -> {
                _state.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    private fun loadPaymentMethods() {
        _state.update { it.copy(isLoading = true) }
        val methodsJson = sharedPreferences.getString(KEY_METHODS, null)
        val methods = if (!methodsJson.isNullOrBlank()) {
            try {
                Json.decodeFromString<List<PaymentMethod>>(methodsJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        if (methods.isEmpty()) {
            // Seed default M-Pesa if user has a phone number
            viewModelScope.launch {
                val user = tokenRepository.getUserFlow().firstOrNull()
                if (user?.phoneNumber != null) {
                    val defaultMpesa = PaymentMethod(
                        id = UUID.randomUUID().toString(),
                        type = PaymentMethodType.MPESA,
                        detail = user.phoneNumber,
                        isDefault = true,
                        providerName = "M-Pesa"
                    )
                    saveAndEmitMethods(listOf(defaultMpesa))
                } else {
                    _state.update { it.copy(paymentMethods = emptyList(), isLoading = false) }
                }
            }
        } else {
            _state.update { it.copy(paymentMethods = methods, isLoading = false) }
        }
    }

    private fun addPaymentMethod(detail: String) {
        val currentList = _state.value.paymentMethods.toMutableList()
        val newId = UUID.randomUUID().toString()
        val formattedDetail = formatPhoneNumber(detail)

        // Validate input details
        if (formattedDetail.isBlank()) {
            _state.update { it.copy(errorMessage = "Invalid phone number provided") }
            return
        }

        // Check for duplicates
        if (currentList.any { it.detail == formattedDetail }) {
            _state.update { it.copy(errorMessage = "This M-Pesa number is already added") }
            return
        }

        // If this is the first method, make it default automatically
        val shouldBeDefault = currentList.isEmpty()

        val newMethod = PaymentMethod(
            id = newId,
            type = PaymentMethodType.MPESA,
            detail = formattedDetail,
            isDefault = shouldBeDefault,
            providerName = "M-Pesa"
        )

        currentList.add(newMethod)
        saveAndEmitMethods(currentList)
        _state.update { 
            it.copy(
                showAddDialog = false, 
                successMessage = "M-Pesa number added successfully"
            ) 
        }
        sendSnackbar("Added M-Pesa payment method")
    }

    private fun deletePaymentMethod(id: String) {
        val currentList = _state.value.paymentMethods.toMutableList()
        val methodToDelete = currentList.find { it.id == id } ?: return
        
        currentList.remove(methodToDelete)
        
        // If we deleted the default one, make the first remaining method default
        if (methodToDelete.isDefault && currentList.isNotEmpty()) {
            currentList[0] = currentList[0].copy(isDefault = true)
        }

        saveAndEmitMethods(currentList)
        _state.update { it.copy(successMessage = "Payment method removed") }
        sendSnackbar("Removed ${methodToDelete.providerName}")
    }

    private fun setDefaultPaymentMethod(id: String) {
        val updatedList = _state.value.paymentMethods.map { method ->
            method.copy(isDefault = method.id == id)
        }
        saveAndEmitMethods(updatedList)
        val selectedMethod = updatedList.find { it.isDefault }
        selectedMethod?.let {
            sendSnackbar("${it.providerName} set as default payment method")
        }
    }

    private fun saveAndEmitMethods(methods: List<PaymentMethod>) {
        try {
            val json = Json.encodeToString(methods)
            sharedPreferences.edit().putString(KEY_METHODS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _state.update { it.copy(paymentMethods = methods, isLoading = false) }
    }

    private fun formatPhoneNumber(phone: String): String {
        val clean = phone.replace(Regex("[^0-9]"), "")
        return when {
            clean.startsWith("254") && clean.length == 12 -> clean
            clean.startsWith("0") && clean.length == 10 -> "254" + clean.substring(1)
            clean.length == 9 -> "254" + clean
            else -> clean
        }
    }

    private fun sendSnackbar(message: String) {
        viewModelScope.launch {
            _events.send(PaymentMethodsEvent.ShowSnackbar(message))
        }
    }

    private companion object {
        const val PREFS_NAME = "agriflow_payment_methods_prefs"
        const val KEY_METHODS = "saved_payment_methods"
    }
}
