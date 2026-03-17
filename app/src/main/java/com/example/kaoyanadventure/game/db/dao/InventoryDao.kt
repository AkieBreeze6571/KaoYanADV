package com.example.kaoyanadventure.game.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kaoyanadventure.game.db.entities.InventoryEntity
import com.example.kaoyanadventure.game.model.EffectType
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY id ASC")
    fun observeAll(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE effectType = :type LIMIT 1")
    suspend fun getByType(type: EffectType): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: InventoryEntity)

    @Query("DELETE FROM inventory WHERE effectType = :type")
    suspend fun deleteByType(type: EffectType)
}
