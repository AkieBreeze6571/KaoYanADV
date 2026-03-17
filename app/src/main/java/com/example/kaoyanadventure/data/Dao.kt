package com.example.kaoyanadventure.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SubjectEntity): Long

    @Update
    suspend fun update(entity: SubjectEntity)

    @Query("UPDATE subjects SET colorArgb = :colorArgb, coverUri = :coverUri WHERE id = :id")
    suspend fun updateStyle(id: Long, colorArgb: Long, coverUri: String?)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM subjects WHERE name = :name")
    suspend fun countByName(name: String): Int
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE endEpochMs IS NULL LIMIT 1")
    suspend fun getActive(): SessionEntity?

    @Insert
    suspend fun insert(entity: SessionEntity): Long

    @Query("UPDATE sessions SET endEpochMs = :endEpochMs WHERE id = :id")
    suspend fun endSession(id: Long, endEpochMs: Long)

    @Query("UPDATE sessions SET note = :note, rating = :rating, attachmentUrisJson = :attachments WHERE id = :id")
    suspend fun updateDetail(id: Long, note: String, rating: Int, attachments: String)

    @Query("SELECT * FROM sessions ORDER BY startEpochMs DESC LIMIT :limit OFFSET :offset")
    fun observePaged(limit: Int, offset: Int): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE startEpochMs BETWEEN :from AND :to ORDER BY startEpochMs ASC")
    fun observeBetween(from: Long, to: Long): Flow<List<SessionEntity>>

    @Query("SELECT SUM(CASE WHEN endEpochMs IS NULL THEN (:now - startEpochMs) ELSE (endEpochMs - startEpochMs) END) FROM sessions WHERE subjectId = :subjectId AND startEpochMs BETWEEN :from AND :to")
    fun observeSubjectDurationBetween(subjectId: Long, from: Long, to: Long, now: Long): Flow<Long?>

    @Query("SELECT SUM(CASE WHEN endEpochMs IS NULL THEN (:now - startEpochMs) ELSE (endEpochMs - startEpochMs) END) FROM sessions WHERE startEpochMs BETWEEN :from AND :to")
    fun observeTotalDurationBetween(from: Long, to: Long, now: Long): Flow<Long?>
}
@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement_unlocks ORDER BY unlockedAtEpochMs DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<AchievementUnlockEntity>>

    @Query("SELECT COUNT(*) FROM achievement_unlocks WHERE dateKey = :dateKey AND tier = :tier")
    suspend fun exists(dateKey: String, tier: Int): Int

    @Insert
    suspend fun insert(entity: AchievementUnlockEntity)
}
