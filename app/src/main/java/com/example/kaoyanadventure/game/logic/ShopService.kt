package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.game.db.dao.InventoryDao
import com.example.kaoyanadventure.game.db.dao.ShopDao
import com.example.kaoyanadventure.game.db.dao.WalletDao
import com.example.kaoyanadventure.game.db.entities.InventoryEntity
import com.example.kaoyanadventure.game.db.entities.WalletEntity
import com.example.kaoyanadventure.game.model.EffectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShopService(
    private val walletDao: WalletDao,
    private val shopDao: ShopDao,
    private val inventoryDao: InventoryDao,
    private val settings: GameSettings
) {
    suspend fun buy(sku: String): Result<Unit> = withContext(Dispatchers.IO) {
        val item = shopDao.getBySku(sku)
            ?: return@withContext Result.failure(IllegalArgumentException("SKU not found"))

        val wallet = walletDao.get() ?: run {
            val init = WalletEntity()
            walletDao.upsert(init)
            init
        }

        if (wallet.gold < item.priceGold) {
            return@withContext Result.failure(IllegalStateException("Not enough gold"))
        }
        if (wallet.level < item.levelReq) {
            return@withContext Result.failure(IllegalStateException("Level too low"))
        }

        walletDao.addGold(-item.priceGold)

        if (!item.isConsumable) {
            applyPermanent(item.effectType, item.effectValue)
        } else {
            addToInventory(item.effectType, 1)
        }

        Result.success(Unit)
    }

    private suspend fun applyPermanent(effect: EffectType, value: Int) {
        when (effect) {
            EffectType.PERM_TASK_SLOTS_PLUS_1 -> {
                val cur = settings.getTaskSlots()
                settings.setTaskSlots((cur + value).coerceAtMost(5))
            }
            EffectType.PERM_FREE_REROLL_PLUS_1 -> {
                val cur = settings.getFreeRerollPerDay()
                settings.setFreeRerollPerDay((cur + value).coerceAtLeast(0))
            }
            EffectType.PERM_ENABLE_SPRINT_MODE -> settings.setSprintMode(true)
            EffectType.PERM_RARITY_RATE_UP -> settings.setRarityRateUp(true)
            else -> {
                // no-op
            }
        }
    }

    private suspend fun addToInventory(effect: EffectType, delta: Int) {
        val cur = inventoryDao.getByType(effect)
        if (cur == null) {
            inventoryDao.upsert(
                InventoryEntity(effectType = effect, quantity = delta.coerceAtLeast(0))
            )
        } else {
            inventoryDao.upsert(cur.copy(quantity = (cur.quantity + delta).coerceAtLeast(0)))
        }
    }
}
