package com.nyxforge.gamebooster

/**
 * Definisi 30 fitur Game Booster.
 * type menentukan aksi nyata apa yang dijalankan saat toggle di-ON.
 */
enum class FeatureType {
    KILL_BACKGROUND,      // matikan proses background non-system
    DND_MODE,              // Do Not Disturb saat gaming
    OVERLAY_CROSSHAIR,     // crosshair overlay untuk FF
    OVERLAY_FPS,           // penghitung FPS mengambang
    OVERLAY_PING,          // monitor ping ke server mengambang
    OVERLAY_MEMORY,        // info RAM mengambang
    LOCK_ROTATION,         // kunci orientasi layar
    LOCK_BRIGHTNESS,       // kunci brightness ke max
    SCREEN_TIMEOUT_MAX,    // set screen timeout maksimal saat main
    SILENT_NOTIF,          // matikan notifikasi popup
    VIBRATION_OFF,         // matikan getar sistem
    AUTO_ROTATE_OFF,       // matikan auto rotate
  BATTERY_SAVER_OFF,     // matikan battery saver (biar performa max)
    OPEN_APP_SETTINGS,     // shortcut ke App Info game (buat batasi data BG)
    OPEN_DATA_SAVER,       // shortcut ke Data Saver settings
    OPEN_BATTERY_OPT,      // shortcut ke battery optimization exemption
    OPEN_WIFI_SETTINGS,    // shortcut wifi settings (cek sinyal)
    OPEN_DEV_OPTIONS,      // shortcut developer options
    CLEAR_CLIPBOARD,       // bersihkan clipboard
    LAUNCH_TARGET_GAME,    // shortcut langsung buka game target
    FORCE_STOP_LIST,       // buka halaman force-stop utk app berat custom
    SCREEN_RECORD_BLOCK_INFO, // info/reminder toggle screen record block
    QUICK_SETTINGS_TILE_INFO, // info cara pasang quick tile booster
    NOTIFICATION_LED_OFF,  // shortcut matikan LED notif (jika tersedia OEM)
    GPS_OFF_SHORTCUT,      // shortcut ke location settings
    BLUETOOTH_OFF_SHORTCUT,// shortcut bluetooth settings
    ADAPTIVE_BATTERY_INFO, // shortcut adaptive battery settings
    STORAGE_INFO,          // info storage mengambang
    NETWORK_TYPE_INFO,     // info tipe jaringan (4G/5G/WIFI) mengambang
    PERFORMANCE_MODE_INFO  // reminder aktifkan Game Mode / Performance Mode bawaan HP
}

data class BoosterFeature(
    val id: Int,
    val title: String,
    val description: String,
    val type: FeatureType,
    var enabled: Boolean = false,
    val needsOverlayPermission: Boolean = false
)

