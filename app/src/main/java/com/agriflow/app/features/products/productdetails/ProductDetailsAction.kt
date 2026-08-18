package com.agriflow.app.features.products.productdetails

sealed interface ProductDetailsAction {
    data object OnIncrementQuantity : ProductDetailsAction
    data object OnDecrementQuantity : ProductDetailsAction
    data object OnAddToCart : ProductDetailsAction
    data object OnNavigateBack : ProductDetailsAction
    data class OnSupplierClick(val businessId: String) : ProductDetailsAction
}
