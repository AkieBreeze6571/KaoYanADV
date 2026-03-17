package com.example.kaoyanadventure.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.data.TodayAchievementState
import com.example.kaoyanadventure.data.isGameReviewSubjectName
import com.example.kaoyanadventure.ai.CoachCard
import com.example.kaoyanadventure.game.logic.RewardRules
import com.example.kaoyanadventure.timer.TimerService
import com.example.kaoyanadventure.ui.components.LevelRing
import com.example.kaoyanadventure.ui.util.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    container: AppContainer,
    onOpenAchievements: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val subjects by container.repo.observeSubjects().collectAsState(initial = emptyList())
    val active by container.timerStore.activeState.collectAsState(
        initial = com.example.kaoyanadventure.data.ActiveTimerState()
    )
    val bgEnabled by container.settings.enableBackgroundTimer.collectAsState(initial = true)

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = System.currentTimeMillis()
        }
    }

    val todayStart = remember(now) { dayStartEpochMs(now) }
    val todayEnd = remember(now) { dayEndEpochMs(now) }

    val rangeStart = remember(now) {
        val c = Calendar.getInstance().apply { timeInMillis = now }
        c.add(Calendar.DAY_OF_YEAR, -29)
        dayStartEpochMs(c.timeInMillis)
    }

    val historySessions by container.repo.observeSessionsBetween(rangeStart, todayEnd)
        .collectAsState(initial = emptyList())

    val daySummaries = remember(historySessions, rangeStart, now) {
        buildDaySummaries(historySessions, rangeStart, 30, now)
    }
    val streak = remember(daySummaries) { computeStreak(daySummaries) }

    val sessionsToday = remember(historySessions, todayStart, todayEnd) {
        historySessions.filter { it.startEpochMs in todayStart..todayEnd }
    }

    val totalTodayMs = remember(sessionsToday, now) {
        sessionsToday.sumOf { s ->
            val end = s.endEpochMs ?: now
            (end - s.startEpochMs).coerceAtLeast(0)
        }
    }

    val wallet by container.game.observeWallet().collectAsState(initial = null)
    val lvl = wallet?.level ?: 1
    val walletExp = wallet?.exp ?: 0
    val needExp = remember(lvl) { RewardRules.expToNext(lvl) }
    val prog = remember(lvl, walletExp, needExp) {
        if (lvl >= 100) 1f else (walletExp.toFloat() / needExp.toFloat()).coerceIn(0f, 1f)
    }
    val expHint = remember(lvl, walletExp, needExp) {
        if (lvl >= 100) "MAX LEVEL" else "NEXT ${(needExp - walletExp).coerceAtLeast(0)} EXP"
    }
    val animProg by animateFloatAsState(targetValue = prog, label = "lvlProg")

    val snackbarHostState = remember { SnackbarHostState() }
    val activeSubject = remember(subjects, active.subjectId) { subjects.firstOrNull { it.id == active.subjectId } }

    suspend fun settleEndedSegmentIfNeeded(durationMs: Long, endedSubjectName: String?) {
        if (durationMs <= 0L) return
        if (endedSubjectName != null && isGameReviewSubjectName(endedSubjectName)) {
            container.settings.addGamePlayDuration(durationMs)
            snackbarHostState.showSnackbar("游戏复盘计时：+${formatDuration(durationMs)}")
        } else {
            val reward = container.game.studyEconomy.rewardByStudyDuration(durationMs)
            if (reward != null) {
                snackbarHostState.showSnackbar(
                    "学习结算：${reward.durationMinutes}分钟 +${reward.expGain} EXP / +${reward.goldGain} 金币"
                )
            }
        }
    }

    val todayKey = remember(now) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
    }

    val savedAch by container.settings.todayAchievement.collectAsState(initial = TodayAchievementState())
    val savedTierForToday = remember(savedAch, todayKey) {
        if (savedAch.dateKey == todayKey) savedAch.tier else 0
    }

    val tier = remember(totalTodayMs) { achievementTier(totalTodayMs) }

    LaunchedEffect(savedAch.dateKey, todayKey) {
        if (savedAch.dateKey.isNotBlank() && savedAch.dateKey != todayKey) {
            container.settings.resetTodayAchievement(todayKey)
        }
    }

    LaunchedEffect(tier, savedTierForToday, todayKey) {
        if (tier > savedTierForToday) {
            val msg = achievementText(tier)
            if (msg.isNotBlank()) snackbarHostState.showSnackbar(msg)

            container.settings.setTodayAchievement(todayKey, tier)

            container.repo.recordAchievementIfNeeded(
                dateKey = todayKey,
                tier = tier,
                title = msg,
                unlockedAtEpochMs = System.currentTimeMillis()
            )
        }
    }

    val title = remember(streak, totalTodayMs) { titleFrom(streak, totalTodayMs) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SnackbarHost(hostState = snackbarHostState) }

        item {
            HeroDashboardCard(
                title = "我的考研冒险",
                subtitle = "称号：$title",
                streak = streak,
                level = lvl,
                levelProgress = animProg,
                levelExpHint = expHint,
                todayDuration = formatDuration(totalTodayMs),
                badge = badgeText(totalTodayMs),
                onOpenAchievements = onOpenAchievements
            )
        }

        item {
            CoachCard(container)
        }

        item {
            Text("今日各科进度", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(subjects) { subj ->
            val ms = remember(sessionsToday, now) {
                sessionsToday.asSequence()
                    .filter { it.subjectId == subj.id }
                    .sumOf {
                        val end = it.endEpochMs ?: now
                        (end - it.startEpochMs).coerceAtLeast(0)
                    }
            }
            val isRunning = active.sessionId != 0L && active.subjectId == subj.id
            SubjectProgressCard(
                name = subj.name,
                duration = formatDuration(ms),
                ratio = progressRatio(ms, totalTodayMs),
                isRunning = isRunning,
                onStart = {
                    scope.launch {
                        val nowMs = System.currentTimeMillis()
                        val endedSubjectName = activeSubject?.name
                        val endedDuration = if (active.sessionId != 0L) {
                            (nowMs - active.startEpochMs).coerceAtLeast(0L)
                        } else {
                            0L
                        }

                        container.repo.startOrSwitchToSubject(subj.id, nowMs)
                        settleEndedSegmentIfNeeded(endedDuration, endedSubjectName)
                        if (bgEnabled) TimerService.start(ctx)

                        snackbarHostState.showSnackbar(
                            if (isRunning) "已重开：${subj.name}" else "已开始：${subj.name}"
                        )
                    }
                }
            )
        }

        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun HeroDashboardCard(
    title: String,
    subtitle: String,
    streak: Int,
    level: Int,
    levelProgress: Float,
    levelExpHint: String,
    todayDuration: String,
    badge: String,
    onOpenAchievements: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val gradient = Brush.linearGradient(
        listOf(
            cs.primary.copy(alpha = 0.95f),
            cs.secondary.copy(alpha = 0.85f),
            cs.tertiary.copy(alpha = 0.75f)
        )
    )

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    }
                    LevelRing(
                        level = level,
                        progress = levelProgress,
                        ringSize = 76.dp,
                        strokeWidth = 7.dp,
                        progressColor = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        textColor = Color.White,
                        expText = levelExpHint
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AssistChip(
                        onClick = { },
                        label = { Text("连胜 $streak 天", color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.18f))
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(badge, color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.18f))
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("今日总时长", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f))
                    Text(todayDuration, style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = onOpenAchievements,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White
                        )
                    ) { Text("成就墙") }

                    OutlinedButton(
                        onClick = onOpenAchievements,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.5f)))
                        )
                    ) { Text("查看战利品") }
                }
            }
        }
    }
}

