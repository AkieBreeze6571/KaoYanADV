package com.example.kaoyanadventure.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.ui.util.formatDuration
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordsScreen(
    container: AppContainer,
    onOpenSession: (Long) -> Unit
) {
    val subjects by container.repo.observeSubjects().collectAsState(initial = emptyList())
    val sessions by container.repo.observePagedSessions(limit = 200, offset = 0).collectAsState(initial = emptyList())

    val sdf = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("记录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(sessions) { _, ses ->
                val subject = subjects.firstOrNull { it.id == ses.subjectId }
                val end = ses.endEpochMs ?: System.currentTimeMillis()
                val dur = (end - ses.startEpochMs).coerceAtLeast(0)

                Card(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSession(ses.id) } // ✅ 点进详情（计时本）
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(subject?.name ?: "未知科目", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${sdf.format(Date(ses.startEpochMs))}  →  ${if (ses.endEpochMs == null) "进行中" else sdf.format(Date(ses.endEpochMs!!))}")
                        Text("时长：${formatDuration(dur)}  |  评分：${ses.rating}")
                        if (ses.note.isNotBlank()) {
                            Text(ses.note, maxLines = 2)
                        } else {
                            Text("（点开写冒险日志）", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}