package com.example.kaoyanadventure.game.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int = 1,
    val level: Int = 1,
    val exp: Int = 0, // 当前等级内经验
    val gold: Int = 0,
    val streakDays: Int = 0,
    val lastClaimDateEpochDay: Long? = null
)
