package com.example.kaoyanadventure.game.logic

import com.example.kaoyanadventure.data.Repository
import com.example.kaoyanadventure.data.SettingsStore
import com.example.kaoyanadventure.game.db.GameDatabase
import java.time.LocalDate

class GameModule(
    appRepo: Repository,
    settingsStore: SettingsStore,
    db: GameDatabase
) {
    private val settingsAdapter = GameSettingsAdapter(settingsStore)
    private val walletDao = db.walletDao()
    private val dailyTaskDao = db.dailyTaskDao()
    private val inventoryDao = db.inventoryDao()
    private val shopDao = db.shopDao()

    val tasks = DailyTaskService(
        appRepo = appRepo,
        dailyTaskDao = dailyTaskDao,
        walletDao = walletDao,
        inventoryDao = inventoryDao,
        shopDao = shopDao,
        settings = settingsAdapter
    )

    val shop = ShopService(
        walletDao = walletDao,
        shopDao = shopDao,
        inventoryDao = inventoryDao,
        settings = settingsAdapter
    )

    val studyEconomy = StudyEconomyService(walletDao)

    fun observeShopItems() = shopDao.observeAll()
    fun observeInventory() = inventoryDao.observeAll()
    fun observeWallet() = walletDao.observe()
    fun observeTasks(date: LocalDate) = dailyTaskDao.observeTasksByDate(date)
}
