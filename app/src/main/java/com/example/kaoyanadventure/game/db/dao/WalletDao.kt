package com.example.kaoyanadventure.game.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kaoyanadventure.game.db.entities.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE id = 1")
    fun observe(): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet WHERE id = 1")
    suspend fun get(): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WalletEntity)

    @Query("UPDATE wallet SET gold = gold + :delta WHERE id = 1")
    suspend fun addGold(delta: Int)

    @Query("UPDATE wallet SET exp = exp + :delta WHERE id = 1")
    suspend fun addExp(delta: Int)

    @Query("UPDATE wallet SET level = :level, exp = :exp WHERE id = 1")
    suspend fun setLevelExp(level: Int, exp: Int)
}
