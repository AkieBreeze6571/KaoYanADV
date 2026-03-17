package com.example.kaoyanadventure.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class Repository(

    private val db: AppDb,
    private val timerStore: TimerStore
) {
    private val subjects = db.subjectDao()
    private val achievements = db.achievementDao()
    private val sessions = db.sessionDao()
    private val json = Json { ignoreUnknownKeys = true }
    fun observeAchievementWall(): kotlinx.coroutines.flow.Flow<List<AchievementUnlockEntity>> =
        achievements.observeAll()

    suspend fun recordAchievementIfNeeded(dateKey: String, tier: Int, title: String, unlockedAtEpochMs: Long) =
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            if (tier <= 0) return@withContext
            if (achievements.exists(dateKey, tier) > 0) return@withContext
            achievements.insert(
                AchievementUnlockEntity(
                    dateKey = dateKey,
                    tier = tier,
                    title = title,
                    unlockedAtEpochMs = unlockedAtEpochMs
                )
            )
        }
    fun observeSubjects(): Flow<List<SubjectEntity>> = subjects.observeAll()
    suspend fun getSubject(id: Long) = subjects.getById(id)
    suspend fun updateSubjectStyle(id: Long, colorArgb: Long, coverUri: String?) =
        subjects.updateStyle(id, colorArgb, coverUri)

    fun observeSessionById(id: Long): Flow<SessionEntity?> = sessions.observeById(id)
    fun observeSessionsBetween(from: Long, to: Long): Flow<List<SessionEntity>> = sessions.observeBetween(from, to)
    fun observePagedSessions(limit: Int, offset: Int): Flow<List<SessionEntity>> = sessions.observePaged(limit, offset)

    suspend fun ensureDefaultSubjects() = withContext(Dispatchers.IO) {
        val defaults = listOf(
            "数学" to 0xFF4CAF50,
            "英语" to 0xFF2196F3,
            "政治" to 0xFFFF9800,
            "专业课" to 0xFF9C27B0,
            GAME_REVIEW_SUBJECT_NAME to 0xFF607D8B
        )
        defaults.forEachIndexed { idx, (name, color) ->
            if (subjects.countByName(name) == 0) {
                subjects.upsert(
                    SubjectEntity(
                        name = name,
                        colorArgb = color,
                        coverUri = null,
                        sortOrder = idx
                    )
                )
            }
        }
    }

    suspend fun startOrSwitchToSubject(subjectId: Long, nowEpochMs: Long): Long = withContext(Dispatchers.IO) {
        val active = sessions.getActive()
        if (active != null && active.endEpochMs == null) {
            sessions.endSession(active.id, nowEpochMs)
            timerStore.clear()
        }
        val sessionId = sessions.insert(
            SessionEntity(subjectId = subjectId, startEpochMs = nowEpochMs)
        )
        timerStore.setActive(sessionId, subjectId, nowEpochMs)
        sessionId
    }

    suspend fun stopActive(nowEpochMs: Long) = withContext(Dispatchers.IO) {
        val active = sessions.getActive() ?: return@withContext
        if (active.endEpochMs == null) {
            sessions.endSession(active.id, nowEpochMs)
        }
        timerStore.clear()
    }

    suspend fun updateSessionDetail(id: Long, note: String, rating: Int, attachments: List<String>) =
        withContext(Dispatchers.IO) {
            val payload = json.encodeToString(AttachmentList.serializer(), AttachmentList(attachments))
            sessions.updateDetail(id, note, rating, payload)
        }

    fun decodeAttachments(jsonStr: String): List<String> =
        runCatching {
            json.decodeFromString(AttachmentList.serializer(), jsonStr).uris
        }.getOrDefault(emptyList())
}
