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
import com.agriflow.app.features.products.productdetails.ProductDao
import com.agriflow.app.features.products.productdetails.ProductEntity
import com.agriflow.app.features.orders.OrderDao
import com.agriflow.app.features.orders.OrderEntity
import com.agriflow.app.features.wallet.TransactionDao
import com.agriflow.app.features.wallet.TransactionEntity
import com.agriflow.app.features.notifications.NotificationDao
import com.agriflow.app.features.notifications.NotificationEntity
import com.agriflow.app.features.suppliernetwork.SupplierDao
import com.agriflow.app.features.suppliernetwork.SupplierEntity
import com.agriflow.app.features.MyStore.StoreInventoryDao
import com.agriflow.app.features.MyStore.StoreInventoryEntity

@Database(
    entities = [
        SyncMetadataEntity::class,
        ProductEntity::class,
        UserEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        TransactionEntity::class,
        NotificationEntity::class,
        SupplierEntity::class,
        StoreInventoryEntity::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class AgriflowDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supplierDao(): SupplierDao
    abstract fun storeInventoryDao(): StoreInventoryDao
}
