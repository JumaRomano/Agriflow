/**
 * Repository implementation of [OfflineFirstMarketplaceRepository] managing remote and local data operations.
 */
package com.agriflow.app.features.marketplace

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.EmptyResult
import com.agriflow.app.core.util.Result
import com.agriflow.app.core.util.TimeProvider
import com.agriflow.app.core.util.asEmptyDataResult
import com.agriflow.app.core.util.map
import com.agriflow.app.features.marketplace.productdetails.ProductDao
import com.agriflow.app.features.marketplace.productdetails.toDomain
import com.agriflow.app.features.marketplace.productdetails.toEntity
import com.agriflow.app.features.marketplace.productdetails.Product
import com.agriflow.app.features.marketplace.productdetails.ProductUpdateRequest
import com.agriflow.app.features.marketplace.productdetails.ProductUploadRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import javax.inject.Inject

class OfflineFirstMarketplaceRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val marketplaceApi: MarketplaceApi,
    private val timeProvider: TimeProvider
) : MarketplaceRepository {

    override fun observeProducts(): Flow<List<Product>> {
        return productDao.observeProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getProductById(id: String): Flow<Product?> {
        return productDao.observeProductById(id).map { it?.toDomain() }
    }
    

    override suspend fun refreshProducts(): EmptyResult<DataError.Network> {
        return safeApiCall {
            marketplaceApi.getProducts(page = 0, size = 20)
        }.map { productDtos ->
            android.util.Log.d("MarketplaceRepository", "Received ${productDtos.size} products from API")
            val now = timeProvider.currentTimeMillis()
            val entities = productDtos.mapNotNull { dto ->
                val entity = dto.toEntity(now)
                if (entity == null) {
                    android.util.Log.w("MarketplaceRepository", "Failed to map ProductDto to ProductEntity: $dto")
                }
                entity
            }
            android.util.Log.d("MarketplaceRepository", "Successfully mapped ${entities.size} of ${productDtos.size} products to database")
            productDao.syncProducts(entities)
        }.asEmptyDataResult()
    }

    override suspend fun getCategories(): Result<List<CategoryDto>, DataError.Network> {
        return safeApiCall {
            marketplaceApi.getCategories()
        }
    }

    override suspend fun getVerifiedBusinesses(): Result<List<BusinessDto>, DataError.Network> {
        return safeApiCall {
            marketplaceApi.getVerifiedBusinesses()
        }
    }

    override suspend fun uploadImage(file: MultipartBody.Part): Result<ImageResponseDto, DataError.Network> {
        return safeApiCall {
            marketplaceApi.uploadImage(file)
        }
    }

    override suspend fun createProduct(request: ProductUploadRequest): Result<Unit, DataError.Network> {
        return safeApiCall {
            marketplaceApi.createProduct(request)
        }
    }

    override suspend fun updateProduct(id: String, request: ProductUpdateRequest): Result<Unit, DataError.Network> {
        return safeApiCall {
            marketplaceApi.updateProduct(id, request)
        }
    }

    override suspend fun markAsOutOfStock(id: String): Result<Unit, DataError.Network> {
        return safeApiCall {
            marketplaceApi.markAsOutOfStock(id)
        }
    }

    override suspend fun getMyProducts(): Result<List<Product>, DataError.Network> {
        return safeApiCall {
            marketplaceApi.getMyProducts()
        }.map { dtos ->
            val now = timeProvider.currentTimeMillis()
            dtos.mapNotNull { dto ->
                dto.toEntity(now)?.toDomain()
            }
        }
    }

    override suspend fun getBusinessPublicDetails(id: String): Result<PublicBusinessDto, DataError.Network> {
        return safeApiCall {
            marketplaceApi.getBusinessPublicDetails(id)
        }
    }

    override suspend fun getBusinessProducts(businessId: String): Result<List<Product>, DataError.Network> {
        return safeApiCall {
            marketplaceApi.getBusinessProducts(businessId)
        }.map { dtos ->
            val now = timeProvider.currentTimeMillis()
            val entities = dtos.mapNotNull { dto ->
                dto.toEntity(now)
            }
            if (entities.isNotEmpty()) {
                productDao.upsertProducts(entities)
            }
            entities.map { it.toDomain() }
        }
    }
}
