package com.example.kaoyanadventure.game.db

import androidx.room.TypeConverter
import com.example.kaoyanadventure.game.model.EffectType
import com.example.kaoyanadventure.game.model.Rarity
import com.example.kaoyanadventure.game.model.Subject
import com.example.kaoyanadventure.game.model.TaskStatus
import com.example.kaoyanadventure.game.model.TaskType
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun subjectToString(v: Subject): String = v.name

    @TypeConverter
    fun stringToSubject(v: String): Subject = Subject.valueOf(v)

    @TypeConverter
    fun taskTypeToString(v: TaskType): String = v.name

    @TypeConverter
    fun stringToTaskType(v: String): TaskType = TaskType.valueOf(v)

    @TypeConverter
    fun statusToString(v: TaskStatus): String = v.name

    @TypeConverter
    fun stringToStatus(v: String): TaskStatus = TaskStatus.valueOf(v)

    @TypeConverter
    fun rarityToString(v: Rarity): String = v.name

    @TypeConverter
    fun stringToRarity(v: String): Rarity = Rarity.valueOf(v)

    @TypeConverter
    fun effectTypeToString(v: EffectType): String = v.name

    @TypeConverter
    fun stringToEffectType(v: String): EffectType = EffectType.valueOf(v)

    @TypeConverter
    fun localDateToString(v: LocalDate): String = v.toString()

    @TypeConverter
    fun stringToLocalDate(v: String): LocalDate = LocalDate.parse(v)
}
