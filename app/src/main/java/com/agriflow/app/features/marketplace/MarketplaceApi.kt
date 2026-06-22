/**
 * Retrofit API interface defining network endpoints for the Marketplace service.
 */
package com.agriflow.app.features.marketplace

import com.agriflow.app.features.products.productdetails.ProductDto
import com.agriflow.app.features.products.productdetails.ProductUpdateRequest
import com.agriflow.app.features.products.productdetails.ProductUploadRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PATCH
import retrofit2.http.Path

import retrofit2.http.Query

interface MarketplaceApi {
    @GET("products/feed")
    suspend fun getProducts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<List<ProductDto>>

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("businesses/verified")
    suspend fun getVerifiedBusinesses(): Response<List<BusinessDto>>

    @Multipart
    @POST("products/upload-image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ImageResponseDto>

    @POST("products")
    suspend fun createProduct(
        @Body request: ProductUploadRequest
    ): Response<Unit>

    @PATCH("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body request: ProductUpdateRequest
    ): Response<Unit>

    @PATCH("products/{id}/out-of-stock")
    suspend fun markAsOutOfStock(
        @Path("id") id: String
    ): Response<Unit>

    @GET("products/my-products")
    suspend fun getMyProducts(): Response<List<ProductDto>>

    @GET("businesses/{id}/public")
    suspend fun getBusinessPublicDetails(
        @Path("id") id: String
    ): Response<PublicBusinessDto>

    @GET("products/business/{businessId}")
    suspend fun getBusinessProducts(
        @Path("businessId") businessId: String
    ): Response<List<ProductDto>>
}
