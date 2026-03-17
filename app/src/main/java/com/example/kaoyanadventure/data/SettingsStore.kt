package com.example.kaoyanadventure.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "settings")

data class TodayAchievementState(
    val dateKey: String = "",
    val tier: Int = 0
)

class SettingsStore(private val context: Context) {
    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_BG_TIMER = booleanPreferencesKey("enable_bg_timer")
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_USER_AVATAR_URI = stringPreferencesKey("user_avatar_uri")
    private val KEY_FIRST_LAUNCH_EPOCH = longPreferencesKey("first_launch_epoch")
    private val KEY_OPEN_DAYS = intPreferencesKey("open_days")
    private val KEY_LAST_OPEN_DATE = stringPreferencesKey("last_open_date")
    private val KEY_GAME_ENABLED_SUBJECTS = stringSetPreferencesKey("enabled_subjects")
    private val KEY_GAME_SPRINT_MODE = booleanPreferencesKey("sprint_mode")
    private val KEY_GAME_TASK_SLOTS = intPreferencesKey("task_slots")
    private val KEY_GAME_FREE_REROLL_PER_DAY = intPreferencesKey("free_reroll_per_day")
    private val KEY_GAME_RARITY_RATE_UP = booleanPreferencesKey("rarity_rate_up")
    private val KEY_GAME_REROLL_DATE = stringPreferencesKey("game_reroll_date")
    private val KEY_GAME_REROLL_USED = intPreferencesKey("game_reroll_used")
    private val KEY_GAME_TOTAL_PLAY_MS = longPreferencesKey("game_total_play_ms")

    // ✅ 今日成就：日期 + 今日已达成的最高档位（0/1/2/3）
    private val KEY_ACH_DATE = stringPreferencesKey("ach_date_key")
    private val KEY_ACH_TIER = intPreferencesKey("ach_tier")

    // ✅ 本地LLM模型路径（用户选择的GGUF拷贝到私有目录）
    private val KEY_LLM_MODEL_PATH = stringPreferencesKey("llm_model_path")

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_THEME]) {
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    }

    val enableBackgroundTimer: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BG_TIMER] ?: true
    }

    val llmModelPath: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LLM_MODEL_PATH] ?: ""
    }

    val username: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USERNAME] ?: ""
    }

    val userAvatarUri: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_AVATAR_URI] ?: ""
    }

    val firstLaunchEpochMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_FIRST_LAUNCH_EPOCH] ?: 0L
    }

    val openDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPEN_DAYS] ?: 0
    }

    val enabledGameSubjects: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val fallback = setOf("MATH", "ENGLISH")
        prefs[KEY_GAME_ENABLED_SUBJECTS]?.takeIf { it.isNotEmpty() } ?: fallback
    }

    val sprintMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GAME_SPRINT_MODE] ?: false
    }

    val taskSlots: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_GAME_TASK_SLOTS] ?: 3).coerceIn(3, 5)
    }

    val freeRerollPerDay: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_GAME_FREE_REROLL_PER_DAY] ?: 1).coerceAtLeast(0)
    }

    val rarityRateUp: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GAME_RARITY_RATE_UP] ?: false
    }

    val gameTotalPlayMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_GAME_TOTAL_PLAY_MS] ?: 0L
    }

    // ✅ 读今日成就状态
    val todayAchievement: Flow<TodayAchievementState> = context.dataStore.data.map { prefs ->
        TodayAchievementState(
            dateKey = prefs[KEY_ACH_DATE] ?: "",
            tier = prefs[KEY_ACH_TIER] ?: 0
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setBackgroundTimerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BG_TIMER] = enabled }
    }

    suspend fun setLlmModelPath(path: String) {
        context.dataStore.edit { it[KEY_LLM_MODEL_PATH] = path }
    }

    suspend fun setUsername(name: String) {
        context.dataStore.edit { it[KEY_USERNAME] = name }
    }

    suspend fun setUserAvatarUri(uri: String) {
        context.dataStore.edit { it[KEY_USER_AVATAR_URI] = uri }
    }

    suspend fun setEnabledGameSubjects(subjects: Set<String>) {
        context.dataStore.edit { it[KEY_GAME_ENABLED_SUBJECTS] = subjects }
    }

    suspend fun setSprintMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_GAME_SPRINT_MODE] = enabled }
    }

    suspend fun setTaskSlots(v: Int) {
        context.dataStore.edit { it[KEY_GAME_TASK_SLOTS] = v.coerceIn(3, 5) }
    }

    suspend fun setFreeRerollPerDay(v: Int) {
        context.dataStore.edit { it[KEY_GAME_FREE_REROLL_PER_DAY] = v.coerceAtLeast(0) }
    }

    suspend fun setRarityRateUp(v: Boolean) {
        context.dataStore.edit { it[KEY_GAME_RARITY_RATE_UP] = v }
    }

    suspend fun addGamePlayDuration(deltaMs: Long) {
        if (deltaMs <= 0L) return
        context.dataStore.edit {
            val cur = it[KEY_GAME_TOTAL_PLAY_MS] ?: 0L
            it[KEY_GAME_TOTAL_PLAY_MS] = (cur + deltaMs).coerceAtLeast(0L)
        }
    }

    suspend fun consumeFreeRerollQuota(dateKey: String, quotaPerDay: Int): Boolean {
        if (quotaPerDay <= 0) return false
        var granted = false
        context.dataStore.edit {
            val savedDate = it[KEY_GAME_REROLL_DATE] ?: ""
            var used = it[KEY_GAME_REROLL_USED] ?: 0

            if (savedDate != dateKey) {
                used = 0
            }

            if (used < quotaPerDay) {
                used += 1
                granted = true
            }

            it[KEY_GAME_REROLL_DATE] = dateKey
            it[KEY_GAME_REROLL_USED] = used
        }
        return granted
    }

    suspend fun recordAppOpen(nowEpochMs: Long) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(nowEpochMs))
        context.dataStore.edit {
            if ((it[KEY_FIRST_LAUNCH_EPOCH] ?: 0L) <= 0L) {
                it[KEY_FIRST_LAUNCH_EPOCH] = nowEpochMs
            }

            val last = it[KEY_LAST_OPEN_DATE] ?: ""
            if (last != today) {
                it[KEY_OPEN_DAYS] = (it[KEY_OPEN_DAYS] ?: 0) + 1
                it[KEY_LAST_OPEN_DATE] = today
            }
        }
    }

    // ✅ 写今日成就状态（只要 tier 更高才写）
    suspend fun setTodayAchievement(dateKey: String, tier: Int) {
        context.dataStore.edit {
            it[KEY_ACH_DATE] = dateKey
            it[KEY_ACH_TIER] = tier
        }
    }

    // ✅ 如果跨天了，重置为 0（你也可以不调用它，让 Dashboard 判断）
    suspend fun resetTodayAchievement(dateKey: String) {
        context.dataStore.edit {
            it[KEY_ACH_DATE] = dateKey
            it[KEY_ACH_TIER] = 0
        }
    }
}
