package com.example.kaoyanadventure.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val inGame = currentRoute == Routes.GAME_TASKS ||
        currentRoute == Routes.GAME_WALLET ||
        currentRoute == Routes.GAME_SHOP

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BarItem(
                selected = currentRoute == Routes.DASHBOARD,
                label = "仪表盘",
                icon = { Icon(Icons.Filled.Dashboard, contentDescription = "仪表盘") },
                onClick = { onNavigate(Routes.DASHBOARD) }
            )
            BarItem(
                selected = currentRoute == Routes.TIMER,
                label = "计时",
                icon = { Icon(Icons.Filled.Book, contentDescription = "计时") },
                onClick = { onNavigate(Routes.TIMER) }
            )
            BarItem(
                selected = currentRoute == Routes.RECORDS,
                label = "记录",
                icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = "记录") },
                onClick = { onNavigate(Routes.RECORDS) }
            )
            BarItem(
                selected = currentRoute == Routes.REVIEW,
                label = "统计",
                icon = { Icon(Icons.Filled.BarChart, contentDescription = "统计") },
                onClick = { onNavigate(Routes.REVIEW) }
            )
            BarItem(
                selected = inGame,
                label = "任务",
                icon = { Icon(Icons.Filled.Extension, contentDescription = "任务") },
                onClick = { onNavigate(Routes.GAME_TASKS) }
            )
            BarItem(
                selected = currentRoute == Routes.SETTINGS,
                label = "设置",
                icon = { Icon(Icons.Filled.Settings, contentDescription = "设置") },
                onClick = { onNavigate(Routes.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BarItem(
    selected: Boolean,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val bg = if (selected) cs.primary.copy(alpha = 0.12f) else cs.surface
    val fg = if (selected) cs.primary else cs.onSurfaceVariant

    Surface(
        color = bg,
        contentColor = fg,
        shape = MaterialTheme.shapes.large
    ) {
        IconButton(onClick = onClick, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
            // 竖排：图标 +（选中时显示）文字
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.Center
            ) {
                icon()
                if (selected) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
