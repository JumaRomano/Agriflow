package com.agriflow.app.features.marketplace.MyStore.myproducts

sealed interface MyProductsAction {
    data object OnAddProductClicked : MyProductsAction
    data class OnProductClicked(val productId: String) : MyProductsAction
    data class OnEditClicked(val productId: String) : MyProductsAction
    data class OnDeleteClicked(val productId: String) : MyProductsAction
}
