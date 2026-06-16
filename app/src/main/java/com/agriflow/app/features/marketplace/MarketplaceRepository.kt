/**
 * Repository interface for managing data transactions related to Marketplace.
 */
package com.agriflow.app.features.marketplace

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.EmptyResult
import com.agriflow.app.features.marketplace.Product
import com.agriflow.app.core.util.Result
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface MarketplaceRepository {
    fun observeProducts(): Flow<List<Product>>

    fun getProductById(id: String): Flow<Product?>

    suspend fun refreshProducts(): EmptyResult<DataError.Network>

    suspend fun getCategories(): Result<List<CategoryDto>, DataError.Network>

    suspend fun uploadImage(file: MultipartBody.Part): Result<ImageResponseDto, DataError.Network>

    suspend fun createProduct(request: ProductUploadRequest): Result<Unit, DataError.Network>

    suspend fun updateProduct(id: String, request: ProductUpdateRequest): Result<Unit, DataError.Network>
    suspend fun markAsOutOfStock(id: String): Result<Unit, DataError.Network>
    suspend fun getMyProducts(): Result<List<Product>, DataError.Network>
}
