package com.agriflow.app.features.suppliernetwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.marketplace.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierNetworkViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SupplierNetworkState())
    val state = _state.asStateFlow()

    private val _allSuppliers = MutableStateFlow<List<SupplierNetworkItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow("All")


    init {
        observeSuppliers()
        fetchVerifiedBusinesses()

        viewModelScope.launch {
            combine(_allSuppliers, _searchQuery, _selectedFilter) { list, query, filter ->
                list.filter { item ->
                    val matchesSearch = query.isBlank() || 
                            item.name.contains(query, ignoreCase = true) ||
                            item.tagline.contains(query, ignoreCase = true)
                    
                    val matchesFilter = when (filter) {
                        "Farms" -> item.type == SupplierType.FARMER
                        "Suppliers" -> item.type == SupplierType.SUPPLIER
                        else -> true
                    }
                    matchesSearch && matchesFilter
                }
            }.collect { filteredList ->
                _state.update { it.copy(suppliers = filteredList) }
            }
        }
    }

    private fun observeSuppliers() {
        viewModelScope.launch {
            marketplaceRepository.observeSuppliers().collect { entities ->
                val uiItems = entities.map { entity ->
                    SupplierNetworkItem(
                        id = entity.supplierId,
                        name = entity.name,
                        type = if (entity.type.equals("FARMER", ignoreCase = true) || entity.type.equals("FARM", ignoreCase = true)) SupplierType.FARMER else SupplierType.SUPPLIER,
                        tagline = entity.farmLocation.takeIf { it.isNotBlank() } ?: "Verified partner in the AgriFlow network",
                        logoUrl = entity.logoUrl,
                        rating = entity.rating,
                        reviewCount = entity.reviewCount,
                        emoji = if (entity.type.equals("FARMER", ignoreCase = true) || entity.type.equals("FARM", ignoreCase = true)) "🌾" else "🏢",
                        isVerified = entity.isVerified
                    )
                }
                _allSuppliers.value = uiItems
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        _state.update { it.copy(selectedFilter = filter) }
    }

    private fun fetchVerifiedBusinesses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = marketplaceRepository.getVerifiedBusinesses()) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false, errorMessage = null) }
                }
                is Result.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = if (_allSuppliers.value.isEmpty()) "Failed to load supplier network" else null
                        ) 
                    }
                }
            }
        }
    }
}
