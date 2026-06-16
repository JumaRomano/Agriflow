/**
 * Retrofit API interface defining network endpoints for the Cart service.
 */
package com.agriflow.app.features.cart.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CartApi {

    @POST("cart/add")
    suspend fun addToCart(
        @Body request: AddToCartRequestDto
    ): Response<CartResponseDto>

    @GET("cart")
    suspend fun getCart(): Response<CartResponseDto>

    @DELETE("cart/items/{itemId}")
    suspend fun removeCartItem(
        @Path("itemId") itemId: String
    ): Response<Unit>

    @PATCH("cart/items/deduct/{cartItemId}")
    suspend fun deductCartItem(
        @Path("cartItemId") cartItemId: String,
        @Body request: DeductCartItemRequestDto
    ): Response<CartResponseDto>

    @DELETE("cart/clear")
    suspend fun clearCart(): Response<Unit>
}
