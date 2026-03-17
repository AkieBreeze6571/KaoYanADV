package com.example.kaoyanadventure.game.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kaoyanadventure.game.db.dao.DailyTaskDao
import com.example.kaoyanadventure.game.db.dao.InventoryDao
import com.example.kaoyanadventure.game.db.dao.ShopDao
import com.example.kaoyanadventure.game.db.dao.WalletDao
import com.example.kaoyanadventure.game.db.entities.DailyTaskEntity
import com.example.kaoyanadventure.game.db.entities.InventoryEntity
import com.example.kaoyanadventure.game.db.entities.ShopItemEntity
import com.example.kaoyanadventure.game.db.entities.WalletEntity

@Database(
    entities = [DailyTaskEntity::class, WalletEntity::class, InventoryEntity::class, ShopItemEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun walletDao(): WalletDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun shopDao(): ShopDao

    companion object {
        fun build(context: Context): GameDatabase =
            Room.databaseBuilder(context, GameDatabase::class.java, "kaoyan_game.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
