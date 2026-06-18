package com.agriflow.app.features.suppliernetwork

enum class SupplierType { FARMER, SUPPLIER }

data class SupplierNetworkItem(
    val id: String,
    val name: String,
    val type: SupplierType,
    val tagline: String,
    val logoUrl: String? = null,
    val rating: Double,
    val reviewCount: Int,
    val emoji: String,
    val isVerified: Boolean = true
)
