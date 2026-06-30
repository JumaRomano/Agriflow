package com.agriflow.app.features.ratings

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.core.network.safeApiCall
import javax.inject.Inject

interface RatingsRepository {
    suspend fun getRatingsForSeller(sellerId: String): Result<List<SellerRating>, DataError.Network>
    
    suspend fun submitRating(
        sellerId: String, 
        ratingValue: Float, 
        reviewText: String?
    ): Result<Unit, DataError.Network>
    
    suspend fun getPendingReviews(): Result<List<PendingReview>, DataError.Network>
}

class RatingsRepositoryImpl @Inject constructor(
    private val api: RatingsApi
) : RatingsRepository {

    override suspend fun getRatingsForSeller(sellerId: String): Result<List<SellerRating>, DataError.Network> {
        return when (val result = safeApiCall { api.getSellerRatings(sellerId) }) {
            is Result.Success -> Result.Success(result.data.map { it.toSellerRating() })
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun submitRating(
        sellerId: String,
        ratingValue: Float,
        reviewText: String?
    ): Result<Unit, DataError.Network> {
        val request = SubmitRatingRequestDto(sellerId, ratingValue, reviewText)
        return safeApiCall { api.submitRating(request) }
    }

    override suspend fun getPendingReviews(): Result<List<PendingReview>, DataError.Network> {
        return when (val result = safeApiCall { api.getPendingReviews() }) {
            is Result.Success -> Result.Success(result.data.map { it.toPendingReview() })
            is Result.Error -> Result.Error(result.error)
        }
    }
}
