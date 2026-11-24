# Network Speed Monitor ⚡

[![Android 17](https://img.shields.io/badge/Android%2017-API%2037-3DDC84?style=flat&logo=android)](https://developer.android.com/about/versions/17)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![One UI 9](https://img.shields.io/badge/Samsung%20One%20UI-9.0%20Now%20Bar-1428A0?style=flat&logo=samsung)](https://www.samsung.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweight, modern native Android application that continuously measures and displays real-time, device-wide network throughput (download and upload speeds). Built with Kotlin and Jetpack Compose, featuring native **Android 17 Live Updates**, **`NotificationCompat.MetricStyle`**, dynamic status bar icon rendering, and deep compatibility with **Samsung Galaxy One UI 9.0 (Now Bar)**.

---

## 🌟 Key Features

- **Live Device-Wide Throughput**: Accurately tracks total device network traffic using Android's native `TrafficStats` (`getTotalRxBytes()` and `getTotalTxBytes()`), calculating throughput over exact elapsed intervals via `SystemClock.elapsedRealtime()`.
- **Decimal MB/s**: Calculates speeds in standard decimal MB/s ($1\text{ MB} = 1,000,000\text{ bytes}$) with high accuracy and fractional formatting.
- **Dynamic Status Bar Speed Icon**: Renders live speed values directly onto a custom status bar icon mask in real time, so you can glance at your speed next to the status bar clock without pulling down the notification drawer.
- **Android 17 Live Updates (`MetricStyle`)**: Utilizes the modern Android 17 `NotificationCompat.MetricStyle` template with dedicated Download (critical) and Upload metrics, requesting ongoing promotion via `setRequestPromotedOngoing(true)`.
- **Samsung One UI 9.0 & Now Bar Support**: Specially optimized for Samsung Galaxy devices (e.g. Galaxy M17) running One UI 9.0, including short critical text pills (`setShortCriticalText`), public lock screen visibility, and direct Now Bar settings integration.
- **Ultra-Lightweight Foreground Service**: Powered by Kotlin Coroutines (`Dispatchers.Default`), sampling once per second with virtually zero CPU and RAM usage. No wake locks, no background polling loops, and no network requests.
- **Survives Recents Clearance**: Configured with `START_STICKY` so monitoring persists when minimizing the app, switching apps, turning off the screen, or swiping the app away from Recents.
- **Clean Dark Material 3 UI**: Polished, responsive dark-themed interface built entirely with Jetpack Compose.
- **100% Private & Offline**: Requires **NO** internet access permission, **NO** location access, **NO** analytics, and **NO** floating overlay permissions.

---

## 📱 Screenshots & Previews

| Status Bar Speed Icon | Main Application Screen |
| :---: | :---: |
| *Real-time speed digits in the status bar* | *Real-time Download & Upload metric cards* |

---

## 🏗️ Architecture & Project Structure

The project follows modern Android architecture guidelines with clean separation of concerns:

```
app/src/main/java/com/riyo/networkspeedmonitor/
├── MainActivity.kt               # Edge-to-edge Compose activity
├── model/
│   └── SpeedData.kt              # Speed data models & monotonic math calculator
├── repository/
│   └── SpeedMonitorRepository.kt # Reactive in-memory StateFlow repository
├── service/
│   └── NetworkSpeedMonitorService.kt # Foreground service (specialUse type)
├── notification/
│   └── NotificationHelper.kt     # MetricStyle, Live Update & channel manager
├── util/
│   ├── LiveUpdateHelper.kt       # Promotion & Samsung Now Bar settings helper
│   └── SpeedIconGenerator.kt     # Dynamic monochrome bitmap status bar generator
└── ui/
    ├── SpeedMonitorScreen.kt     # Jetpack Compose UI & status controls
    └── theme/
        ├── Color.kt              # Sleek dark color palette
        ├── Theme.kt              # Material 3 dark theme definition
        └── Type.kt               # Typography styles
```

---

## 🔒 Permissions & Security

This app adheres to the principle of least privilege:

| Permission | Type | Purpose |
| :--- | :--- | :--- |
| `android.permission.FOREGROUND_SERVICE` | Normal | Run persistent background monitoring service |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Normal | Declare continuous device throughput monitoring |
| `android.permission.POST_NOTIFICATIONS` | Runtime (API 33+) | Display ongoing speed notification |
| `android.permission.POST_PROMOTED_NOTIFICATIONS` | Normal (API 36+) | Request Live Update / Now Bar status chip promotion |

> **Zero Network Permission (`android.permission.INTERNET`)**: The app does **not** make network requests or transmit any telemetry. It reads Android's existing local kernel traffic counters.

---

## ⚙️ Samsung One UI 9.0 (Now Bar) Setup

To display the live speed pill in the Samsung Now Bar on One UI 9.0:

1. Open **Settings** on your Samsung device.
2. Navigate to **Lock screen and AOD** > **Now bar**.
3. Tap **Live notifications** (or **See all**).
4. Find **Network Speed Monitor** and switch it **ON**.
5. *(Optional)* Inside the app, tap the **"Open Samsung Now Bar Settings"** shortcut button to jump directly to this menu.

---

## 🚀 Building from Source

### Prerequisites
- **Android Studio**: Ladybug / Meerkat (2024.2+) or newer
- **JDK**: Java 17 or Java 25
- **Android SDK**: Compile SDK `37`, Target SDK `37`, Min SDK `34`

### Build Steps
```bash
# Clone the repository
git clone https://github.com/I-AM-A-PROGRAMER/Network-Speed-Monitor.git
cd Network-Speed-Monitor

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 👨‍💻 Author

**Made by [Supriyo](https://github.com/I-AM-A-PROGRAMER)**

- GitHub: [@I-AM-A-PROGRAMER](https://github.com/I-AM-A-PROGRAMER)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
