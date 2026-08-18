package com.nyxforge.gamebooster

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nyxforge.gamebooster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var features: MutableList<BoosterFeature>
    private lateinit var adapter: FeatureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("booster_prefs", Context.MODE_PRIVATE)
        features = FeatureCatalog.buildDefaultList()
        restoreToggleState()

        binding.txtSelectedGame.text = prefs.getString("target_game_name", "Belum ada game dipilih")

        binding.btnPilihGame.setOnClickListener { showGamePicker() }

        adapter = FeatureAdapter(features) { feature, isChecked ->
            onFeatureToggled(feature, isChecked)
        }
        binding.recyclerFeatures.layoutManager = LinearLayoutManager(this)
        binding.recyclerFeatures.adapter = adapter

        binding.btnStartBooster.setOnClickListener {
            val i = Intent(this, BoosterService::class.java)
            startForegroundService(i)
            Toast.makeText(this, "Booster service aktif", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreToggleState() {
        for (f in features) {
            f.enabled = prefs.getBoolean("feature_${f.id}", false)
        }
    }

    private fun saveToggleState(feature: BoosterFeature) {
        prefs.edit().putBoolean("feature_${feature.id}", feature.enabled).apply()
    }

    // ---------- Game Picker ----------

    private fun showGamePicker() {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(pm).toString() }

        val names = apps.map { it.loadLabel(pm).toString() }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Pilih game target")
            .setItems(names) { _, which ->
                val chosen = apps[which]
                val pkg = chosen.activityInfo.packageName
                val label = chosen.loadLabel(pm).toString()
                prefs.edit()
                    .putString("target_game_package", pkg)
                    .putString("target_game_name", label)
                    .apply()
                binding.txtSelectedGame.text = label
            }
            .show()
    }

    // ---------- Feature Actions ----------

    private fun onFeatureToggled(feature: BoosterFeature, isChecked: Boolean) {
        feature.enabled = isChecked
        saveToggleState(feature)
        if (!isChecked) return

        if (feature.needsOverlayPermission && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }

        when (feature.type) {
            FeatureType.KILL_BACKGROUND -> killBackgroundApps()
            FeatureType.DND_MODE -> requestDndAccess()
            FeatureType.OVERLAY_CROSSHAIR,
            FeatureType.OVERLAY_FPS,
            FeatureType.OVERLAY_PING,
            FeatureType.OVERLAY_MEMORY,
            FeatureType.STORAGE_INFO,
            FeatureType.NETWORK_TYPE_INFO -> toggleOverlay(feature)
            FeatureType.LOCK_ROTATION -> openIntent(Settings.ACTION_DISPLAY_SETTINGS)
            FeatureType.LOCK_BRIGHTNESS -> openIntent(Settings.ACTION_DISPLAY_SETTINGS)
            FeatureType.SCREEN_TIMEOUT_MAX -> openIntent(Settings.ACTION_DISPLAY_SETTINGS)
            FeatureType.SILENT_NOTIF -> openIntent(Settings.ACTION_NOTIFICATION_SETTINGS)
            FeatureType.VIBRATION_OFF -> openIntent(Settings.ACTION_SOUND_SETTINGS)
            FeatureType.AUTO_ROTATE_OFF -> openIntent(Settings.ACTION_DISPLAY_SETTINGS)
            FeatureType.BATTERY_SAVER_OFF -> openIntent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            FeatureType.OPEN_APP_SETTINGS -> openTargetGameAppInfo()
            FeatureType.OPEN_DATA_SAVER -> openIntent("android.settings.DATA_SAVER_SETTINGS")
            FeatureType.OPEN_BATTERY_OPT -> openIntent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            FeatureType.OPEN_WIFI_SETTINGS -> openIntent(Settings.ACTION_WIFI_SETTINGS)
            FeatureType.OPEN_DEV_OPTIONS -> openIntent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            FeatureType.CLEAR_CLIPBOARD -> clearClipboard()
            FeatureType.LAUNCH_TARGET_GAME -> launchTargetGame()
            FeatureType.FORCE_STOP_LIST -> openIntent(Settings.ACTION_APPLICATION_SETTINGS)
            FeatureType.SCREEN_RECORD_BLOCK_INFO -> showInfo("Android tidak izinkan blokir screen-record pihak ketiga tanpa root. Gunakan mode privasi bawaan game jika tersedia.")
            FeatureType.QUICK_SETTINGS_TILE_INFO -> showInfo("Tambahkan tile 'Game Booster' lewat panel Quick Settings > Edit (ikon pensil) > drag tile ke atas.")
            FeatureType.NOTIFICATION_LED_OFF -> openIntent(Settings.ACTION_NOTIFICATION_SETTINGS)
            FeatureType.GPS_OFF_SHORTCUT -> openIntent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            FeatureType.BLUETOOTH_OFF_SHORTCUT -> openIntent(Settings.ACTION_BLUETOOTH_SETTINGS)
            FeatureType.ADAPTIVE_BATTERY_INFO -> openIntent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            FeatureType.PERFORMANCE_MODE_INFO -> showInfo("Cek pengaturan bawaan HP: Game Mode / Game Turbo / Performance Mode (nama beda tiap merk HP).")
        }
    }

    private fun killBackgroundApps() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = packageManager
        val protected = setOf(packageName, "android", "com.android.systemui")
        val targetGame = prefs.getString("target_game_package", null)

        val runningApps = am.runningAppProcesses ?: emptyList()
        var killed = 0
        for (proc in runningApps) {
            val pkg = proc.pkgList.firstOrNull() ?: continue
            if (pkg in protected || pkg == targetGame) continue
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue
                am.killBackgroundProcesses(pkg)
                killed++
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }
        Toast.makeText(this, "Selesai kill background: $killed app", Toast.LENGTH_SHORT).show()
    }

    private fun requestDndAccess() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            Toast.makeText(this, "Izinkan akses DND untuk Game Booster", Toast.LENGTH_LONG).show()
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            Toast.makeText(this, "Mode Fokus Gaming aktif", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
        Toast.makeText(this, "Izinkan 'Tampil di atas aplikasi lain' lalu aktifkan lagi togglenya", Toast.LENGTH_LONG).show()
    }

    private fun toggleOverlay(feature: BoosterFeature) {
        val i = Intent(this, OverlayService::class.java)
        i.putExtra("mode", feature.type.name)
        startForegroundService(i)
    }

    private fun openIntent(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Toast.makeText(this, "Setting tidak tersedia di device ini", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTargetGameAppInfo() {
        val pkg = prefs.getString("target_game_package", null)
        if (pkg == null) {
            Toast.makeText(this, "Pilih game target dulu", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$pkg")
        startActivity(intent)
    }

    private fun launchTargetGame() {
        val pkg = prefs.getString("target_game_package", null)
        if (pkg == null) {
            Toast.makeText(this, "Pilih game target dulu", Toast.LENGTH_SHORT).show()
            return
        }
        val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) startActivity(launchIntent)
    }

    private fun clearClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.clearPrimaryClip()
        Toast.makeText(this, "Clipboard dibersihkan", Toast.LENGTH_SHORT).show()
    }

    private fun showInfo(msg: String) {
        AlertDialog.Builder(this).setMessage(msg).setPositiveButton("OK", null).show()
    }
}
