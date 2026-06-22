package com.agriflow.app.features.businessdetails

import com.agriflow.app.features.products.productdetails.Product

data class BusinessDetailsState(
    val isLoading: Boolean = false,
    val id: String = "",
    val name: String = "",
    val tagline: String = "",
    val description: String = "",
    val email: String = "",
    val phone: String = "",
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val logoUrl: String? = null,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null
)
