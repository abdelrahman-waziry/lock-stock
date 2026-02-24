# Lock Widget — Flutter App

A Flutter app that lets users lock their Android screen either from inside the app
or directly from a home-screen widget — no root required.

---

## How it works

| Layer | Mechanism |
|---|---|
| In-app lock button | `MethodChannel` → `DevicePolicyManager.lockNow()` |
| Home-screen widget button | `BroadcastReceiver` → `DevicePolicyManager.lockNow()` |
| Device Admin | `LockDeviceAdminReceiver` with `<force-lock />` policy |

The widget locks the screen **without opening the app** as long as Device Admin is
already granted. If it hasn't been granted yet, tapping the widget opens the app so
the user can grant it.

---

## Project structure

```
lock_widget_app/
├── lib/
│   └── main.dart                          # Flutter UI + MethodChannel client
│
└── android/app/src/main/
    ├── AndroidManifest.xml
    ├── kotlin/com/example/lockwidgetapp/
    │   ├── MainActivity.kt                # MethodChannel host (isAdminActive, requestAdmin, lockScreen)
    │   ├── LockDeviceAdminReceiver.kt     # Device Admin callbacks
    │   └── LockWidgetProvider.kt          # AppWidgetProvider — handles widget taps
    └── res/
        ├── drawable/
        │   ├── ic_lock.xml                # Lock icon (vector)
        │   └── widget_background.xml      # Rounded indigo background
        ├── layout/
        │   └── lock_widget_layout.xml     # Widget UI
        ├── values/
        │   └── strings.xml
        └── xml/
            ├── device_admin.xml           # Declares force-lock policy
            └── lock_widget_info.xml       # AppWidget metadata
```

---

## Setup steps

### 1. Create the Flutter project

```bash
flutter create --org com.example lock_widget_app
cd lock_widget_app
```

Replace the generated files with the ones provided here.

### 2. Update `pubspec.yaml`

```yaml
dependencies:
  flutter:
    sdk: flutter
  home_widget: ^0.7.0
```

Then run:

```bash
flutter pub get
```

### 3. Place the native files

Copy every file under `android/` into your project, preserving the directory structure.

> **Package name**: The code uses `com.example.lockwidgetapp`. If you change it,
> do a global find-and-replace across all Kotlin files and the manifest.

### 4. Run the app

```bash
flutter run
```

### 5. Grant Device Admin (one-time)

Tap **"Grant Device Admin"** inside the app. Android will show a system confirmation
screen — tap **Activate**.

### 6. Add the widget to your home screen

Long-press the home screen → **Widgets** → search for **"Lock Widget"** → drag it
onto the home screen.

Now tapping the widget locks the screen instantly.

---

## Permissions & security notes

- Only the `<force-lock />` Device Admin policy is requested — the most minimal set.
- No internet permission is required.
- The widget never transmits any data; it only sends a local broadcast to itself.
- Users can revoke Device Admin at any time via **Settings → Security → Device admins**.

---

## Removing Device Admin

If you need to uninstall the app, revoke Device Admin first:

**Settings → Security → Device admins → Lock Widget → Deactivate**

Then uninstall normally.
