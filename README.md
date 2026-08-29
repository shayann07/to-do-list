# Reminders TDL (to-do-list)

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Repository-blue?style=for-the-badge)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Storage-Room%20%2B%20Firestore-FFA000?style=for-the-badge&logo=firebase&logoColor=white)](https://firebase.google.com/docs/firestore)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-00BCD4?style=for-the-badge)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-00BCD4?style=for-the-badge)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

> An iOS Reminders-inspired Android task management and scheduling system featuring real-time Cloud Firestore synchronization, offline-first Room database persistence, and precision background alarms via AlarmManager.

---

## 📖 Overview

**Reminders TDL** brings Apple’s iconic iOS Reminders user experience to the Android platform with native Material components and modern Android architecture. Engineered for high productivity and daily utility, the app bridges local offline-first reliability with real-time cloud synchronization. 

Users can organize their personal and professional commitments into intuitive smart filter categories (**Today**, **Scheduled**, **All**, **Flagged**, **Completed**, and custom group views like **iCloud** and **Outlook**), configure granular due dates and times, attach contextual notes, and receive timely push notifications triggered by Android's exact alarm scheduling infrastructure.

### Why Reminders TDL?
- **Familiar iOS Aesthetic**: Polished grid layout featuring colored badge tiles and dynamic item counts.
- **Offline-First Resilience**: Tasks are instantly persisted to a local SQLite database via Room DAO and synced asynchronously with Firebase Cloud Firestore.
- **Precision Notifications**: Leverages `AlarmManager` exact alarms and custom `BroadcastReceiver` broadcasts to trigger alert notifications precisely when tasks fall due.
- **Reactive State Flow**: Uses Kotlin Coroutines and reactive `Flow` queries to update UI counters and list views in real time.

---

## 🏗️ Architecture & Data Flow

Reminders TDL strictly follows the recommended **Android Jetpack MVVM (Model-View-ViewModel)** and **Repository pattern**.

```mermaid
flowchart TD
    subgraph UI_Layer [UI Layer - Jetpack Navigation]
        Home[HomeFragment - Smart Categories]
        Lists[Today / Scheduled / All / Flagged / Completed]
        Details[NewReminderFragment / TaskDetailsFragment]
        Auth[LoginFragment / RegisterFragment]
    end

    subgraph ViewModel_Layer [State & Presentation]
        VM[TaskViewModel / ViewModel]
        State[StateFlow & LiveData Streams]
    end

    subgraph Data_Layer [Data & Synchronization Layer]
        Repo[Repository Pattern]
        RoomDB[(Room Local DB\ntasks_table / user_table)]
        Firestore[(Cloud Firestore\nUsers/{uid}/Tasks)]
        FirebaseAuth[Firebase Auth API]
    end

    subgraph Scheduling_Layer [Notification & Background Alarms]
        AlarmHelper[AlarmManagerHelper]
        AlarmMgr[Android AlarmManager]
        Receiver[TaskReminderReceiver]
        NotifHelper[Notification System]
    end

    Home --> VM
    Lists --> VM
    Details --> VM
    Auth --> VM

    VM --> State
    VM --> Repo

    Repo <--> RoomDB
    Repo <--> Firestore
    Repo <--> FirebaseAuth

    Details --> AlarmHelper
    AlarmHelper --> AlarmMgr
    AlarmMgr -.->|Fires Exact Alarm| Receiver
    Receiver --> NotifHelper
```

---

## ✨ Core Features

### 📅 Smart Category Dashboard
- **Today**: Automatically aggregates tasks scheduled for the current calendar date (`yyyy-MM-dd`).
- **Scheduled**: Queries future tasks within custom date ranges with real-time badge counts.
- **All Incomplete**: Overview of all active tasks across all categories.
- **Flagged**: Instant access to high-priority items with custom flag markers.
- **Completed**: History log tracking task completion timestamps (`dateCompleted`) with one-click purge.
- **Custom Lists**: Dedicated workspaces for categorized organizational streams (iCloud / Outlook).

### 🔄 Dual Persistence & Cloud Sync
- **Local Room Database**: Embedded SQLite schema via Room ORM ensuring zero latency and full offline operation.
- **Firebase Cloud Firestore**: Cloud document storage structured under `/Users/{uid}/Tasks` for cross-device consistency.
- **Conflict Resolution**: Bidirectional updates ensuring local Room state reflects Firestore changes seamlessly.

### ⏰ Exact Alarm & Notification Engine
- **AlarmManagerHelper**: Registers exact alarms (`SCHEDULE_EXACT_ALARM`) for specified dates and time slots.
- **TaskReminderReceiver**: Wakes the device to post high-priority heads-up notifications with direct deep links to the task details.
- **Notification Channels**: Modern Android 8.0+ notification channel configuration.

### 🔐 User Authentication & Session Management
- **Firebase Auth**: Secure Email & Password registration and sign-in.
- **Local User Cache**: Persists user profiles in local Room database for quick offline validation.

---

## 📱 Key Screens & Modules

| Screen / Component | Class | Description |
|---|---|---|
| **Host Splash** | `HostFragment` | Checks authentication state and routes users to Auth or Home. |
| **Home Dashboard** | `HomeFragment` | Displays iOS-style smart cards with dynamic badges and search bar. |
| **Authentication** | `LoginFragment` & `RegisterFragment` | Firebase Auth credential inputs with input validation. |
| **Today View** | `TodayFragment` | Interactive list of items due today with checkbox toggle. |
| **Scheduled View** | `ScheduledFragment` | Chronologically organized list of upcoming reminders. |
| **All Tasks** | `AllFragment` | Comprehensive list of all pending tasks. |
| **Flagged View** | `FlaggedFragment` | Priority view filtered by `flag == true`. |
| **Completed View**| `CompletedFragment` | Completed task archive with option to clear completed history. |
| **Create Task** | `NewReminderFragment` | Form with title, notes, DatePicker, TimePicker, and flag toggle. |
| **Task Details** | `TaskDetailsFragment` | View and edit task parameters, notes, and scheduled alert times. |

---

## 🛠️ Technical Stack Matrix

| Layer / Concern | Technology / Library | Version / Details |
|---|---|---|
| **Language** | [Kotlin](https://kotlinlang.org/) | 1.9+ |
| **Min / Target SDK** | Android SDK | `minSdk 26` (Android 8.0) / `targetSdk 34` (Android 14) / `compileSdk 35` |
| **Architecture** | MVVM + Repository Pattern | Android Jetpack Architecture Components |
| **UI Components** | AndroidX & Material 3 | CardViews, RecyclerViews, ViewBinding, Navigation Graph |
| **Local Database** | [Room Persistence Library](https://developer.android.com/training/data-storage/room) | SQLite ORM via Kotlin Symbol Processing (KSP) |
| **Cloud Backend** | [Firebase Firestore](https://firebase.google.com/docs/firestore) | Real-time NoSQL cloud database |
| **Authentication** | [Firebase Auth](https://firebase.google.com/docs/auth) | Email/Password user management |
| **Concurrency** | Kotlin Coroutines & Flow | Asynchronous IO dispatching & reactive database streams |
| **Background Scheduling** | Android `AlarmManager` | Exact alarms & `BroadcastReceiver` notification triggers |
| **Build System** | Gradle Kotlin DSL (`build.gradle.kts`) | AGP 8.7+ with KSP annotation processing |

---

## 📂 Project Structure

```text
to-do-list/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/shayan/reminderstdl/
│   │   │   │   ├── adapters/
│   │   │   │   │   └── TaskAdapter.kt             # RecyclerView adapter with check listener
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt         # Room database singleton
│   │   │   │   │   │   └── dao/
│   │   │   │   │   │       ├── TasksDao.kt        # Room task query definitions & Flows
│   │   │   │   │   │       └── UserDao.kt         # User profile Room DAO
│   │   │   │   │   ├── models/
│   │   │   │   │   │   ├── Tasks.kt               # Task entity with Firestore mappings
│   │   │   │   │   │   └── User.kt                # User entity
│   │   │   │   │   └── repository/
│   │   │   │   │       └── Repository.kt          # Dual-source data repository
│   │   │   │   ├── receivers/
│   │   │   │   │   └── TaskReminderReceiver.kt    # BroadcastReceiver for alarms
│   │   │   │   ├── ui/
│   │   │   │   │   ├── MainActivity.kt            # Single Activity container
│   │   │   │   │   ├── fragments/                 # Navigation destination fragments
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       └── ViewModel.kt           # Shared ViewModel
│   │   │   │   └── utils/
│   │   │   │       ├── AlarmManagerHelper.kt      # Exact alarm scheduler
│   │   │   │       └── Notification.kt            # Notification builder helper
│   │   │   ├── res/
│   │   │   │   ├── layout/                        # XML view layouts
│   │   │   │   ├── navigation/nav_graph.xml       # Jetpack Navigation routing graph
│   │   │   │   └── values/                        # Colors, styles, themes
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── LICENSE
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Hedgehog 2023.1.1 or newer recommended).
- **JDK 11** or **JDK 17**.
- An active **Firebase Project** with **Firestore Database** and **Email/Password Auth** enabled.
- A physical Android device or Emulator running **Android 8.0 (API level 26)** or higher.

### Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/shayann07/to-do-list.git
   cd to-do-list
   ```

2. **Add Firebase Configuration**:
   - Download your `google-services.json` from the [Firebase Console](https://console.firebase.google.com/).
   - Place the file inside the `app/` directory:
     ```text
     to-do-list/app/google-services.json
     ```

3. **Build the project**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on Device / Emulator**:
   - Install and launch the app via Android Studio or run:
     ```bash
     ./gradlew installDebug
     ```

> [!NOTE]
> On devices running Android 12 (API 31) and higher, ensure you grant the **Schedule Exact Alarms** permission in the system settings (`Settings -> Apps -> Reminders TDL -> Alarms & Reminders`) for alarm notifications to dispatch with millisecond accuracy.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for complete details.

```text
Copyright (c) 2026 shayann07
```
