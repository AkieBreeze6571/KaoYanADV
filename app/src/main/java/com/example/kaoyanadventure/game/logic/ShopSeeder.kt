package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.game.db.dao.ShopDao

object ShopSeeder {
    suspend fun seedIfEmpty(shopDao: ShopDao) {
        val n = shopDao.count()
        if (n == 0) {
            shopDao.insertAll(ShopCatalog.items)
        }
    }
}
