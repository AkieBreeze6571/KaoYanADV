package com.example.kaoyanadventure.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kaoyanadventure.MainActivity
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.data.isGameReviewSubjectName
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicBoolean

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val running = AtomicBoolean(false)
    private val stopping = AtomicBoolean(false)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action.orEmpty()
        when (action) {
            ACTION_START -> startForegroundTimer()
            ACTION_STOP -> finalizeSessionAndStop()
            ACTION_TOGGLE -> {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotification("计时进行中（点此返回）"))
            }
            else -> startForegroundTimer()
        }
        return START_STICKY
    }

    private fun startForegroundTimer() {
        if (running.getAndSet(true)) return
        startForeground(NOTIF_ID, buildNotification("计时进行中（点此返回）"))

        scope.launch {
            while (running.get()) {
                delay(1000)
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotification("计时进行中（点此返回）"))
            }
        }
    }

    private fun finalizeSessionAndStop() {
        if (stopping.getAndSet(true)) return

        scope.launch {
            runCatching {
                val container = AppContainer().apply { init(applicationContext) }
                val nowMs = System.currentTimeMillis()

                val active = withContext(Dispatchers.IO) {
                    container.timerStore.activeState.first()
                }
                val endedDuration = if (active.sessionId != 0L) {
                    (nowMs - active.startEpochMs).coerceAtLeast(0L)
                } else {
                    0L
                }
                val endedSubjectName = withContext(Dispatchers.IO) {
                    if (active.subjectId == 0L) null else container.repo.getSubject(active.subjectId)?.name
                }

                withContext(Dispatchers.IO) {
                    container.repo.stopActive(nowMs)
                    if (endedDuration > 0L) {
                        if (endedSubjectName != null && isGameReviewSubjectName(endedSubjectName)) {
                            container.settings.addGamePlayDuration(endedDuration)
                        } else {
                            container.game.studyEconomy.rewardByStudyDuration(endedDuration)
                        }
                    }
                }
            }

            stopForegroundTimer()
            stopSelf()
        }
    }

    private fun stopForegroundTimer() {
        running.set(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        scope.cancel()
    }

    private fun buildNotification(content: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TimerService::class.java).setAction(ACTION_STOP)
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("我的考研冒险")
            .setContentText(content)
            .setContentIntent(openPending)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "停止计时", stopPending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "后台计时",
            NotificationManager.IMPORTANCE_LOW
        )
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "kaoyan_timer"
        const val NOTIF_ID = 24242

        const val ACTION_START = "timer.start"
        const val ACTION_STOP = "timer.stop"
        const val ACTION_TOGGLE = "timer.toggle"

        fun start(context: Context) {
            val i = Intent(context, TimerService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i) else context.startService(i)
        }

        fun stop(context: Context) {
            val i = Intent(context, TimerService::class.java).setAction(ACTION_STOP)
            context.startService(i)
        }
    }
}
