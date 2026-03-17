package com.example.kaoyanadventure.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.kaoyanadventure.data.AppContainer
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb
@Composable
fun SubjectEditScreen(container: AppContainer, subjectId: Long) {
    val scope = rememberCoroutineScope()
    val subject = remember { mutableStateOf<com.example.kaoyanadventure.data.SubjectEntity?>(null) }

    LaunchedEffect(subjectId) {
        subject.value = container.repo.getSubject(subjectId)
    }

    var color by remember { mutableStateOf(Color(0xFF4CAF50)) }
    var coverUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(subject.value?.id) {
        subject.value?.let {
            color = Color(it.colorArgb.toInt())
            coverUri = it.coverUri
        }
    }

    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            coverUri = uri.toString()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("科目外观", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

        val s = subject.value
        if (s == null) {
            Text("加载中…")
            return@Column
        }

        Card(shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    if (!coverUri.isNullOrBlank()) {
                        AsyncImage(model = coverUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(color.copy(alpha = 0.45f)))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { pickCover.launch("image/*") }) { Text("选择封面图") }
                    TextButton(onClick = { coverUri = null }) { Text("移除封面") }
                }

                Text("颜色（调色板）", fontWeight = FontWeight.SemiBold)

                ColorPalettePicker(
                    value = color,
                    onChange = { color = it }
                )

                Button(
                    onClick = {
                        scope.launch {
                            container.repo.updateSubjectStyle(subjectId, color.toArgb().toLong(), coverUri)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
private fun ColorPalettePicker(value: Color, onChange: (Color) -> Unit) {
    val presets = listOf(
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFFFFC107),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF00BCD4),
        Color(0xFFFF5722),
        Color(0xFF607D8B)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            presets.forEach { c ->
                AssistChip(
                    onClick = { onChange(c) },
                    label = { Text(" ") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = c)
                )
            }
        }

        var r by remember(value) { mutableStateOf(value.red) }
        var g by remember(value) { mutableStateOf(value.green) }
        var b by remember(value) { mutableStateOf(value.blue) }

        fun emit() { onChange(Color(r, g, b, 1f)) }

        Text("细调（RGB）", style = MaterialTheme.typography.bodySmall)
        Slider(value = r, onValueChange = { r = it; emit() })
        Slider(value = g, onValueChange = { g = it; emit() })
        Slider(value = b, onValueChange = { b = it; emit() })
    }
}
