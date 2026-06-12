# to-do-list

iOS-Reminders-style Android to-do app. `applicationId = com.shayan.reminderstdl`, label `RemindersTDL`. Kotlin-only (25 `.kt` files, no Java), single-Activity + Jetpack Navigation with 12 fragments, Firebase Auth + Firestore for sync, Room v3 for offline cache, `AlarmManager.setExactAndAllowWhileIdle` for local reminder notifications. AGP 8.7.3, Kotlin 2.0.21, `compileSdk 35`, `minSdk 26`, Java 11, viewBinding, KSP for Room.

The repo had no README before this one.

## 🚨 Things to fix before shipping

### 1. `app/google-services.json` is committed

Firebase project `reminders-4ee51`, project number `348606568459`, Android API key `AIzaSyD-G0ift70Vk4cO8E-tv_TRKVvs7PmhwPA`, package `com.shayan.reminderstdl`. Android API keys aren't strictly secret — Google restricts them to the package + signing certificate — but apply API-key restrictions + Firebase App Check anyway, and stop tracking the file:

```bash
git rm --cached app/google-services.json
echo "**/google-services.json" >> .gitignore
```

### 2. There are no Firestore security rules in this repo

The app reads/writes `Users/{uid}/Tasks/...`. No `firestore.rules` is present. If the project is still on default test-mode rules, every authenticated user can read/write the entire database, and test mode auto-expires after 30 days (after which the app silently breaks). At minimum:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    match /Users/{uid}/{document=**} {
      allow read, write: if request.auth.uid == uid;
    }
  }
}
```

### 3. `.kotlin/` and most of `.idea/` are tracked

`.gitignore` excludes `local.properties`, `/build`, `/captures`, `.gradle`, `*.iml`, `.DS_Store`, and a few cherry-picked `.idea/*.xml` files — but it tracks the rest of `.idea/` and the `.kotlin/` Kotlin compiler build cache. Replace `.gitignore` with the standard Android template (entire `.idea/` excluded, `.kotlin/` excluded) and:

```bash
git rm -r --cached .idea .kotlin
```

### 4. The "iCloud" and "Outlook" sidebar entries are misleading

`HomeFragment` shows iOS-Reminders-style sidebar entries for "iCloud" and "Outlook":

- `app/src/main/java/com/shayan/reminderstdl/ui/fragments/iCloudFragment.kt:28-58` simply observes `viewModel.totalTasks` and renders **every task** — there's no iCloud sync (no CalDAV / EventKit / iCloud account linkage). The label is decorative.
- `app/src/main/java/com/shayan/reminderstdl/ui/fragments/OutlookFragment.kt` is a 29-line stub: it inflates `fragment_outlook.xml` and wires only a back button. **No data, no list.**

Either implement real iCloud / Microsoft Graph integration, or relabel them and remove the dead one.

### 5. `AlarmManagerHelper` does not gate `SCHEDULE_EXACT_ALARM` on Android 12+

`utils/AlarmManagerHelper.kt` calls `AlarmManager.setExactAndAllowWhileIdle` directly. On Android 12+ this throws `SecurityException` if the user has revoked `SCHEDULE_EXACT_ALARM`. Wrap with a `canScheduleExactAlarms()` check and bounce to `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` if denied.

### 6. Room database uses destructive migration

`data/local/AppDatabase.kt` calls `fallbackToDestructiveMigration()`. Every schema bump wipes the user's offline cache. Cloud data in Firestore is preserved, but anything offline (or not yet synced) is destroyed. Write proper `Migration` objects.

## What the app actually does

1. **Launch.** `MainActivity` hosts a `NavHostFragment` and starts at `HostFragment`, which checks `FirebaseAuth.currentUser` and routes to `LoginFragment` or `HomeFragment`.
2. **Auth.** `LoginFragment` / `RegisterFragment` use Firebase Auth email/password. The user's UID becomes the Firestore root key under `Users/{uid}/`. Email is mirrored into a Room `User` row and `SharedPreferences("PrefsDatabase")`.
3. **Dashboard.** `HomeFragment` shows tile counts: **Today, Scheduled, All, Flagged, Completed**, plus the misleading "iCloud" and "Outlook" tiles. Tapping a tile navigates to the matching list fragment.
4. **Create.** `NewReminderFragment` captures title, notes, date, time (Morning / Afternoon / Tonight tag), flag toggle. On Save: writes a Firestore doc under `Users/{uid}/Tasks`, captures the Firestore `documentId` as the task primary key, mirrors the row into Room, and schedules an exact `AlarmManager` alarm. (If no time is chosen, defaults to **11:00 AM** the same day.)
5. **List filters.**
   - `TodayFragment` — today's tasks, grouped Morning/Afternoon/Tonight.
   - `ScheduledFragment` — future-dated tasks grouped by month.
   - `FlaggedFragment` — `flag = true`.
   - `CompletedFragment` — `isCompleted = true`. Has "clear all completed".
   - `AllFragment` — every task. Search by title via Room `LIKE`.
6. **Detail.** `TaskDetailsFragment` is read-only, parcelled in via `kotlin-parcelize`.
7. **Notify.** `TaskReminderReceiver` (a `BroadcastReceiver` registered in the manifest) catches the `AlarmManager` `PendingIntent` and posts a notification on the high-priority "Task Reminders" channel via `utils/Notification.kt`. Tapping the notification opens `MainActivity`.
8. **Sync on launch.** `Repository` pulls every Firestore task and merges into Room (insert if missing, update if `isCompleted` differs).

## Permissions

Declared in `app/src/main/AndroidManifest.xml`:

| Permission | Why |
| --- | --- |
| `INTERNET` | Firebase Auth + Firestore |
| `SCHEDULE_EXACT_ALARM` | Exact reminder alarms on Android 12+ |
| `FOREGROUND_SERVICE` | Declared but **unused** — no foreground service is started; drop |
| `POST_NOTIFICATIONS` | Notification runtime permission on Android 13+ |

## Tech stack

- **AGP** 8.7.3, **Kotlin** 2.0.21, **Java target** 11, **KSP** for Room, **kotlin-parcelize**.
- **compileSdk** 35, **targetSdk** 34, **minSdk** 26.
- **Firebase Auth** 23.1.0, **Firebase Firestore** 25.1.1 (no FCM, no Storage, no Functions).
- **Room** 2.6.1 (runtime + ktx + ksp).
- **Jetpack Navigation** 2.8.5, **Lifecycle ViewModel ktx** 2.8.7, **Coroutines Android** 1.9.0.
- **Material** 1.12.0, **AppCompat**, **ConstraintLayout**, **RecyclerView** 1.3.2, **Activity** + **Fragment** ktx.
- **viewBinding** on. Release: `isMinifyEnabled = true` + `isShrinkResources = true`.
- **No** Hilt / Compose / Retrofit / OkHttp / WorkManager / DataStore.

## Project layout

```
to-do-list/
├── build.gradle.kts                              root build script
├── settings.gradle.kts
├── gradle/libs.versions.toml                     version catalog
├── gradle.properties
├── gradlew, gradlew.bat
├── .gitignore                                    13 lines
├── .idea/                                        🚨 partially tracked
├── .kotlin/                                      🚨 tracked (Kotlin compiler cache)
└── app/
    ├── build.gradle.kts                          applicationId com.shayan.reminderstdl
    ├── google-services.json                      🚨 tracked — Firebase project reminders-4ee51
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/shayan/reminderstdl/
        │   ├── adapters/TaskAdapter.kt
        │   ├── data/local/AppDatabase.kt          Room v3, fallbackToDestructiveMigration()
        │   ├── data/local/dao/{TasksDao, UserDao}.kt
        │   ├── data/models/{Tasks, User}.kt       @Parcelize
        │   ├── data/repository/Repository.kt
        │   ├── receivers/TaskReminderReceiver.kt
        │   ├── ui/MainActivity.kt
        │   ├── ui/fragments/
        │   │   ├── HostFragment.kt                auth-state router
        │   │   ├── LoginFragment.kt, RegisterFragment.kt
        │   │   ├── HomeFragment.kt                tile dashboard
        │   │   ├── NewReminderFragment.kt         create + schedule alarm
        │   │   ├── TodayFragment.kt, ScheduledFragment.kt
        │   │   ├── FlaggedFragment.kt, CompletedFragment.kt, AllFragment.kt
        │   │   ├── TaskDetailsFragment.kt
        │   │   ├── iCloudFragment.kt              ⚠ shows ALL tasks; no real iCloud sync
        │   │   └── OutlookFragment.kt             ⚠ stub layout + back button only
        │   ├── ui/viewmodel/ViewModel.kt
        │   └── utils/{AlarmManagerHelper, Notification}.kt
        └── res/
            ├── layout/                            15 XMLs (activity_main + 13 fragments + reminder item)
            ├── menu/menu_dropdown_toolbar.xml     logout entry
            ├── navigation/nav_graph.xml           12 destinations + fade/slide animations
            ├── values/strings.xml                 75 entries
            ├── xml/{backup_rules, data_extraction_rules}.xml
            └── drawable*/, mipmap-*/              ~28 icons
```

## Setup / run

1. Open in Android Studio (Hedgehog or newer for AGP 8.7.3). Sync Gradle.
2. **Replace `app/google-services.json` with your own Firebase project's file** before running. Apply API-key restrictions + Firebase App Check.
3. **Write Firestore security rules** before any public deployment (see section above).
4. Run on Android 8+ (`minSdk 26`). On Android 12+, grant `SCHEDULE_EXACT_ALARM` in system settings or alarms will fail.
5. `./gradlew :app:assembleDebug`.

## Status

- Working tree clean on `master`. **43 commits**, ~39 of which have the literal message `.`. The two named ones are `notifications` and `transitions` (latest, `3705cd7`). **No GitPulse pollution.**
- Remote: `https://github.com/shayann07/to-do-list.git`. **No `LICENSE` file** — treat as "all rights reserved" until one is committed.
- **No tests.** JUnit + Espresso + AndroidJUnitRunner are declared in `app/build.gradle.kts` but `app/src/test/` and `app/src/androidTest/` contain no test files.
