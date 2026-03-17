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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.game.db.entities.ShopItemEntity
import kotlinx.coroutines.launch

@Composable
fun GameShopScreen(
    container: AppContainer,
    onOpenTasks: () -> Unit,
    onOpenWallet: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val wallet by container.game.observeWallet().collectAsState(initial = null)
    val items by container.game.observeShopItems().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SnackbarHost(hostState = snackbar) }

        item {
            Card(shape = MaterialTheme.shapes.extraLarge, elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("商店", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text("金币：${wallet?.gold ?: 0} · 等级：${wallet?.level ?: 1}")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(onClick = onOpenTasks) { Text("任务页") }
                        Button(onClick = onOpenWallet) { Text("钱包页") }
                    }
                }
            }
        }

        items(items) { item ->
            ShopItemCard(
                item = item,
                canBuy = (wallet?.gold ?: 0) >= item.priceGold && (wallet?.level ?: 1) >= item.levelReq,
                onBuy = {
                    scope.launch {
                        val result = container.game.shop.buy(item.sku)
                        snackbar.showSnackbar(
                            result.fold(
                                onSuccess = { "购买成功：${item.name}" },
                                onFailure = { it.message ?: "购买失败" }
                            )
                        )
                    }
                }
            )
        }

        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItemEntity,
    canBuy: Boolean,
    onBuy: () -> Unit
) {
    Card(shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = {}, label = { Text(if (item.isConsumable) "消耗" else "永久") })
            }
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "价格 ${item.priceGold} 金币 · 等级要求 ${item.levelReq}",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = onBuy, enabled = canBuy) {
                Text("购买")
            }
        }
    }
}
