package com.example.kaoyanadventure.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.game.db.entities.DailyTaskEntity
import com.example.kaoyanadventure.game.logic.RewardRules
import com.example.kaoyanadventure.game.model.TaskStatus
import com.example.kaoyanadventure.ui.components.LevelRing
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun GameTasksScreen(
    container: AppContainer,
    onOpenWallet: () -> Unit,
    onOpenShop: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val today = remember { LocalDate.now() }

    val tasks by container.game.observeTasks(today).collectAsState(initial = emptyList())
    val wallet by container.game.observeWallet().collectAsState(initial = null)
    val level = wallet?.level ?: 1
    val exp = wallet?.exp ?: 0
    val needExp = remember(level) { RewardRules.expToNext(level) }
    val levelProgress = remember(level, exp, needExp) {
        if (level >= 100) 1f else (exp.toFloat() / needExp.toFloat()).coerceIn(0f, 1f)
    }
    val expHint = remember(level, exp, needExp) {
        if (level >= 100) "MAX LEVEL" else "NEXT ${(needExp - exp).coerceAtLeast(0)} EXP"
    }

    LaunchedEffect(Unit) {
        container.game.tasks.ensureTodayTasks()
    }

    fun showResult(result: Result<*>, okMsg: String) {
        scope.launch {
            snackbar.showSnackbar(result.fold(onSuccess = { okMsg }, onFailure = { it.message ?: "操作失败" }))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SnackbarHost(hostState = snackbar) }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("今日任务", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("金币：${wallet?.gold ?: 0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("等级：$level", style = MaterialTheme.typography.titleMedium)
                            Text("当前经验：$exp/$needExp", style = MaterialTheme.typography.bodyMedium)
                        }
                        LevelRing(
                            level = level,
                            progress = levelProgress,
                            ringSize = 84.dp,
                            strokeWidth = 8.dp,
                            expText = expHint
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(onClick = onOpenWallet) { Text("钱包页") }
                        OutlinedButton(onClick = onOpenShop) { Text("商店页") }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val result = container.game.tasks.rerollAllToday()
                                    snackbar.showSnackbar(
                                        result.fold(
                                            onSuccess = { "已整组刷新今日任务（优先消耗免费次数）" },
                                            onFailure = { it.message ?: "刷新失败" }
                                        )
                                    )
                                }
                            }
                        ) { Text("整组刷新") }
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Card(shape = MaterialTheme.shapes.large) {
                    Text(
                        "今日暂无任务，稍后再试。",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
            }
        }

        items(tasks) { task ->
            TaskCard(
                task = task,
                onStart = {
                    scope.launch {
                        showResult(container.game.tasks.startTask(task.id), "任务已开始")
                    }
                },
                onComplete = {
                    scope.launch {
                        val result = container.game.tasks.completeTask(
                            taskId = task.id,
                            actualMinutes = task.targetMinutes,
                            note = "已完成"
                        )
                        snackbar.showSnackbar(
                            result.fold(
                                onSuccess = { s -> "完成 +${s.rewardExp} EXP / +${s.rewardGold} 金币" },
                                onFailure = { it.message ?: "结算失败" }
                            )
                        )
                    }
                },
                onAbandon = {
                    scope.launch {
                        showResult(container.game.tasks.abandonTask(task.id), "任务已放弃")
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }
    }
}

@Composable
private fun TaskCard(
    task: DailyTaskEntity,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onAbandon: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = {}, label = { Text(task.status.name) })
            }
            Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "科目 ${task.subject.name} · ${task.targetMinutes} 分钟 · 难度 ${task.difficulty} · ${task.rarity.name}",
                style = MaterialTheme.typography.bodySmall
            )

            if (task.status == TaskStatus.COMPLETED) {
                Text(
                    "已结算：+${task.rewardExp ?: 0} EXP / +${task.rewardGold ?: 0} 金币",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (task.status == TaskStatus.NEW) {
                        Button(onClick = onStart) { Text("开始") }
                    }
                    if (task.status == TaskStatus.NEW || task.status == TaskStatus.IN_PROGRESS) {
                        FilledTonalButton(onClick = onComplete) { Text("完成并结算") }
                    }
                    OutlinedButton(onClick = onAbandon) { Text("放弃") }
                }
            }
        }
    }
}
