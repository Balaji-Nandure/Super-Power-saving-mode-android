# ⚡ Super Power Saving Mode (Android)

An extreme, battery-maximizing Android Launcher & System Optimizer designed to reduce power consumption down to **~10% of standard usage** while keeping **phone calls** and **up to 6 essential apps** fully functional.

---

## 🌟 Key Features

### 1. 🖤 True OLED Black Interface (`#000000`)
* On AMOLED and OLED screens, pure black pixels consume **0 Watts**.
* Eliminates heavy launcher animations, widgets, and GPU load.
* Displays minimal typography for Time, Date, and Battery status.

### 2. 📞 Unrestricted Incoming & Outgoing Calls
* Incoming phone calls, carrier voice calls, and alarms are **never blocked**.
* Dedicated instant emergency phone dialer on the home screen.

### 3. 🔔 Notification Gatekeeper Service
* Intercepts and filters system notifications.
* **Allows notifications ONLY from your 6 selected whitelisted apps** (e.g. WhatsApp, SMS, Work, Banking).
* Cancels and silences background notifications from all other 50+ installed apps, preventing CPU wakeups and screen light-ups.

### 4. ⚡ Hardware Power Optimization Engine
* **Screen Timeout**: Automatically sets screen timeout to 15 seconds.
* **Auto-Dimming**: Clamps screen brightness to minimal levels.
* **Master Sync Off**: Disables background Google & app synchronization while power saving is active.

### 5. 📱 Customizable 6-App Whitelist
* Select and modify your 6 essential apps anytime with a built-in search and picker.

---

## 📥 How to Download & Install the APK

1. Go to the **[Actions tab](https://github.com/Balaji-Nandure/Super-Power-saving-mode-android/actions)** in this repository or the **[Releases](https://github.com/Balaji-Nandure/Super-Power-saving-mode-android/releases)** page.
2. Download the `app-debug.apk` file on your Android device.
3. Tap the file to install it.
4. Open **Super Power Saver** and tap **"Permission Setup"** to grant:
   - **Notification Access**: Enables the Gatekeeper to silence unwanted background app wakeups.
   - **Modify System Settings**: Allows auto-dimming and 15s screen timeout.
5. (Optional) Set **Super Power Saver** as your default home app / launcher when you want extreme battery life.

---

## 🛠️ Tech Stack & Architecture
* **Language**: Kotlin
* **Architecture**: Android Views & Material Components (Optimized for minimal RAM & zero lag)
* **SDK**: Min SDK 24 (Android 7.0+), Target SDK 34 (Android 14)
* **CI/CD**: GitHub Actions (Automatic cloud compilation & release tagging)
