package com.example.kaoyanadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.data.SessionEntity
import com.example.kaoyanadventure.ui.util.*
import java.util.Calendar

@Composable
fun ReviewScreen(container: AppContainer) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10_000)
            now = System.currentTimeMillis()
        }
    }

    val calStart = remember(now) {
        Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -6)
        }
    }
    val start = remember(now) { dayStartEpochMs(calStart.timeInMillis) }
    val end = now

    val weekSessions by container.repo.observeSessionsBetween(start, end).collectAsState(initial = emptyList())

    val perDay = remember(weekSessions, start, now) {
        val map = LinkedHashMap<Long, Long>()
        for (i in 0..6) {
            val day = start + i * 24L * 60L * 60L * 1000L
            val dayEnd = day + 24L * 60L * 60L * 1000L
            val ms = weekSessions.filter { it.startEpochMs in day until dayEnd }.sumOf {
                val e = it.endEpochMs ?: now
                (e - it.startEpochMs).coerceAtLeast(0)
            }
            map[day] = ms
        }
        map
    }

    val totalWeek = remember(perDay) { perDay.values.sum() }

    val heatmap = remember(weekSessions, start, now) { buildHeatmap7x24(weekSessions, start, now) }
    val maxCell = remember(heatmap) { heatmap.flatten().maxOrNull()?.coerceAtLeast(1L) ?: 1L }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("统计", style = MaterialTheme.typography.headlineSmall)
        Text("近7天总时长：${formatDuration(totalWeek)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("高效时段热力图", style = MaterialTheme.typography.titleMedium)
                                Text("越深=该小时学得越多", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            AssistChip(onClick = {}, label = { Text("HUD") })
                        }
                        HeatmapGrid(data = heatmap, maxValue = maxCell)
                    }
                }
            }

            item {
                Text("每日战报", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }

            items(perDay.entries.toList()) { (day, ms) ->
                val p = if (totalWeek == 0L) 0f else (ms.toFloat() / totalWeek.toFloat()).coerceIn(0f, 1f)
                Card(shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(formatDateLabel(day), style = MaterialTheme.typography.titleMedium)
                            Text(formatDuration(ms), style = MaterialTheme.typography.titleMedium)
                        }
                        LinearProgressIndicator(progress = p, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapGrid(data: List<List<Long>>, maxValue: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        data.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { cell ->
                    val ratio = (cell.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
                    val alpha = 0.08f + ratio * 0.72f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), MaterialTheme.shapes.small)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("早", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("午", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("晚", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("夜", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun buildHeatmap7x24(sessions: List<SessionEntity>, startDay0: Long, now: Long): List<List<Long>> {
    val grid = MutableList(7) { MutableList(24) { 0L } }
    fun dayIndexOf(epoch: Long): Int = ((epoch - startDay0) / (24L * 60L * 60L * 1000L)).toInt()

    sessions.forEach { s ->
        val end = s.endEpochMs ?: now
        var t = s.startEpochMs.coerceAtLeast(startDay0)
        val e = end

        while (t < e) {
            val di = dayIndexOf(t)
            if (di !in 0..6) break

            val cal = Calendar.getInstance().apply { timeInMillis = t }
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            val nextHour = Calendar.getInstance().apply {
                timeInMillis = t
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.HOUR_OF_DAY, 1)
            }.timeInMillis

            val sliceEnd = minOf(e, nextHour)
            val addMs = (sliceEnd - t).coerceAtLeast(0)

            grid[di][hour] = grid[di][hour] + addMs
            t = sliceEnd
        }
    }

    return grid.map { it.toList() }
}