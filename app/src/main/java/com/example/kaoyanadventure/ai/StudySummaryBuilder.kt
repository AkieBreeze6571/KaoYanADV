package com.example.kaoyanadventure.ai

import com.example.kaoyanadventure.data.Repository
import com.example.kaoyanadventure.ui.util.dayEndEpochMs
import com.example.kaoyanadventure.ui.util.dayStartEpochMs
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class CoachSubjectSummary(val name: String, val minutes: Int)

@Serializable
data class CoachNote(val subject: String, val rating: Int, val note: String)

@Serializable
data class CoachSummary(
    val date: String,
    val today_total_min: Int,
    val streak_days: Int,
    val subjects: List<CoachSubjectSummary>,
    val best_hours: List<String>,
    val recent_notes: List<CoachNote>
)

object StudySummaryBuilder {
    private val json = Json { prettyPrint = false; encodeDefaults = true }

    suspend fun build(repo: Repository, now: Long, streakDays: Int): String {
        val subjects = repo.observeSubjects().first()
        val start = dayStartEpochMs(now)
        val end = dayEndEpochMs(now)
        val sessionsToday = repo.observeSessionsBetween(start, end).first()

        val subjectMinutes = subjects.associate { s ->
            val ms = sessionsToday.filter { it.subjectId == s.id }.sumOf { ses ->
                val e = ses.endEpochMs ?: now
                (e - ses.startEpochMs).coerceAtLeast(0)
            }
            s.name to (ms / 60000L).toInt()
        }
        val totalMin = subjectMinutes.values.sum()

        val hourBuckets = IntArray(24)
        sessionsToday.forEach { ses ->
            val e = ses.endEpochMs ?: now
            var t = ses.startEpochMs
            while (t < e) {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = t }
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)

                val nextHour = java.util.Calendar.getInstance().apply {
                    timeInMillis = t
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    add(java.util.Calendar.HOUR_OF_DAY, 1)
                }.timeInMillis

                val sliceEnd = minOf(e, nextHour)
                hourBuckets[hour] += ((sliceEnd - t) / 60000L).toInt().coerceAtLeast(0)
                t = sliceEnd
            }
        }

        val bestHours = hourBuckets
            .mapIndexed { idx, v -> idx to v }
            .sortedByDescending { it.second }
            .take(2)
            .filter { it.second > 0 }
            .map { (h, _) -> String.format("%02d:00-%02d:00", h, (h + 1) % 24) }

        val recentSessions = repo.observePagedSessions(limit = 50, offset = 0).first()
        val notes = recentSessions
            .filter { it.note.isNotBlank() }
            .take(5)
            .map { ses ->
                val name = subjects.firstOrNull { it.id == ses.subjectId }?.name ?: "未知"
                CoachNote(subject = name, rating = ses.rating, note = ses.note.take(140))
            }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))

        val payload = CoachSummary(
            date = dateStr,
            today_total_min = totalMin,
            streak_days = streakDays,
            subjects = subjects.map { CoachSubjectSummary(it.name, subjectMinutes[it.name] ?: 0) },
            best_hours = bestHours,
            recent_notes = notes
        )
        return json.encodeToString(payload)
    }
}
