# Game Booster FF — Nyxforge Build

Android project (Kotlin) — game booster dengan 30 fitur, overlay crosshair,
FPS monitor, ping monitor, dan shortcut optimasi sistem. Tanpa root.

## CATATAN PENTING

- Project ini adalah **source code**, bukan APK jadi. Harus di-build dulu
  pakai Android Studio.
- Tidak ada API key eksternal yang dibutuhkan — semua fitur jalan lokal
  di device (ping pakai socket ke 8.8.8.8, bukan API pihak ketiga).
- Fitur "Crosshair Overlay" adalah overlay visual biasa (menggambar simbol
  di layar), **bukan** aimbot/cheat yang membaca memori game. Tetap ada
  risiko dianggap third-party overlay oleh sistem anti-cheat Garena FF —
  tanggung jawab pengguna.
- "Kill Background Apps" pada Android modern (8+) punya efek terbatas
  karena pembatasan sistem (Doze/App Standby) — ini best-effort, sama
  seperti booster app lain di Play Store non-root.

## CARA BUILD

1. Install [Android Studio](https://developer.android.com/studio) (terbaru).
2. Buka folder project ini lewat **File > Open**.
3. Tunggu Gradle sync selesai (butuh koneksi internet, otomatis download
   dependency).
4. Sambungkan HP Android (USB debugging aktif) atau pakai emulator.
5. Klik **Run** (▶) atau **Build > Build Bundle(s)/APK(s) > Build APK(s)**.
6. APK hasil build ada di `app/build/outputs/apk/debug/app-debug.apk`.
7. Copy APK ke HP, install manual (aktifkan "Install dari sumber tidak
   dikenal" di Settings).

## IZIN YANG PERLU DIAKTIFKAN MANUAL DI HP

- **Tampil di atas aplikasi lain** (Overlay) — untuk crosshair/FPS/ping/RAM
  overlay. Diminta otomatis saat toggle fitur overlay pertama kali.
- **Akses DND** (Do Not Disturb) — untuk Mode Fokus Gaming.
- **Notifikasi** — untuk foreground service notification (Android 13+).

## STRUKTUR PROJECT

```
GameBoosterFF/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/nyxforge/gamebooster/
│       │   ├── MainActivity.kt        # UI utama + pilih game + list fitur
│       │   ├── BoosterFeature.kt      # definisi 30 fitur
│       │   ├── FeatureAdapter.kt      # RecyclerView adapter
│       │   ├── BoosterService.kt      # foreground service kill background
│       │   └── OverlayService.kt      # crosshair/FPS/ping/RAM overlay
│       └── res/
│           ├── layout/
│           └── values/
├── build.gradle
└── settings.gradle
```

## DAFTAR 30 FITUR

1. Kill Background Apps
2. Mode Fokus Gaming (DND)
3. Crosshair Overlay (FF)
4. FPS Counter Overlay
5. Ping Monitor Overlay
6. RAM Monitor Overlay
7. Kunci Orientasi Layar
8. Kunci Brightness Maksimal
9. Screen Timeout Maksimal
10. Silent Notifikasi Popup
11. Matikan Getar Sistem
12. Matikan Auto-Rotate
13. Cek Battery Saver
14. Batasi Data BG Game
15. Data Saver Settings
16. Battery Optimization Exemption
17. Cek Sinyal WiFi
18. Developer Options
19. Bersihkan Clipboard
20. Quick Launch Game
21. Force Stop Manager
22. Reminder Blokir Screen Record
23. Panduan Quick Settings Tile
24. Matikan LED Notifikasi
25. Matikan GPS
26. Matikan Bluetooth
27. Adaptive Battery
28. Storage Monitor Overlay
29. Info Jaringan Overlay
30. Reminder Game Mode HP

## TODO / PENGEMBANGAN LANJUTAN

- Tambah ic_launcher (mipmap) — saat ini pakai default Android Studio,
  ganti manual saat generate project baru atau tambahkan asset sendiri.
- Kalau mau app kill background lebih agresif, butuh root + shell command
  (`am kill`), di luar scope non-root build ini.
