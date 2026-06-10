package com.agriflow.app.features.cart.data

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.core.util.map
import com.agriflow.app.core.util.asEmptyDataResult
import com.agriflow.app.features.cart.domain.Cart
import com.agriflow.app.features.cart.domain.CartRepository
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val cartApi: CartApi
) : CartRepository {

    override suspend fun getCart(): Result<Cart, DataError.Network> {
        return safeApiCall {
            cartApi.getCart()
        }.map { dto ->
            dto.toDomain()
        }
    }

    override suspend fun addToCart(productId: String, quantity: Double): Result<Cart, DataError.Network> {
        return safeApiCall {
            cartApi.addToCart(
                request = AddToCartRequestDto(
                    productId = productId,
                    quantity = quantity
                )
            )
        }.map { dto ->
            dto.toDomain()
        }
    }

    override suspend fun removeCartItem(itemId: String): Result<Unit, DataError.Network> {
        return safeApiCall {
            cartApi.removeCartItem(itemId)
        }
    }

    override suspend fun deductCartItem(itemId: String, quantity: Double): Result<Cart, DataError.Network> {
        return safeApiCall {
            cartApi.deductCartItem(
                cartItemId = itemId,
                request = DeductCartItemRequestDto(quantity)
            )
        }.map { dto ->
            dto.toDomain()
        }
    }

    override suspend fun clearCart(): Result<Unit, DataError.Network> {
        return safeApiCall {
            cartApi.clearCart()
        }.asEmptyDataResult()
    }
}
