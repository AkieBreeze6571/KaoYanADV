package com.example.kaoyanadventure.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.timerStore by preferencesDataStore(name = "timer_state")

data class ActiveTimerState(
    val sessionId: Long = 0,
    val subjectId: Long = 0,
    val startEpochMs: Long = 0
)

class TimerStore(private val context: Context) {
    private val KEY_SESSION_ID = longPreferencesKey("active_session_id")
    private val KEY_SUBJECT_ID = longPreferencesKey("active_subject_id")
    private val KEY_START_EPOCH = longPreferencesKey("active_start_epoch")

    val activeState: Flow<ActiveTimerState> = context.timerStore.data.map { prefs ->
        ActiveTimerState(
            sessionId = prefs[KEY_SESSION_ID] ?: 0,
            subjectId = prefs[KEY_SUBJECT_ID] ?: 0,
            startEpochMs = prefs[KEY_START_EPOCH] ?: 0
        )
    }

    suspend fun setActive(sessionId: Long, subjectId: Long, startEpochMs: Long) {
        context.timerStore.edit {
            it[KEY_SESSION_ID] = sessionId
            it[KEY_SUBJECT_ID] = subjectId
            it[KEY_START_EPOCH] = startEpochMs
        }
    }

    suspend fun clear() {
        context.timerStore.edit {
            it.remove(KEY_SESSION_ID)
            it.remove(KEY_SUBJECT_ID)
            it.remove(KEY_START_EPOCH)
        }
    }
}
