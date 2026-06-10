package com.agriflow.app.features.marketplace.productdetails

sealed interface ProductDetailsEvent {
    data class ShowSnackbar(val message: String) : ProductDetailsEvent
    data object MapsBack : ProductDetailsEvent
}
