package com.agriflow.app.features.suppliernetwork

data class SupplierNetworkState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: String = "All", // "All", "Farms", "Suppliers"
    val suppliers: List<SupplierNetworkItem> = emptyList(),
    val errorMessage: String? = null
)
