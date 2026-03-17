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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.game.logic.RewardRules
import com.example.kaoyanadventure.ui.components.LevelRing

@Composable
fun GameWalletScreen(
    container: AppContainer,
    onOpenTasks: () -> Unit,
    onOpenShop: () -> Unit
) {
    val wallet by container.game.observeWallet().collectAsState(initial = null)
    val inventory by container.game.observeInventory().collectAsState(initial = emptyList())

    val level = wallet?.level ?: 1
    val exp = wallet?.exp ?: 0
    val nextNeed = RewardRules.expToNext(level)
    val progress = if (level >= 100) 1f else (exp.toFloat() / nextNeed.toFloat()).coerceIn(0f, 1f)
    val expHint = if (level >= 100) "MAX LEVEL" else "NEXT ${nextNeed - exp} EXP"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("钱包/状态", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("金币：${wallet?.gold ?: 0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("等级：$level", style = MaterialTheme.typography.titleMedium)
                            Text("当前经验：$exp/$nextNeed", style = MaterialTheme.typography.bodyMedium)
                        }
                        LevelRing(
                            level = level,
                            progress = progress,
                            ringSize = 92.dp,
                            strokeWidth = 8.dp,
                            expText = expHint
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onOpenTasks) { Text("回任务页") }
                        OutlinedButton(onClick = onOpenShop) { Text("去商店") }
                    }
                }
            }
        }

        item {
            Text("背包", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        if (inventory.isEmpty()) {
            item {
                Card(shape = MaterialTheme.shapes.large) {
                    Text(
                        "暂无道具",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(inventory) { item ->
            Card(shape = MaterialTheme.shapes.large) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.effectType.name, style = MaterialTheme.typography.bodyMedium)
                    Text("x${item.quantity}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item { Spacer(Modifier.height(6.dp)) }
    }
}
