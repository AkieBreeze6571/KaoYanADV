package com.example.kaoyanadventure.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaoyanadventure.data.AppContainer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionDetailScreen(container: AppContainer, sessionId: Long) {
    val scope = rememberCoroutineScope()
    val session by container.repo.observeSessionById(sessionId).collectAsState(initial = null)
    val subjects by container.repo.observeSubjects().collectAsState(initial = emptyList())

    var note by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) }
    var attachments by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(session?.id) {
        session?.let {
            note = it.note
            rating = it.rating.toFloat()
            attachments = container.repo.decodeAttachments(it.attachmentUrisJson)
        }
    }

    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            attachments = attachments + uris.map { it.toString() }
        }
    }

    val sdf = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("学习记录编辑", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

        if (session == null) {
            Text("找不到这条记录。")
            return@Column
        }

        val subject = subjects.firstOrNull { it.id == session!!.subjectId }
        Card(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(subject?.name ?: "未知科目", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${sdf.format(Date(session!!.startEpochMs))}  →  ${session!!.endEpochMs?.let { sdf.format(Date(it)) } ?: "进行中"}")
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            label = { Text("本段记录（文字）") }
        )

        Card(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("总评（0-5）", fontWeight = FontWeight.SemiBold)
                Slider(value = rating, onValueChange = { rating = it }, valueRange = 0f..5f, steps = 4)
                Text("评分：${rating.toInt()}")
            }
        }

        Card(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("图片附件", fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { pickImages.launch("image/*") }) { Text("添加图片") }
                }

                if (attachments.isEmpty()) {
                    Text("暂无图片。你可以把这一段做成“计时本”。", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(attachments) { u ->
                            AsyncImage(model = u, contentDescription = null, modifier = Modifier.size(96.dp))
                        }
                    }
                    TextButton(onClick = { attachments = emptyList() }) { Text("清空附件") }
                }
            }
        }

        Button(
            onClick = {
                scope.launch {
                    container.repo.updateSessionDetail(
                        id = sessionId,
                        note = note,
                        rating = rating.toInt(),
                        attachments = attachments
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }
    }
}
