/**
 * Room Data Access Object (DAO) defining local database operations.
 */
package com.agriflow.app.features.marketplace.productdetails

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    abstract fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    abstract fun observeProductById(id: String): Flow<ProductEntity?>

    @Upsert
    abstract suspend fun upsertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id NOT IN (:ids)")
    protected abstract suspend fun deleteProductsNotIn(ids: List<String>)

    @Query("DELETE FROM products")
    protected abstract suspend fun clearProducts()

    // A non-destructive sync ensures relationships aren't unnecessarily broken.
    @Transaction
    open suspend fun syncProducts(products: List<ProductEntity>) {
        val ids = products.map { it.id }
        if (ids.isNotEmpty()) {
            deleteProductsNotIn(ids)
            upsertProducts(products)
        } else {
            // If the server explicitly returns an empty list, it means all items are deleted.
            clearProducts()
        }
    }
}
