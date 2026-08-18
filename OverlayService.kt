package com.nyxforge.gamebooster

import android.app.*
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.StatFs
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var running = true
    private val channelId = "overlay_channel"

    override fun onCreate() {
        super.onCreate()
        createChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Overlay Booster aktif")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
        startForeground(1002, notification)

        val mode = intent?.getStringExtra("mode") ?: FeatureType.OVERLAY_FPS.name
        removeExistingOverlay()
        when (FeatureType.valueOf(mode)) {
            FeatureType.OVERLAY_CROSSHAIR -> showCrosshair()
            FeatureType.OVERLAY_FPS -> showFpsCounter()
            FeatureType.OVERLAY_PING -> showPingMonitor()
            FeatureType.OVERLAY_MEMORY -> showMemoryMonitor()
            FeatureType.STORAGE_INFO -> showStorageMonitor()
            FeatureType.NETWORK_TYPE_INFO -> showNetworkMonitor()
            else -> {}
        }
        return START_STICKY
    }

    // ---------- Crosshair ----------

    private fun showCrosshair() {
        val tv = TextView(this).apply {
            text = "+"
            textSize = 42f
            setTextColor(0xFF00FF00.toInt())
        }
        addDraggableOverlay(tv, centerOnScreen = true)
    }

    // ---------- FPS ----------

    private fun showFpsCounter() {
        val tv = makeOverlayText("FPS: --")
        addDraggableOverlay(tv)

        var frameCount = 0
        var lastReportTime = System.nanoTime()

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!running) return
                frameCount++
                val elapsed = frameTimeNanos - lastReportTime
                if (elapsed >= 1_000_000_000L) {
                    val fps = frameCount
                    handler.post { tv.text = "FPS: $fps" }
                    frameCount = 0
                    lastReportTime = frameTimeNanos
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)
    }

    // ---------- Ping ----------

    private fun showPingMonitor() {
        val tv = makeOverlayText("Ping: -- ms")
        addDraggableOverlay(tv)

        thread {
            while (running) {
                val ms = measurePingMs("8.8.8.8", 53)
                handler.post { tv.text = if (ms >= 0) "Ping: $ms ms" else "Ping: timeout" }
                Thread.sleep(2000)
            }
        }
    }

    private fun measurePingMs(host: String, port: Int): Long {
        return try {
            val start = System.currentTimeMillis()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 1500)
            }
            System.currentTimeMillis() - start
        } catch (e: Exception) {
            -1
        }
    }

    // ---------- RAM ----------

    private fun showMemoryMonitor() {
        val tv = makeOverlayText("RAM: --")
        addDraggableOverlay(tv)

        thread {
            while (running) {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val info = ActivityManager.MemoryInfo()
                am.getMemoryInfo(info)
                val availMb = info.availMem / (1024 * 1024)
                val totalMb = info.totalMem / (1024 * 1024)
                handler.post { tv.text = "RAM: ${availMb}MB / ${totalMb}MB free" }
                Thread.sleep(2000)
            }
        }
    }

    // ---------- Storage ----------

    private fun showStorageMonitor() {
        val tv = makeOverlayText("Storage: --")
        addDraggableOverlay(tv)

        thread {
            while (running) {
                val stat = StatFs(filesDir.path)
                val availGb = (stat.availableBytes / (1024.0 * 1024 * 1024))
                handler.post { tv.text = "Storage: %.1f GB free".format(availGb) }
                Thread.sleep(5000)
            }
        }
    }

    // ---------- Network Type ----------

    private fun showNetworkMonitor() {
        val tv = makeOverlayText("Net: --")
        addDraggableOverlay(tv)

        thread {
            while (running) {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val net = cm.activeNetwork
                val caps = net?.let { cm.getNetworkCapabilities(it) }
                val type = when {
                    caps == null -> "Tidak ada koneksi"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Seluler"
                    else -> "Lainnya"
                }
                handler.post { tv.text = "Net: $type" }
                Thread.sleep(3000)
            }
        }
    }

    // ---------- Overlay helpers ----------

    private fun makeOverlayText(initial: String): TextView = TextView(this).apply {
        text = initial
        textSize = 14f
        setTextColor(0xFFFFFFFF.toInt())
        setBackgroundColor(0x99000000.toInt())
        setPadding(16, 8, 16, 8)
    }

    private fun addDraggableOverlay(view: View, centerOnScreen: Boolean = false) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = if (centerOnScreen) Gravity.CENTER else Gravity.TOP or Gravity.START
        params.x = 40
        params.y = 120

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager?.updateViewLayout(v, params)
                    true
                }
                else -> false
            }
        }

        windowManager?.addView(view, params)
        overlayView = view
    }

    private fun removeExistingOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {
            }
        }
        overlayView = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Overlay Booster", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        running = false
        removeExistingOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
