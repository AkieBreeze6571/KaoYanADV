package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.data.SettingsStore
import com.example.kaoyanadventure.game.model.Subject
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class GameSettingsAdapter(
    private val settings: SettingsStore
) : GameSettings {
    override suspend fun getEnabledSubjects(): Set<Subject> =
        settings.enabledGameSubjects.first().mapNotNull {
            runCatching { Subject.valueOf(it) }.getOrNull()
        }.toSet()

    override suspend fun setEnabledSubjects(v: Set<Subject>) {
        settings.setEnabledGameSubjects(v.map { it.name }.toSet())
    }

    override suspend fun getTaskSlots(): Int = settings.taskSlots.first()

    override suspend fun setTaskSlots(v: Int) {
        settings.setTaskSlots(v)
    }

    override suspend fun getFreeRerollPerDay(): Int = settings.freeRerollPerDay.first()

    override suspend fun setFreeRerollPerDay(v: Int) {
        settings.setFreeRerollPerDay(v)
    }

    override suspend fun consumeFreeReroll(date: LocalDate): Boolean {
        val quota = settings.freeRerollPerDay.first()
        return settings.consumeFreeRerollQuota(date.toString(), quota)
    }

    override suspend fun isSprintMode(): Boolean = settings.sprintMode.first()

    override suspend fun setSprintMode(v: Boolean) {
        settings.setSprintMode(v)
    }

    override suspend fun isRarityRateUp(): Boolean = settings.rarityRateUp.first()

    override suspend fun setRarityRateUp(v: Boolean) {
        settings.setRarityRateUp(v)
    }
}
