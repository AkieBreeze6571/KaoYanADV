package com.example.kaoyanadventure.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AchievementUnlockEntity
import com.example.kaoyanadventure.data.AppContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AchievementWallScreen(container: AppContainer) {
    val list by container.repo.observeAchievementWall().collectAsState(initial = emptyList())
    val sdf = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("成就墙", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

        if (list.isEmpty()) {
            Card(shape = MaterialTheme.shapes.extraLarge, elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("背包空空如也", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("去学习，打点战利品回来。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Column
        }

        val total = list.size
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayCount = list.count { it.dateKey == todayKey }

        Card(shape = MaterialTheme.shapes.extraLarge, elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("收藏：$total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("今日新增：$todayCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(onClick = {}, label = { Text("收藏册") })
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(list) { a ->
                AchievementCard(a = a, sdf = sdf)
            }
        }
    }
}

@Composable
private fun AchievementCard(a: AchievementUnlockEntity, sdf: SimpleDateFormat) {
    val (rarity, subtitle) = remember(a.tier) { rarityInfo(a.tier) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(onClick = {}, label = { Text(rarity) })
            }

            Divider()

            Text(
                "解锁：${sdf.format(Date(a.unlockedAtEpochMs))}  ·  ${a.dateKey}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun rarityInfo(tier: Int): Pair<String, String> {
    return when (tier) {
        1 -> "普通" to "进入冒险状态"
        2 -> "稀有" to "状态在线，继续推图"
        3 -> "史诗" to "学霸怪物（褒义）"
        else -> "未知" to ""
    }
}