package com.nyxforge.gamebooster

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BoosterService : Service() {

    private val channelId = "booster_channel"

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Game Booster aktif")
            .setContentText("Anti-lag mode berjalan di background")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
        performBoost()
        return START_STICKY
    }

    private fun performBoost() {
        val prefs = getSharedPreferences("booster_prefs", Context.MODE_PRIVATE)
        val targetGame = prefs.getString("target_game_package", null)
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = packageManager
        val protected = setOf(packageName, "android", "com.android.systemui")

        val running = am.runningAppProcesses ?: emptyList()
        for (proc in running) {
            val pkg = proc.pkgList.firstOrNull() ?: continue
            if (pkg in protected || pkg == targetGame) continue
            try {
                val info = pm.getApplicationInfo(pkg, 0)
                if ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue
                am.killBackgroundProcesses(pkg)
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Game Booster", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
