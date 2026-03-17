package com.example.kaoyanadventure.data

import androidx.room.*
import kotlinx.serialization.Serializable

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Long,
    val coverUri: String? = null,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId"), Index("startEpochMs"), Index("endEpochMs")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val startEpochMs: Long,
    val endEpochMs: Long? = null,
    val note: String = "",
    val rating: Int = 0,
    val attachmentUrisJson: String = "[]"
)

@Serializable
data class AttachmentList(val uris: List<String>)

enum class AppThemeMode { SYSTEM, LIGHT, DARK }