object FeatureCatalog {
    fun buildDefaultList(): MutableList<BoosterFeature> = mutableListOf(
        BoosterFeature(1, "Kill Background Apps", "Matikan proses aplikasi lain yang jalan di background", FeatureType.KILL_BACKGROUND),
        BoosterFeature(2, "Mode Fokus Gaming (DND)", "Blokir notifikasi & telepon masuk saat main", FeatureType.DND_MODE),
        BoosterFeature(3, "Crosshair Overlay (FF)", "Tampilkan crosshair mengambang di atas game", FeatureType.OVERLAY_CROSSHAIR, needsOverlayPermission = true),
        BoosterFeature(4, "FPS Counter Overlay", "Tampilkan estimasi FPS real-time mengambang", FeatureType.OVERLAY_FPS, needsOverlayPermission = true),
        BoosterFeature(5, "Ping Monitor Overlay", "Tampilkan ping ke server (ms) mengambang", FeatureType.OVERLAY_PING, needsOverlayPermission = true),
        BoosterFeature(6, "RAM Monitor Overlay", "Tampilkan sisa RAM mengambang", FeatureType.OVERLAY_MEMORY, needsOverlayPermission = true),
        BoosterFeature(7, "Kunci Orientasi Layar", "Kunci layar ke landscape/portrait", FeatureType.LOCK_ROTATION),
        BoosterFeature(8, "Kunci Brightness Maksimal", "Set brightness ke maksimal otomatis", FeatureType.LOCK_BRIGHTNESS),
        BoosterFeature(9, "Screen Timeout Maksimal", "Set layar tidak cepat mati saat main", FeatureType.SCREEN_TIMEOUT_MAX),
        BoosterFeature(10, "Silent Notifikasi Popup", "Matikan heads-up notification saat main", FeatureType.SILENT_NOTIF),
        BoosterFeature(11, "Matikan Getar Sistem", "Nonaktifkan vibration feedback sistem", FeatureType.VIBRATION_OFF),
        BoosterFeature(12, "Matikan Auto-Rotate", "Kunci rotasi otomatis layar", FeatureType.AUTO_ROTATE_OFF),
        BoosterFeature(13, "Cek Battery Saver", "Buka setting battery saver (matikan biar FPS stabil)", FeatureType.BATTERY_SAVER_OFF),
        BoosterFeature(14, "Batasi Data BG Game", "Buka App Info target game utk atur data background", FeatureType.OPEN_APP_SETTINGS),
        BoosterFeature(15, "Data Saver Settings", "Buka pengaturan Data Saver sistem", FeatureType.OPEN_DATA_SAVER),
        BoosterFeature(16, "Battery Optimization", "Kecualikan game dari battery optimization", FeatureType.OPEN_BATTERY_OPT),
        BoosterFeature(17, "Cek Sinyal WiFi", "Buka pengaturan WiFi untuk cek kekuatan sinyal", FeatureType.OPEN_WIFI_SETTINGS),
        BoosterFeature(18, "Developer Options", "Buka developer options (GPU rendering dll)", FeatureType.OPEN_DEV_OPTIONS),
        BoosterFeature(19, "Bersihkan Clipboard", "Kosongkan clipboard biar RAM lega", FeatureType.CLEAR_CLIPBOARD),
        BoosterFeature(20, "Quick Launch Game", "Langsung buka game target yang dipilih", FeatureType.LAUNCH_TARGET_GAME),
        BoosterFeature(21, "Force Stop Manager", "Buka daftar app utk di-force-stop manual", FeatureType.FORCE_STOP_LIST),
        BoosterFeature(22, "Reminder Blokir Screen Record", "Info cara cegah orang lain rekam layar via cast", FeatureType.SCREEN_RECORD_BLOCK_INFO),
        BoosterFeature(23, "Quick Settings Tile", "Panduan pasang tile Booster di quick settings", FeatureType.QUICK_SETTINGS_TILE_INFO),
        BoosterFeature(24, "Matikan LED Notifikasi", "Buka setting LED notifikasi (jika OEM support)", FeatureType.NOTIFICATION_LED_OFF),
        BoosterFeature(25, "Matikan GPS", "Buka location settings, matikan GPS saat main", FeatureType.GPS_OFF_SHORTCUT),
        BoosterFeature(26, "Matikan Bluetooth", "Buka bluetooth settings buat dimatikan", FeatureType.BLUETOOTH_OFF_SHORTCUT),
        BoosterFeature(27, "Adaptive Battery", "Buka setting adaptive battery (matikan saat main)", FeatureType.ADAPTIVE_BATTERY_INFO),
        BoosterFeature(28, "Storage Monitor Overlay", "Tampilkan sisa storage mengambang", FeatureType.STORAGE_INFO, needsOverlayPermission = true),
        BoosterFeature(29, "Info Jaringan Overlay", "Tampilkan tipe jaringan aktif mengambang", FeatureType.NETWORK_TYPE_INFO, needsOverlayPermission = true),
        BoosterFeature(30, "Reminder Game Mode HP", "Ingatkan aktifkan Game Mode/Performance Mode bawaan HP", FeatureType.PERFORMANCE_MODE_INFO)
    )
}
