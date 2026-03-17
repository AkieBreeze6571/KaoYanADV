package com.example.kaoyanadventure.ui.util

data class AdventureStats(
    val todayTotalMs: Long = 0,
    val todaySubjectMs: Map<Long, Long> = emptyMap(),
    val streakDays: Int = 0,
    val level: Int = 1,
    val levelProgress: Float = 0f,
    val todayBadgeText: String = ""
)

fun expFromMs(ms: Long): Int = (ms / (60_000L)).toInt()

private const val MAX_LEVEL = 100

private fun expNeedForLevel(level: Int): Int {
    if (level >= MAX_LEVEL) return 0

    return if (level < 20) {
        // 1-19 级：平滑增长，便于前期建立正反馈。
        60 + (level - 1) * 8
    } else {
        // 20 级后：非线性递增，升级节奏逐步变慢。
        val t = level - 20
        204 + t * 18 + t * t * 2
    }
}

private fun levelStateFromExp(exp: Int): Triple<Int, Int, Int> {
    var remaining = exp.coerceAtLeast(0)
    var level = 1

    while (level < MAX_LEVEL) {
        val need = expNeedForLevel(level)
        if (remaining < need) return Triple(level, remaining, need)
        remaining -= need
        level += 1
    }

    return Triple(MAX_LEVEL, 0, 0)
}

fun levelFromExp(exp: Int): Pair<Int, Float> {
    val (level, remaining, need) = levelStateFromExp(exp)
    if (level >= MAX_LEVEL) return MAX_LEVEL to 1f
    val progress = (remaining.toFloat() / need.toFloat()).coerceIn(0f, 1f)
    return level to progress
}

fun expToNextLevel(exp: Int): Int {
    val (level, remaining, need) = levelStateFromExp(exp)
    if (level >= MAX_LEVEL) return 0
    return (need - remaining).coerceAtLeast(0)
}
