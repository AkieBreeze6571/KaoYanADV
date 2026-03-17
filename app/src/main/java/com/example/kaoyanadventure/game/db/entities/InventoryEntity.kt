package com.example.kaoyanadventure.game.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.kaoyanadventure.game.model.EffectType

@Entity(
    tableName = "inventory",
    indices = [Index("effectType", unique = true)]
)
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val effectType: EffectType,
    val quantity: Int
)
