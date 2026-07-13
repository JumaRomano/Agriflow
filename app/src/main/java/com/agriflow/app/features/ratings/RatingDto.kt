package com.agriflow.app.features.ratings

import kotlinx.serialization.Serializable
import com.google.gson.annotations.SerializedName

@Serializable
data class SellerRatingDto(
    val id: String,
    val businessId: String,
    val buyerId: String,
    val buyerName: String?,
    val ratingValue: Float,
    val reviewText: String?,
    val timestamp: Long
)

@Serializable
data class SubmitRatingRequestDto(
    val businessId: String,
    val rating: String,
    val comment: String
)

@Serializable
data class BusinessRatingSummaryDto(
    @SerializedName("businessId") val businessId: String? = null,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName("totalRatings") val totalRatings: Int? = null
)

@Serializable
data class PendingReviewDto(
    val orderId: String,
    val businessId: String,
    val sellerName: String,
    val orderCompletedAt: Long
)

@Serializable
data class SubmitBusinessRatingRequestDto(
    @SerializedName("businessId") val businessId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String
)

@Serializable
data class SubmitProductRatingRequestDto(
    @SerializedName("userId") val userId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String
)

@Serializable
data class RatingDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("businessId") val businessId: String? = null,
    @SerializedName("productId") val productId: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("buyerId") val buyerId: String? = null,
    @SerializedName("buyerName") val buyerName: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("ratingValue") val ratingValue: Float? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("reviewText") val reviewText: String? = null,
    @SerializedName("timestamp") val timestamp: Long? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)
