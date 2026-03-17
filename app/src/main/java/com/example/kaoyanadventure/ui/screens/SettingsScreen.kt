package com.example.kaoyanadventure.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaoyanadventure.ai.ModelFileManager
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.data.AppThemeMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(container: AppContainer) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val theme by container.settings.themeMode.collectAsState(initial = AppThemeMode.SYSTEM)
    val bg by container.settings.enableBackgroundTimer.collectAsState(initial = true)
    val modelPath by container.settings.llmModelPath.collectAsState(initial = "")
    val username by container.settings.username.collectAsState(initial = "")
    val userAvatarUri by container.settings.userAvatarUri.collectAsState(initial = "")
    val firstLaunchEpochMs by container.settings.firstLaunchEpochMs.collectAsState(initial = 0L)
    val openDays by container.settings.openDays.collectAsState(initial = 0)
    val enabledSubjects by container.settings.enabledGameSubjects.collectAsState(initial = setOf("MATH", "ENGLISH"))
    val sprintMode by container.settings.sprintMode.collectAsState(initial = false)
    val taskSlots by container.settings.taskSlots.collectAsState(initial = 3)
    val freeRerollPerDay by container.settings.freeRerollPerDay.collectAsState(initial = 1)
    val rarityRateUp by container.settings.rarityRateUp.collectAsState(initial = false)
    val gameTotalPlayMs by container.settings.gameTotalPlayMs.collectAsState(initial = 0L)

    var editingUsername by remember(username) { mutableStateOf(username) }
    val timeFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val firstLaunchText = remember(firstLaunchEpochMs) {
        if (firstLaunchEpochMs <= 0L) "暂无记录" else timeFmt.format(Date(firstLaunchEpochMs))
    }

    val devAvatarResId = remember(context) {
        context.resources.getIdentifier("devh", "drawable", context.packageName)
    }
    val (versionName, versionCode) = remember(context) {
        runCatching {
            val pm = context.packageManager
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, 0)
            }
            val name = pi.versionName ?: "unknown"
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pi.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                pi.versionCode.toString()
            }
            name to code
        }.getOrDefault("unknown" to "unknown")
    }

    val pickModel = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val path = ModelFileManager.importModel(context, uri, "model.gguf")
            container.settings.setLlmModelPath(path)
        }
    }

    val pickUserAvatar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        scope.launch {
            container.settings.setUserAvatarUri(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("用户信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userAvatarUri.isNotBlank()) {
                            AsyncImage(
                                model = userAvatarUri,
                                contentDescription = "用户头像",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "默认头像",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = editingUsername,
                            onValueChange = { editingUsername = it },
                            label = { Text("用户名") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { pickUserAvatar.launch(arrayOf("image/*")) }) { Text("选择头像") }
                            OutlinedButton(
                                onClick = { scope.launch { container.settings.setUserAvatarUri("") } },
                                enabled = userAvatarUri.isNotBlank()
                            ) { Text("清除头像") }
                        }
                    }
                }

                Button(
                    onClick = { scope.launch { container.settings.setUsername(editingUsername.trim()) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存用户名")
                }
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("使用信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("首次启动时间：$firstLaunchText", style = MaterialTheme.typography.bodyMedium)
                Text("使用天数（打开过 App 的天数）：$openDays 天", style = MaterialTheme.typography.bodyMedium)
                Text("累计游戏时长：${formatGamePlayDuration(gameTotalPlayMs)}", style = MaterialTheme.typography.bodyMedium)
                if (username.isNotBlank()) {
                    Text(
                        "当前用户名：$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("开发者信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8ECF7)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (devAvatarResId != 0) {
                            AsyncImage(
                                model = devAvatarResId,
                                contentDescription = "开发者头像 devh",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "开发者默认头像",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column {
                        Text("作者：HoshinoRUBBY", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("QQ：743086532", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (devAvatarResId == 0) {
                    Text(
                        "未找到开发者头像资源（名称应为 devh）。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("版本信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Version Name: $versionName", style = MaterialTheme.typography.bodyMedium)
                Text("Version Code: $versionCode", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("任务系统设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                Text("科目池", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MATH" to "数学", "ENGLISH" to "英语", "POLITICS" to "政治").forEach { (key, label) ->
                        FilterChip(
                            selected = key in enabledSubjects,
                            onClick = {
                                scope.launch {
                                    val next = enabledSubjects.toMutableSet()
                                    if (key in next) next.remove(key) else next.add(key)
                                    val fixed = if (next.isEmpty()) setOf("MATH", "ENGLISH") else next.toSet()
                                    container.settings.setEnabledGameSubjects(fixed)
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("冲刺期（允许政治入池）")
                    Switch(
                        checked = sprintMode,
                        onCheckedChange = { v -> scope.launch { container.settings.setSprintMode(v) } }
                    )
                }

                Text("任务容量（3-5）", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 4, 5).forEach { slot ->
                        FilterChip(
                            selected = taskSlots == slot,
                            onClick = { scope.launch { container.settings.setTaskSlots(slot) } },
                            label = { Text("$slot") }
                        )
                    }
                }

                Text("每日免费刷新：$freeRerollPerDay", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                container.settings.setFreeRerollPerDay((freeRerollPerDay - 1).coerceAtLeast(0))
                            }
                        }
                    ) { Text("-1") }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                container.settings.setFreeRerollPerDay(freeRerollPerDay + 1)
                            }
                        }
                    ) { Text("+1") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("稀有任务概率提升")
                    Switch(
                        checked = rarityRateUp,
                        onCheckedChange = { v -> scope.launch { container.settings.setRarityRateUp(v) } }
                    )
                }
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("主题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = theme == AppThemeMode.SYSTEM,
                        onClick = { scope.launch { container.settings.setThemeMode(AppThemeMode.SYSTEM) } },
                        label = { Text("跟随系统") }
                    )
                    FilterChip(
                        selected = theme == AppThemeMode.LIGHT,
                        onClick = { scope.launch { container.settings.setThemeMode(AppThemeMode.LIGHT) } },
                        label = { Text("浅色") }
                    )
                    FilterChip(
                        selected = theme == AppThemeMode.DARK,
                        onClick = { scope.launch { container.settings.setThemeMode(AppThemeMode.DARK) } },
                        label = { Text("深色") }
                    )
                }
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("本地模型（GGUF）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (modelPath.isBlank()) "未选择模型"
                    else "当前模型路径：$modelPath",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { pickModel.launch(arrayOf("*/*")) }) {
                        Text("选择 .gguf 文件")
                    }
                    OutlinedButton(
                        onClick = { scope.launch { container.settings.setLlmModelPath("") } },
                        enabled = modelPath.isNotBlank()
                    ) {
                        Text("清除")
                    }
                }
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("后台持续计时（推荐）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Android 系统想要长期后台运行，通常必须使用前台服务（会有低优先级通知）。你可以关掉它，但系统清理应用时计时可能中断。",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (bg) "已开启" else "已关闭")
                    Switch(
                        checked = bg,
                        onCheckedChange = { v -> scope.launch { container.settings.setBackgroundTimerEnabled(v) } }
                    )
                }
            }
        }
    }
}

private fun formatGamePlayDuration(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return String.format("%d:%02d:%02d", h, m, s)
}
