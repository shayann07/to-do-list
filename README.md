# to-do-list

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)]()
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> An iOS-Reminders-style to-do app for Android â€” create reminders with dates and times, sync to the cloud, and get local notifications when each one is due.

---

## 📖 Overview

An iOS-Reminders-style to-do app for Android â€” create reminders with dates and times, sync to the cloud, and get local notifications when each one is due.

---

## ✨ Key Features

- **Firebase**: Built-in support and optimized flows for firebase.
- **Ios Style Ui**: Built-in support and optimized flows for ios style ui.
- **Notifications**: Built-in support and optimized flows for notifications.

---

## 🛠️ Technology Stack

| Component / Layer | Technology |
|---|---|
| **Platform** | Android |
| **Primary Language** | Kotlin |
| **Architecture** | MVVM / Clean Architecture |
| **License** | Open Source (MIT) |

---

## 🚀 Getting Started

1. Open in Android Studio (Hedgehog or newer for AGP 8.7.3). Sync Gradle.
2. **Replace `app/google-services.json` with your own Firebase project's file** before running. Apply API-key restrictions + Firebase App Check.
3. **Write Firestore security rules** before any public deployment (see section above).
4. Run on Android 8+ (`minSdk 26`). On Android 12+, grant `SCHEDULE_EXACT_ALARM` in system settings or alarms will fail.
5. `./gradlew :app:assembleDebug`.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — Copyright (c) 2026 [shayann07](https://github.com/shayann07).
