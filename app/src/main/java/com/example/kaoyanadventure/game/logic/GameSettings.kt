package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.game.model.Subject
import java.time.LocalDate

interface GameSettings {
    suspend fun getEnabledSubjects(): Set<Subject>
    suspend fun setEnabledSubjects(v: Set<Subject>)

    suspend fun getTaskSlots(): Int
    suspend fun setTaskSlots(v: Int)

    suspend fun getFreeRerollPerDay(): Int
    suspend fun setFreeRerollPerDay(v: Int)
    suspend fun consumeFreeReroll(date: LocalDate): Boolean

    suspend fun isSprintMode(): Boolean
    suspend fun setSprintMode(v: Boolean)

    suspend fun isRarityRateUp(): Boolean
    suspend fun setRarityRateUp(v: Boolean)
}
