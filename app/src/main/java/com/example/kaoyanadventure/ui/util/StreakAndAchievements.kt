package com.example.kaoyanadventure.ui.util

import com.example.kaoyanadventure.data.SessionEntity

data class DaySummary(
    val dayStart: Long,
    val totalMs: Long
)

fun buildDaySummaries(
    sessions: List<SessionEntity>,
    rangeStart: Long,
    days: Int,
    now: Long
): List<DaySummary> {
    val out = ArrayList<DaySummary>(days)
    for (i in 0 until days) {
        val dayStart = rangeStart + i * 24L * 60L * 60L * 1000L
        val dayEnd = dayStart + 24L * 60L * 60L * 1000L
        val ms = sessions.filter { it.startEpochMs in dayStart until dayEnd }.sumOf {
            val end = it.endEpochMs ?: now
            (end - it.startEpochMs).coerceAtLeast(0)
        }
        out.add(DaySummary(dayStart, ms))
    }
    return out
}

fun computeStreak(daySummaries: List<DaySummary>): Int {
    var streak = 0
    for (i in daySummaries.size - 1 downTo 0) {
        if (daySummaries[i].totalMs > 0) streak++ else break
    }
    return streak
}

fun achievementTier(todayMs: Long): Int {
    return when {
        todayMs >= 6 * 60 * 60 * 1000L -> 3
        todayMs >= 3 * 60 * 60 * 1000L -> 2
        todayMs >= 60 * 60 * 1000L -> 1
        else -> 0
    }
}

fun achievementText(tier: Int): String {
    return when (tier) {
        1 -> "成就解锁：一小时 · 进入冒险状态"
        2 -> "成就解锁：三小时 · 状态在线，继续推图！"
        3 -> "成就解锁：六小时 · 你今天是学霸怪物"
        else -> ""
    }
}