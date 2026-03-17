package com.example.kaoyanadventure.data

import android.content.Context
import com.example.kaoyanadventure.game.db.GameDatabase
import com.example.kaoyanadventure.game.logic.GameModule

class AppContainer {
    lateinit var context: Context
        private set

    val db by lazy { AppDb.build(context) }
    val gameDb by lazy { GameDatabase.build(context) }
    val timerStore by lazy { TimerStore(context) }
    val repo by lazy { Repository(db, timerStore) }
    val settings by lazy { SettingsStore(context) }
    val game by lazy { GameModule(repo, settings, gameDb) }

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }
}
