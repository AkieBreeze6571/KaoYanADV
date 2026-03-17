package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.game.db.dao.WalletDao
import com.example.kaoyanadventure.game.db.entities.WalletEntity
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StudyRewardResult(
    val durationMinutes: Int,
    val expGain: Int,
    val goldGain: Int,
    val level: Int,
    val exp: Int,
    val gold: Int
)

class StudyEconomyService(
    private val walletDao: WalletDao
) {
    suspend fun rewardByStudyDuration(durationMs: Long): StudyRewardResult? = withContext(Dispatchers.IO) {
        val minutes = (durationMs / 60_000L).toInt().coerceAtLeast(0)
        if (minutes <= 0) return@withContext null

        val expGain = calcStudyExp(minutes)
        val goldGain = calcStudyGold(expGain)
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

        return@withContext StudyRewardResult(
            durationMinutes = minutes,
            expGain = expGain,
            goldGain = goldGain,
            level = level,
            exp = exp,
            gold = gold
        )
    }

    private fun calcStudyExp(minutes: Int): Int {
        // 学习是主要收益来源：每分钟 1 EXP，稳定且易理解。
        return minutes.coerceAtLeast(1)
    }

    private fun calcStudyGold(expGain: Int): Int {
        // 金币和学习收益强绑定，略低于 EXP，避免通胀过快。
        return (expGain * 0.8f).roundToInt().coerceAtLeast(1)
    }

    private suspend fun ensureWallet(): WalletEntity {
        val cur = walletDao.get()
        if (cur != null) return cur
        val init = WalletEntity()
        walletDao.upsert(init)
        return init
    }
}
