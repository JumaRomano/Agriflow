package com.agriflow.app.features.products.addproduct

sealed interface AddProductEvent {
    data object MapsBack : AddProductEvent
    data class ShowSnackbar(val message: String) : AddProductEvent
}
