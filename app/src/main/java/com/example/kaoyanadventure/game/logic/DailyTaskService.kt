package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.data.Repository
import com.example.kaoyanadventure.game.db.dao.DailyTaskDao
import com.example.kaoyanadventure.game.db.dao.InventoryDao
import com.example.kaoyanadventure.game.db.dao.ShopDao
import com.example.kaoyanadventure.game.db.dao.WalletDao
import com.example.kaoyanadventure.game.db.entities.DailyTaskEntity
import com.example.kaoyanadventure.game.db.entities.WalletEntity
import com.example.kaoyanadventure.game.model.EffectType
import com.example.kaoyanadventure.game.model.Subject
import com.example.kaoyanadventure.game.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class TaskSettlement(
    val taskId: Long,
    val rewardExp: Int,
    val rewardGold: Int,
    val level: Int,
    val exp: Int,
    val gold: Int
)

class DailyTaskService(
    private val appRepo: Repository,
    private val dailyTaskDao: DailyTaskDao,
    private val walletDao: WalletDao,
    private val inventoryDao: InventoryDao,
    private val shopDao: ShopDao,
    private val settings: GameSettings
) {
    fun observeTasksByDate(date: LocalDate): Flow<List<DailyTaskEntity>> =
        dailyTaskDao.observeTasksByDate(date)

    fun observeWallet(): Flow<WalletEntity?> = walletDao.observe()

    suspend fun ensureTodayTasks(nowEpochMs: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val date = epochToLocalDate(nowEpochMs)
        ShopSeeder.seedIfEmpty(shopDao)
        ensureWallet()
        cleanupOldTasks(date)

        val existing = dailyTaskDao.getTasksByDate(date)
        if (existing.isNotEmpty()) return@withContext

        generateTasksForDate(date, nowEpochMs)
    }

    suspend fun rerollAllToday(nowEpochMs: Long = System.currentTimeMillis()): Result<Unit> =
        withContext(Dispatchers.IO) {
            val date = epochToLocalDate(nowEpochMs)
            val usedFree = settings.consumeFreeReroll(date)
            val hasVoucher = if (!usedFree) consumeOne(EffectType.CONSUME_REROLL_ALL) else true
            if (!hasVoucher) {
                return@withContext Result.failure(IllegalStateException("免费次数不足且缺少整组刷新券"))
            }

            dailyTaskDao.deleteUnfinishedByDate(date, TaskStatus.COMPLETED)
            generateTasksForDate(date, nowEpochMs)
            Result.success(Unit)
        }

    suspend fun startTask(taskId: Long, nowEpochMs: Long = System.currentTimeMillis()): Result<Unit> =
        withContext(Dispatchers.IO) {
            val task = dailyTaskDao.getById(taskId)
                ?: return@withContext Result.failure(IllegalArgumentException("任务不存在"))
            if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.EXPIRED) {
                return@withContext Result.failure(IllegalStateException("任务状态不可开始"))
            }
            dailyTaskDao.update(
                task.copy(
                    status = TaskStatus.IN_PROGRESS,
                    startedAtEpochMs = task.startedAtEpochMs ?: nowEpochMs
                )
            )
            Result.success(Unit)
        }

    suspend fun completeTask(
        taskId: Long,
        actualMinutes: Int,
        note: String?,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Result<TaskSettlement> = withContext(Dispatchers.IO) {
        val task = dailyTaskDao.getById(taskId)
            ?: return@withContext Result.failure(IllegalArgumentException("任务不存在"))
        if (task.status == TaskStatus.COMPLETED) {
            return@withContext Result.failure(IllegalStateException("任务已完成"))
        }

        var exp = RewardRules.calcBaseExp(
            targetMinutes = task.targetMinutes,
            difficulty = task.difficulty,
            rarity = task.rarity
        )
        var gold = RewardRules.calcBaseGold(exp)

        val expBoost = consumeBoostAndGetPercent(
            high = EffectType.CONSUME_EXP_BOOST_20,
            low = EffectType.CONSUME_EXP_BOOST_10
        )
        if (expBoost > 0) {
            exp = (exp * (1f + expBoost / 100f)).roundToInt().coerceAtLeast(1)
        }

        val goldBoost = consumeBoostAndGetPercent(
            high = EffectType.CONSUME_GOLD_BOOST_20,
            low = EffectType.CONSUME_GOLD_BOOST_10
        )
        if (goldBoost > 0) {
            gold = (gold * (1f + goldBoost / 100f)).roundToInt().coerceAtLeast(1)
        }

        val finalActual = actualMinutes.coerceAtLeast(0)
        val normalizedNote = note?.trim()?.takeIf { it.isNotEmpty() }

        dailyTaskDao.update(
            task.copy(
                status = TaskStatus.COMPLETED,
                completedAtEpochMs = nowEpochMs,
                actualMinutes = finalActual,
                note = normalizedNote,
                rewardExp = exp,
                rewardGold = gold
            )
        )

        val wallet = applyWalletRewards(exp, gold)
        Result.success(
            TaskSettlement(
                taskId = taskId,
                rewardExp = exp,
                rewardGold = gold,
                level = wallet.level,
                exp = wallet.exp,
                gold = wallet.gold
            )
        )
    }

    suspend fun abandonTask(taskId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val task = dailyTaskDao.getById(taskId)
            ?: return@withContext Result.failure(IllegalArgumentException("任务不存在"))
        if (task.status == TaskStatus.COMPLETED) {
            return@withContext Result.failure(IllegalStateException("已完成任务不能放弃"))
        }
        dailyTaskDao.update(task.copy(status = TaskStatus.ABANDONED))
        Result.success(Unit)
    }

    private suspend fun generateTasksForDate(date: LocalDate, nowEpochMs: Long) {
        val slots = settings.getTaskSlots().coerceIn(3, 5)
        val rawEnabled = settings.getEnabledSubjects().ifEmpty { setOf(Subject.MATH, Subject.ENGLISH) }
        val sprint = settings.isSprintMode()
        val enabled = if (!sprint && rawEnabled == setOf(Subject.POLITICS)) {
            setOf(Subject.MATH, Subject.ENGLISH)
        } else {
            rawEnabled
        }
        val rarityRateUp = settings.isRarityRateUp()
        val weak = computeWeakSubject(nowEpochMs, enabled)

        // 如有保底券，生成时自动消耗一张来触发“短任务保底”。
        val shortGuaranteed = consumeOne(EffectType.CONSUME_SHORT_TASK_GUARANTEE)

        val generated = TaskGenerator.generate(
            GenerateConfig(
                date = date,
                slots = slots,
                enabledSubjects = enabled,
                sprintMode = sprint,
                weakSubject = weak,
                shortTaskGuaranteed = shortGuaranteed,
                rarityRateUp = rarityRateUp
            )
        )

        val entities = generated.map {
            DailyTaskEntity(
                date = it.date,
                subject = it.subject,
                type = it.type,
                difficulty = it.difficulty,
                targetMinutes = it.targetMinutes,
                rarity = it.rarity,
                title = it.title,
                description = it.description
            )
        }
        dailyTaskDao.insertAll(entities)
    }

    private suspend fun computeWeakSubject(nowEpochMs: Long, enabled: Set<Subject>): Subject? {
        if (enabled.isEmpty()) return null

        val cal = Calendar.getInstance().apply { timeInMillis = nowEpochMs }
        cal.add(Calendar.DAY_OF_YEAR, -6)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val from = cal.timeInMillis

        val sessions = appRepo.observeSessionsBetween(from, nowEpochMs).first()
        val subjects = appRepo.observeSubjects().first()
        val subjectIdToGame = subjects.associate { it.id to mapSubjectName(it.name) }

        val durations = enabled.associateWith { 0L }.toMutableMap()
        sessions.forEach { session ->
            val s = subjectIdToGame[session.subjectId] ?: return@forEach
            if (s !in enabled) return@forEach
            val end = session.endEpochMs ?: nowEpochMs
            val delta = (end - session.startEpochMs).coerceAtLeast(0L)
            durations[s] = (durations[s] ?: 0L) + delta
        }

        return durations.minByOrNull { it.value }?.key
    }

    private fun mapSubjectName(name: String): Subject? {
        val n = name.lowercase()
        return when {
            n.contains("数") || n.contains("math") -> Subject.MATH
            n.contains("英") || n.contains("english") -> Subject.ENGLISH
            n.contains("政") || n.contains("politics") -> Subject.POLITICS
            else -> null
        }
    }

    private suspend fun ensureWallet(): WalletEntity {
        val cur = walletDao.get()
        if (cur != null) return cur
        val init = WalletEntity()
        walletDao.upsert(init)
        return init
    }

    private suspend fun applyWalletRewards(expGain: Int, goldGain: Int): WalletEntity {
        val wallet = ensureWallet()
        var level = wallet.level.coerceAtLeast(1)
        var exp = (wallet.exp + expGain).coerceAtLeast(0)
        var gold = (wallet.gold + goldGain).coerceAtLeast(0)

        while (level < 100) {
            val need = RewardRules.expToNext(level)
            if (exp < need) break
            exp -= need
            level += 1
        }
        if (level >= 100) {
            level = 100
            exp = 0
        }

        walletDao.setLevelExp(level, exp)
        walletDao.addGold(gold - wallet.gold)

        return WalletEntity(
            id = wallet.id,
            level = level,
            exp = exp,
            gold = gold,
            streakDays = wallet.streakDays,
            lastClaimDateEpochDay = wallet.lastClaimDateEpochDay
        )
    }

    private suspend fun consumeBoostAndGetPercent(high: EffectType, low: EffectType): Int {
        return when {
            consumeOne(high) -> 20
            consumeOne(low) -> 10
            else -> 0
        }
    }

    private suspend fun consumeOne(type: EffectType): Boolean {
        val item = inventoryDao.getByType(type) ?: return false
        if (item.quantity <= 0) return false
        val next = item.quantity - 1
        if (next <= 0) {
            inventoryDao.deleteByType(type)
        } else {
            inventoryDao.upsert(item.copy(quantity = next))
        }
        return true
    }

    private suspend fun cleanupOldTasks(today: LocalDate) {
        dailyTaskDao.deleteBefore(today.minusDays(30))
    }

    private fun epochToLocalDate(epochMs: Long): LocalDate =
        Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate()
}
