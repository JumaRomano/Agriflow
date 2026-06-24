/**
 * UI State definition representing the screen state for MyProducts.
 */
package com.agriflow.app.features.products.myproducts

data class MyProductsState(
    val products: List<ListedProduct> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
