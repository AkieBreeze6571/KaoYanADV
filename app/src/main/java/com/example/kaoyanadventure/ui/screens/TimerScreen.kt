package com.example.kaoyanadventure.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.data.isGameReviewSubjectName
import com.example.kaoyanadventure.timer.TimerService
import com.example.kaoyanadventure.ui.util.formatDuration
import kotlinx.coroutines.launch

@Composable
fun TimerScreen(
    container: AppContainer,
    onOpenSession: (Long) -> Unit,
    onEditSubject: (Long) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val subjects by container.repo.observeSubjects().collectAsState(initial = emptyList())
    val active by container.timerStore.activeState.collectAsState(
        initial = com.example.kaoyanadventure.data.ActiveTimerState()
    )

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            now = System.currentTimeMillis()
        }
    }

    val activeDuration = remember(active, now) {
        if (active.sessionId == 0L) 0L else (now - active.startEpochMs).coerceAtLeast(0)
    }

    val bgEnabled by container.settings.enableBackgroundTimer.collectAsState(initial = true)
    val activeSubject = remember(subjects, active.subjectId) { subjects.firstOrNull { it.id == active.subjectId } }
    val snackbar = remember { SnackbarHostState() }

    suspend fun settleEndedSegmentIfNeeded(durationMs: Long, endedSubjectName: String?) {
        if (durationMs <= 0L) return
        if (endedSubjectName != null && isGameReviewSubjectName(endedSubjectName)) {
            container.settings.addGamePlayDuration(durationMs)
            snackbar.showSnackbar("游戏复盘计时：+${formatDuration(durationMs)}")
        } else {
            val reward = container.game.studyEconomy.rewardByStudyDuration(durationMs)
            if (reward != null) {
                snackbar.showSnackbar(
                    "学习结算：${reward.durationMinutes}分钟 +${reward.expGain} EXP / +${reward.goldGain} 金币"
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SnackbarHost(hostState = snackbar) }

        item {
            Text("计时", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

        item {
            ActiveTimerCard(
                activeName = activeSubject?.name,
                running = active.sessionId != 0L,
                duration = formatDuration(activeDuration),
                bgEnabled = bgEnabled,
                onWriteLog = { if (active.sessionId != 0L) onOpenSession(active.sessionId) },
                onStop = {
                    scope.launch {
                        val nowMs = System.currentTimeMillis()
                        val endedSubjectName = activeSubject?.name
                        val endedDuration = if (active.sessionId != 0L) {
                            (nowMs - active.startEpochMs).coerceAtLeast(0L)
                        } else {
                            0L
                        }

                        container.repo.stopActive(nowMs)
                        settleEndedSegmentIfNeeded(endedDuration, endedSubjectName)
                        if (bgEnabled) TimerService.stop(ctx)
                    }
                },
                onKeepBg = { if (bgEnabled) TimerService.start(ctx) }
            )
        }

        item {
            Text("选择科目", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("点击开始 / 切换会自动结算上一段", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(subjects) { s ->
            val isActive = s.id == active.subjectId && active.sessionId != 0L
            SubjectQuestCard(
                name = s.name,
                color = Color(s.colorArgb.toInt()),
                coverUri = s.coverUri,
                active = isActive,
                onStart = {
                    scope.launch {
                        val nowMs = System.currentTimeMillis()
                        val endedSubjectName = activeSubject?.name
                        val endedDuration = if (active.sessionId != 0L) {
                            (nowMs - active.startEpochMs).coerceAtLeast(0L)
                        } else {
                            0L
                        }

                        container.repo.startOrSwitchToSubject(s.id, nowMs)
                        settleEndedSegmentIfNeeded(endedDuration, endedSubjectName)
                        if (bgEnabled) TimerService.start(ctx)
                    }
                },
                onEdit = { onEditSubject(s.id) }
            )
        }

        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun ActiveTimerCard(
    activeName: String?,
    running: Boolean,
    duration: String,
    bgEnabled: Boolean,
    onWriteLog: () -> Unit,
    onStop: () -> Unit,
    onKeepBg: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val gradient = Brush.linearGradient(
        listOf(cs.primary.copy(alpha = 0.22f), cs.secondary.copy(alpha = 0.18f), cs.surface)
    )

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (running) "正在学习：${activeName ?: "未知科目"}" else "当前未在计时",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                if (running) duration else "00:00",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            if (running) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onWriteLog, modifier = Modifier.weight(1f)) { Text("写冒险日志") }
                    FilledTonalButton(onClick = onStop, modifier = Modifier.weight(1f)) { Text("结束本段") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onKeepBg, modifier = Modifier.weight(1f)) { Text("后台保持") }
                    OutlinedButton(onClick = onWriteLog, modifier = Modifier.weight(1f)) { Text("查看/编辑") }
                }

                if (!bgEnabled) {
                    Text(
                        "你已关闭后台持续计时：系统清理应用后计时可能中断。",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    "点下方科目开始学习。切换科目会自动保存上一段。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SubjectQuestCard(
    name: String,
    color: Color,
    coverUri: String?,
    active: Boolean,
    onStart: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (active) 10.dp else 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { onStart() }
        ) {
            if (!coverUri.isNullOrBlank()) {
                AsyncImage(
                    model = coverUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.10f))
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        if (active) "进行中 · 点击可切换/重开" else "点击开始",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(
                        onClick = onEdit,
                        label = { Text("外观") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.18f))
                    )
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f), contentColor = Color.White)
                    ) { Text(if (active) "切换" else "开始") }
                }
            }
        }
    }
}
