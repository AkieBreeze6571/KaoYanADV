package com.example.kaoyanadventure.game.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.kaoyanadventure.game.model.Rarity
import com.example.kaoyanadventure.game.model.Subject
import com.example.kaoyanadventure.game.model.TaskStatus
import com.example.kaoyanadventure.game.model.TaskType
import java.time.LocalDate

@Entity(
    tableName = "daily_tasks",
    indices = [Index("date"), Index("status"), Index("subject")]
)
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: LocalDate,
    val subject: Subject,
    val type: TaskType,
    val difficulty: Int, // 1..5
    val targetMinutes: Int, // 5/10/15/20/25/30...
    val rarity: Rarity,
    val title: String,
    val description: String,
    val status: TaskStatus = TaskStatus.NEW,
    val startedAtEpochMs: Long? = null,
    val completedAtEpochMs: Long? = null,
    val actualMinutes: Int? = null,
    val note: String? = null,
    val rewardExp: Int? = null,
    val rewardGold: Int? = null
)
