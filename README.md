# ⚡ Super Power Saving Mode (Android & Windows)

An extreme, battery-maximizing Android Launcher & System Optimizer designed to reduce power consumption down to **~10% of standard usage** while keeping **phone calls**, **keyboards (Gboard/IME)**, and **up to 25 essential apps & notifications** fully functional.

---

## 🌟 Key Features

### 1. 📱 Customizable 25-App Whitelist + Default Keyboard Protection
* Whitelist and run up to **25 essential apps** (e.g. WhatsApp, Email, Slack, Banking, Maps, Browser).
* **Keyboard Whitelisting**: Google Keyboard (Gboard), Samsung Keyboard, Vivo IME, and SwiftKey are **always whitelisted by default** so you can type freely in any app without being blocked.
* **Notification Gatekeeper**: Filters background notifications, allowing only your selected 25 apps to notify you and wake the CPU.

### 2. ⚙️ Granular Hardware & Services Control Matrix
* **Individual Service Toggles**: Control every power-hungry hardware component individually:
  - 🌐 **Mobile Data / 5G Radio**: Stops background cellular data transfers & 5G tower hunting.
  - 📶 **Wi-Fi & Wi-Fi Scan Throttling**: Disables Wi-Fi radio & 24/7 background location scanning.
  - 📡 **Bluetooth & BLE Scan**: Disables Bluetooth radio & continuous BLE beacon discovery.
  - 🎮 **GPU Animation Engine**: Sets window & transition animation scale to 0.0x (Adreno 660 GPU bypass).
  - ⚡ **CPU Aggressive Freezer**: Continuously kills background tasks to stop Cortex-X1 core wakeups.
  - 🎯 **120Hz ➔ 60Hz LTPO Refresh Rate Clamp**: Reduces display refresh rate on AMOLED panels.
  - 📍 **GPS / Location Services**: Halts high-power GNSS satellite tracking.
  - 📳 **Haptic Motors & Vibration**: Kills keyboard and touch feedback actuators.
  - 🛑 **Motion Sensors & Step Counter**: Freezes gyroscope/accelerometer to prevent pocket wakelocks.
  - 🔕 **Auto-Sync & Cloud Push**: Disables Google Master Sync and background push listeners.

### 3. 📱 Vivo X70 Pro+ & FuntouchOS Deep Optimization Center
* **Vivo `iManager` Direct Launcher**: Quickly configure "High Background Power Consumption" per-app whitelist.
* **Vivo Autostart Manager**: Block non-essential third-party apps from waking the CPU.
* **5G ➔ 4G LTE Switcher**: Cuts modem standby power by ~30–40% on Snapdragon 888+.
* **WQHD+ ➔ FHD+ Resolution Shortcut**: Switch to 1080p to reduce GPU frame composition load.

### 4. 🖤 True OLED Black Interface (`#000000`)
* On AMOLED and OLED screens, pure black pixels consume **0 Watts**.
* Eliminates heavy launcher animations, widgets, and GPU load.
* Displays minimal typography for Time, Date, and Battery status.

### 5. 📞 Unrestricted Incoming & Outgoing Calls
* Incoming phone calls, carrier voice calls, and alarms are **never blocked**.
* Dedicated instant emergency phone dialer on the home screen.

### 6. 🔴 1% Extreme Blackout Survivor Mode
* Single-tap activation for emergency battery survival: calls & SMS only, monochrome grayscale, frozen motion sensors, and 10-second timeout while preserving loud ringtones.

---

## 📥 How to Download & Install the APK

1. Go to the **[Actions tab](https://github.com/Balaji-Nandure/Super-Power-saving-mode-android/actions)** in this repository or the **[Releases](https://github.com/Balaji-Nandure/Super-Power-saving-mode-android/releases)** page.
2. Download the `app-debug.apk` file on your Android device.
3. Tap the file to install it.
4. Open **Super Power Saver** and tap **"OS Takeover Setup"** to grant:
   - **Notification Access**: Enables Gatekeeper to silence unwanted background app wakeups.
   - **Modify System Settings**: Allows auto-dimming and 15s screen timeout.
   - **Accessibility Kiosk**: Prevents unapproved apps from opening in the background.
5. Tap **"⚡ Services Power Matrix"** to customize individual hardware toggles for your device.

---

## 🛠️ Tech Stack & Architecture
* **Language**: Kotlin
* **Architecture**: Android Views & Material Components (Optimized for minimal RAM & zero lag)
* **SDK**: Min SDK 24 (Android 7.0+), Target SDK 34 (Android 14)
* **CI/CD**: GitHub Actions (Automatic cloud compilation & release tagging)