@Composable
private fun SubjectProgressCard(
    name: String,
    duration: String,
    ratio: Float,
    isRunning: Boolean,
    onStart: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(duration, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(
                progress = ratio.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("今日占比：${(ratio * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilledTonalButton(onClick = onStart) {
                    Text(if (isRunning) "重开学习" else "开始学习")
                }
            }
        }
    }
}

private fun progressRatio(part: Long, total: Long): Float {
    if (total <= 0L) return 0f
    return (part.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

private fun badgeText(todayMs: Long): String {
    return when {
        todayMs >= 6 * 60 * 60 * 1000L -> "六小时·史诗推进"
        todayMs >= 3 * 60 * 60 * 1000L -> "三小时·状态在线"
        todayMs >= 60 * 60 * 1000L -> "一小时·进入冒险"
        todayMs > 0 -> "起步·迈出第一步"
        else -> "今日任务：开始第一段学习"
    }
}

private fun titleFrom(streak: Int, todayMs: Long): String {
    return when {
        streak >= 30 -> "考研勇者"
        streak >= 14 -> "主线推进者"
        streak >= 7 -> "稳定刷题人"
        streak >= 3 -> "新手冒险者"
        todayMs >= 6 * 60 * 60 * 1000L -> "今日学霸怪物"
        todayMs >= 3 * 60 * 60 * 1000L -> "状态在线"
        todayMs >= 60 * 60 * 1000L -> "进入冒险"
        else -> "准备出发"
    }
}
