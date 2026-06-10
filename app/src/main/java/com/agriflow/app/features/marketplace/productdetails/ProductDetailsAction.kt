package com.agriflow.app.features.marketplace.productdetails

sealed interface ProductDetailsAction {
    data object OnIncrementQuantity : ProductDetailsAction
    data object OnDecrementQuantity : ProductDetailsAction
    data object OnAddToCart : ProductDetailsAction
    data object OnNavigateBack : ProductDetailsAction
}
