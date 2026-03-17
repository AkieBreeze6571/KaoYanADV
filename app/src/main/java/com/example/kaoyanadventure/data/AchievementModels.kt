package com.example.kaoyanadventure.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_unlocks")
data class AchievementUnlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,          // yyyy-MM-dd
    val tier: Int,                // 1/2/3...
    val title: String,            // 展示标题
    val unlockedAtEpochMs: Long   // 解锁时间
)