package com.example.kaoyanadventure.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SubjectEntity::class,
        SessionEntity::class,
        AchievementUnlockEntity::class   // ✅ 加上这一行
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun sessionDao(): SessionDao
    abstract fun achievementDao(): AchievementDao
    companion object {
        fun build(context: Context): AppDb =
            Room.databaseBuilder(context, AppDb::class.java, "kaoyan_adventure.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
