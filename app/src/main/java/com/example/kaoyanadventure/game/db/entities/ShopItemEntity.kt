package com.example.kaoyanadventure.game.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kaoyanadventure.game.model.EffectType

@Entity(tableName = "shop_items")
data class ShopItemEntity(
    @PrimaryKey val sku: String,
    val name: String,
    val description: String,
    val priceGold: Int,
    val effectType: EffectType,
    val effectValue: Int = 0,
    val isConsumable: Boolean,
    val levelReq: Int = 1,
    val sortOrder: Int = 0
)
