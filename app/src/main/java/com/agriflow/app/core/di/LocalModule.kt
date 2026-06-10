package com.agriflow.app.core.di

import android.content.Context
import androidx.room.Room
import com.agriflow.app.core.database.AgriflowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

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
            .fallbackToDestructiveMigration()
            .build()
    }

    private const val DATABASE_NAME = "agriflow.db"

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
