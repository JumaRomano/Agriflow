package com.agriflow.app.features.products.productdetails

sealed interface ProductDetailsEvent {
    data class ShowSnackbar(val message: String) : ProductDetailsEvent
    data object MapsBack : ProductDetailsEvent
    data class NavigateToBusinessDetails(val businessId: String) : ProductDetailsEvent
}
