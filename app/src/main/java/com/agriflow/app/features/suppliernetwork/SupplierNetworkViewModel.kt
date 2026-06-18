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
                    val apiSuppliers = result.data.mapNotNull { dto ->
                        val id = dto.id ?: return@mapNotNull null
                        val name = dto.name ?: return@mapNotNull null
                        SupplierNetworkItem(
                            id = id,
                            name = name,
                            type = if (dto.type?.equals("FARMER", ignoreCase = true) == true) SupplierType.FARMER else SupplierType.SUPPLIER,
                            tagline = dto.tagline ?: "Verified partner in the AgriFlow network",
                            logoUrl = dto.logoUrl,
                            rating = dto.rating ?: 5.0,
                            reviewCount = dto.reviewCount ?: 0,
                            emoji = if (dto.type?.equals("FARMER", ignoreCase = true) == true) "🌾" else "🏢",
                            isVerified = dto.isVerified ?: true
                        )
                    }
                    _allSuppliers.value = apiSuppliers
                    _state.update { it.copy(isLoading = false, errorMessage = null) }
                }
                is Result.Error -> {
                    _allSuppliers.value = emptyList()
                    _state.update { it.copy(isLoading = false, errorMessage = "Failed to load supplier network") }
                }
            }
        }
    }
}
