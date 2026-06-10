package com.agriflow.app.features.marketplace.MyStore.sellerdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerDashboardViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SellerDashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<SellerDashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _state.update {
            it.copy(

            )
        }
    }

    fun onAction(action: SellerDashboardAction) {
        when (action) {
            SellerDashboardAction.OnExportReportClicked -> {
                viewModelScope.launch {
                    _events.send(SellerDashboardEvent.ShowSnackbarMessage("Exporting dashboard report..."))
                }
            }
            is SellerDashboardAction.OnOrderClicked -> {
                viewModelScope.launch {
                    _events.send(SellerDashboardEvent.NavigateToOrderDetail(action.orderId))
                }
            }
            SellerDashboardAction.OnAddProductClicked -> {
                viewModelScope.launch {
                    _events.send(SellerDashboardEvent.NavigateToAddProduct)
                }
            }
        }
    }
}
