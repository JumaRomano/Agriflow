package com.agriflow.app.features.ratings

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RatingsApi {

    @POST("businesses/ratings")
    suspend fun submitRating(
        @Body request: SubmitRatingRequestDto
    ): Response<Unit>

    @DELETE("ratings/business/{businessId}")
    suspend fun deleteBusinessRating(
        @Path("businessId") businessId: String
    ): Response<Unit>

    @GET("businesses/ratings/business/{businessId}/summary")
    suspend fun getBusinessRatingSummary(
        @Path("businessId") businessId: String
    ): Response<BusinessRatingSummaryDto>

    @POST("businesses/ratings")
    suspend fun submitBusinessRating(
        @Body request: SubmitBusinessRatingRequestDto
    ): Response<Unit>

    @GET("businesses/ratings/business/{businessId}")
    suspend fun getBusinessRatings(
        @Path("businessId") businessId: String
    ): Response<List<RatingDto>>

    @DELETE("businesses/ratings/business/{businessId}")
    suspend fun deleteBusinessRatingNew(
        @Path("businessId") businessId: String
    ): Response<Unit>

    @POST("products/ratings")
    suspend fun submitProductRating(
        @Body request: SubmitProductRatingRequestDto
    ): Response<Unit>

    @GET("products/ratings/product/{productId}")
    suspend fun getProductRatings(
        @Path("productId") productId: String
    ): Response<List<RatingDto>>

    @DELETE("products/ratings/product/{productId}")
    suspend fun deleteProductRating(
        @Path("productId") productId: String
    ): Response<Unit>
}
