package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.game.model.Rarity
import kotlin.math.roundToInt

object RewardRules {
    fun expToNext(level: Int): Int {
        val l = level.coerceAtLeast(1)
        return 60 + 20 * l + 10 * l * l
    }

    fun calcBaseExp(targetMinutes: Int, difficulty: Int, rarity: Rarity): Int {
        val d = difficulty.coerceIn(1, 5)
        val factor = when (d) {
            1 -> 1.0
            2 -> 1.2
            3 -> 1.4
            4 -> 1.7
            else -> 2.0
        }
        val rarityMul = when (rarity) {
            Rarity.COMMON -> 1.0
            Rarity.RARE -> 1.08
            Rarity.EPIC -> 1.15
        }
        val exp = targetMinutes * factor * rarityMul
        return exp.roundToInt().coerceAtLeast(1)
    }

    fun calcBaseGold(exp: Int): Int {
        return (exp * 0.5).roundToInt().coerceAtLeast(1)
    }
}
