package com.agriflow.app.features.ratings

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.core.network.safeApiCall
import javax.inject.Inject

interface RatingsRepository {
    suspend fun getRatingsForBusiness(businessId: String): Result<List<SellerRating>, DataError.Network>
    
    suspend fun submitRating(
        businessId: String,
        ratingValue: Float, 
        reviewText: String?
    ): Result<Unit, DataError.Network>

    suspend fun deleteBusinessRating(businessId: String): Result<Unit, DataError.Network>

    suspend fun getBusinessRatingSummary(businessId: String): Result<BusinessRatingSummary, DataError.Network>

    suspend fun getBusinessRatings(businessId: String): Result<List<Rating>, DataError.Network>
    suspend fun submitBusinessRating(businessId: String, rating: Float, comment: String): Result<Unit, DataError.Network>
    suspend fun deleteBusinessRatingNew(businessId: String): Result<Unit, DataError.Network>
    suspend fun getProductRatings(productId: String): Result<List<Rating>, DataError.Network>
    suspend fun submitProductRating(userId: String, productId: String, rating: Float, comment: String): Result<Unit, DataError.Network>
    suspend fun deleteProductRating(productId: String): Result<Unit, DataError.Network>
}

class RatingsRepositoryImpl @Inject constructor(
    private val api: RatingsApi
) : RatingsRepository {

    override suspend fun getRatingsForBusiness(businessId: String): Result<List<SellerRating>, DataError.Network> {
        return when (val result = safeApiCall { api.getBusinessRatings(businessId) }) {
            is Result.Success -> Result.Success(result.data.map { dto ->
                SellerRating(
                    id = dto.id.orEmpty(),
                    businessId = dto.businessId ?: businessId,
                    buyerId = dto.userId ?: dto.buyerId.orEmpty(),
                    buyerName = dto.buyerName ?: "Anonymous Buyer",
                    ratingValue = dto.ratingValue ?: dto.rating?.toFloatOrNull() ?: 0f,
                    reviewText = dto.comment ?: dto.reviewText,
                    timestamp = dto.timestamp ?: 0L
                )
            })
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun submitRating(
        businessId: String,
        ratingValue: Float,
        reviewText: String?
    ): Result<Unit, DataError.Network> {
        val request = SubmitBusinessRatingRequestDto(
            businessId = businessId,
            rating = ratingValue.toInt(),
            comment = reviewText ?: ""
        )
        return safeApiCall { api.submitBusinessRating(request) }
    }

    override suspend fun deleteBusinessRating(businessId: String): Result<Unit, DataError.Network> {
        return safeApiCall { api.deleteBusinessRatingNew(businessId) }
    }

    override suspend fun getBusinessRatingSummary(businessId: String): Result<BusinessRatingSummary, DataError.Network> {
        return when (val result = safeApiCall { api.getBusinessRatingSummary(businessId) }) {
            is Result.Success -> Result.Success(result.data.toBusinessRatingSummary())
            is Result.Error -> Result.Error(result.error)
        }
    }



    override suspend fun getBusinessRatings(businessId: String): Result<List<Rating>, DataError.Network> {
        return when (val result = safeApiCall { api.getBusinessRatings(businessId) }) {
            is Result.Success -> Result.Success(result.data.map { it.toRating() })
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun submitBusinessRating(
        businessId: String,
        rating: Float,
        comment: String
    ): Result<Unit, DataError.Network> {
        val request = SubmitBusinessRatingRequestDto(
            businessId = businessId,
            rating = rating.toInt(),
            comment = comment
        )
        return safeApiCall { api.submitBusinessRating(request) }
    }

    override suspend fun deleteBusinessRatingNew(businessId: String): Result<Unit, DataError.Network> {
        return safeApiCall { api.deleteBusinessRatingNew(businessId) }
    }

    override suspend fun getProductRatings(productId: String): Result<List<Rating>, DataError.Network> {
        return when (val result = safeApiCall { api.getProductRatings(productId) }) {
            is Result.Success -> Result.Success(result.data.map { it.toRating() })
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun submitProductRating(
        userId: String,
        productId: String,
        rating: Float,
        comment: String
    ): Result<Unit, DataError.Network> {
        val request = SubmitProductRatingRequestDto(
            userId = userId,
            productId = productId,
            rating = rating.toInt(),
            comment = comment
        )
        return safeApiCall { api.submitProductRating(request) }
    }

    override suspend fun deleteProductRating(productId: String): Result<Unit, DataError.Network> {
        return safeApiCall { api.deleteProductRating(productId) }
    }
}
