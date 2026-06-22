package com.agriflow.app.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.agriflow.app.core.database.AgriflowDatabase
import com.agriflow.app.features.auth.UserDao
import com.agriflow.app.features.cart.data.CartDao
import com.agriflow.app.features.products.productdetails.ProductDao
import com.agriflow.app.features.orders.OrderDao
import com.agriflow.app.features.wallet.TransactionDao
import com.agriflow.app.features.notifications.NotificationDao
import com.agriflow.app.features.suppliernetwork.SupplierDao
import com.agriflow.app.features.MyStore.StoreInventoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "agriflow.db"

    @Provides
    @Singleton
    fun provideAgriflowDatabase(
        @ApplicationContext context: Context
    ): AgriflowDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = AgriflowDatabase::class.java,
            name = DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AgriflowDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: AgriflowDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    @Singleton
    fun provideCartDao(database: AgriflowDatabase): CartDao {
        return database.cartDao()
    }

    @Provides
    @Singleton
    fun provideOrderDao(database: AgriflowDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AgriflowDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: AgriflowDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideSupplierDao(database: AgriflowDatabase): SupplierDao {
        return database.supplierDao()
    }

    @Provides
    @Singleton
    fun provideStoreInventoryDao(database: AgriflowDatabase): StoreInventoryDao {
        return database.storeInventoryDao()
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS products (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    priceCents INTEGER NOT NULL,
                    currencyCode TEXT NOT NULL,
                    farmerName TEXT NOT NULL,
                    imageUrl TEXT,
                    availableQuantity REAL NOT NULL,
                    quantityUnit TEXT NOT NULL,
                    updatedAtMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_products_category ON products(category)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_products_farmerName ON products(farmerName)")
        }
    }
}
