package com.example.kaoyanadventure.ui.util

import java.text.SimpleDateFormat
import java.util.*

fun formatDuration(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val ss = s % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, ss) else String.format("%02d:%02d", m, ss)
}

fun dayStartEpochMs(now: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun dayEndEpochMs(now: Long): Long {
    return dayStartEpochMs(now) + 24L * 60L * 60L * 1000L - 1L
}

fun formatDateLabel(epochMs: Long): String {
    val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
    return sdf.format(Date(epochMs))
}
