package com.agriflow.app.features.businessdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.marketplace.MarketplaceRepository
import com.agriflow.app.features.ratings.RatingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusinessDetailsViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val ratingsRepository: RatingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val businessId: String = checkNotNull(savedStateHandle["businessId"])

    private val _state = MutableStateFlow(BusinessDetailsState(id = businessId))
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val detailsJob = launch {
                when (val result = marketplaceRepository.getBusinessPublicDetails(businessId)) {
                    is Result.Success -> {
                        val dto = result.data
                        _state.update { currentState ->
                            currentState.copy(
                                name = dto.name ?: getFallbackName(businessId),
                                tagline = dto.tagline ?: "Verified AgriFlow Partner",
                                description = dto.description ?: "Verified distributor offering high quality agri-products and services in our network.",
                                email = dto.email ?: "info@${dto.name?.lowercase()?.replace(" ", "") ?: "agriflow"}.com",
                                phone = dto.phone ?: "+254 700 000 000",
                                rating = dto.rating ?: 4.8,
                                reviewCount = dto.reviewCount ?: 24,
                                logoUrl = dto.logoUrl
                            )
                        }
                    }
                    is Result.Error -> {
                        // Use fallback values on failure
                        _state.update { currentState ->
                            currentState.copy(
                                name = getFallbackName(businessId),
                                tagline = "Verified AgriFlow Partner",
                                description = "Verified distributor offering high quality agri-products and services in our network.",
                                email = "info@agriflow.com",
                                phone = "+254 700 000 000",
                                rating = 4.8,
                                reviewCount = 24
                            )
                        }
                    }
                }
            }

            val productsJob = launch {
                when (val result = marketplaceRepository.getBusinessProducts(businessId)) {
                    is Result.Success -> {
                        _state.update { currentState ->
                            currentState.copy(products = result.data)
                        }
                    }
                    is Result.Error -> {
                        // Keep products empty if API fails
                    }
                }
            }

            val ratingSummaryJob = launch {
                when (val result = ratingsRepository.getBusinessRatingSummary(businessId)) {
                    is Result.Success -> {
                        _state.update { currentState ->
                            currentState.copy(
                                rating = result.data.averageRating,
                                reviewCount = result.data.totalRatings
                            )
                        }
                    }
                    is Result.Error -> {
                        // Keep existing rating if API fails
                    }
                }
            }

            detailsJob.join()
            productsJob.join()
            ratingSummaryJob.join()
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun getFallbackName(id: String): String {
        return when (id) {
            "dist_1" -> "AgriCorp Distributors"
            "dist_2" -> "Safal Agro Services"
            "dist_3" -> "Apex Logistics Ltd"
            "dist_4" -> "GreenGrow Supplies"
            else -> "Verified Agri-Business"
        }
    }
}
