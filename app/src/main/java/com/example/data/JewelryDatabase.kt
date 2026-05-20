package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JewelryDao {
    // Jewelry Items
    @Query("SELECT * FROM jewelry_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<JewelryItem>>

    @Query("SELECT * FROM jewelry_items WHERE id = :id")
    suspend fun getItemById(id: Int): JewelryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: JewelryItem)

    @Update
    suspend fun updateItem(item: JewelryItem)

    @Delete
    suspend fun deleteItem(item: JewelryItem)

    // Sales
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Int)
}

@Database(entities = [JewelryItem::class, Sale::class], version = 3, exportSchema = false)
abstract class JewelryDatabase : RoomDatabase() {
    abstract fun jewelryDao(): JewelryDao

    companion object {
        @Volatile
        private var INSTANCE: JewelryDatabase? = null

        fun getDatabase(context: Context): JewelryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JewelryDatabase::class.java,
                    "jewelry_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class JewelryRepository(private val dao: JewelryDao) {
    val allItems: Flow<List<JewelryItem>> = dao.getAllItems()
    val allSales: Flow<List<Sale>> = dao.getAllSales()

    suspend fun insertItem(item: JewelryItem) {
        dao.insertItem(item)
    }

    suspend fun updateItem(item: JewelryItem) {
        dao.updateItem(item)
    }

    suspend fun deleteItem(item: JewelryItem) {
        dao.deleteItem(item)
    }

    suspend fun registerSale(sale: Sale, item: JewelryItem) {
        // Safe stock check
        val newStock = Math.max(0, item.stockQuantity - sale.quantity)
        dao.updateItem(item.copy(stockQuantity = newStock))
        dao.insertSale(sale)
    }

    suspend fun insertSale(sale: Sale) {
        dao.insertSale(sale)
    }
}
