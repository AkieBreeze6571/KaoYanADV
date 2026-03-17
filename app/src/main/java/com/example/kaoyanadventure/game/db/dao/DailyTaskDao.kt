package com.example.kaoyanadventure.game.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kaoyanadventure.game.db.entities.DailyTaskEntity
import com.example.kaoyanadventure.game.model.TaskStatus
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY id ASC")
    fun observeTasksByDate(date: LocalDate): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY id ASC")
    suspend fun getTasksByDate(date: LocalDate): List<DailyTaskEntity>

    @Query("SELECT * FROM daily_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DailyTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DailyTaskEntity>)

    @Update
    suspend fun update(item: DailyTaskEntity)

    @Query("UPDATE daily_tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TaskStatus)

    @Query("DELETE FROM daily_tasks WHERE date < :beforeDate")
    suspend fun deleteBefore(beforeDate: LocalDate)

    @Query("DELETE FROM daily_tasks WHERE date = :date AND status != :completedStatus")
    suspend fun deleteUnfinishedByDate(date: LocalDate, completedStatus: TaskStatus)
}
