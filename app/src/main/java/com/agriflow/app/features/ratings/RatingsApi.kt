package com.agriflow.app.features.ratings

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RatingsApi {
    @GET("sellers/{sellerId}/ratings")
    suspend fun getSellerRatings(
        @Path("sellerId") sellerId: String
    ): Response<List<SellerRatingDto>>

    @POST("sellers/ratings")
    suspend fun submitRating(
        @Body request: SubmitRatingRequestDto
    ): Response<Unit>

    @GET("users/me/pending-reviews")
    suspend fun getPendingReviews(): Response<List<PendingReviewDto>>
}
