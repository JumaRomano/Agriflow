/**
 * Represents the class [AgriflowDatabase] providing core functionality within the application.
 */
package com.agriflow.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agriflow.app.features.auth.UserDao
import com.agriflow.app.features.auth.UserEntity
import com.agriflow.app.features.cart.data.CartDao
import com.agriflow.app.features.cart.data.CartItemEntity
import com.agriflow.app.features.marketplace.ProductDao
import com.agriflow.app.features.marketplace.ProductEntity
import com.agriflow.app.features.orders.OrderDao
import com.agriflow.app.features.orders.OrderEntity

@Database(
    entities = [
        SyncMetadataEntity::class,
        ProductEntity::class,
        UserEntity::class,
        CartItemEntity::class,
        OrderEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class AgriflowDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
}
