package com.example.kaoyanadventure.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AppContainer

@Composable
fun CoachCard(container: AppContainer) {
    val vm = remember { CoachViewModel(container) }

    LaunchedEffect(Unit) {
        vm.refreshModelPathAndLoad()
    }

    Card(shape = MaterialTheme.shapes.extraLarge, elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("私人教练", style = MaterialTheme.typography.titleMedium)

            Text(
                if (vm.state.ready) "教练已就位（离线本地模型）" else "未加载模型：去设置选择 .gguf",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { vm.generateCoachText() },
                    enabled = vm.state.ready && !vm.state.generating
                ) { Text(if (vm.state.generating) "生成中…" else "生成今日战报") }

                OutlinedButton(
                    onClick = { vm.refreshModelPathAndLoad() },
                    enabled = !vm.state.generating
                ) { Text("刷新模型") }
            }

            if (vm.state.output.isNotBlank()) {
                Divider()
                Text(vm.state.output, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
