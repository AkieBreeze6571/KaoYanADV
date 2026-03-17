package com.example.kaoyanadventure.game.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kaoyanadventure.game.db.entities.ShopItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_items ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<ShopItemEntity>>

    @Query("SELECT COUNT(*) FROM shop_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShopItemEntity>)

    @Query("SELECT * FROM shop_items WHERE sku = :sku LIMIT 1")
    suspend fun getBySku(sku: String): ShopItemEntity?
}
