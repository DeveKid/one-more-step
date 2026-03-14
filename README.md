# One More Step 🚶‍♂️

A minimalist, battery-efficient step-tracking application for Android.

Built with **Jetpack Compose**, **Room Database**, and **Foreground Services**, this app is designed to provide a seamless tracking experience while maximizing battery life on modern OLED screens.

---

## 🚀 Features

- **True Black Dark Mode:** Optimized for OLED screens (`#000000`) to draw zero power for black pixels.
- **Hardware-Level Tracking:** Uses `Sensor.TYPE_STEP_COUNTER` and `Sensor.TYPE_STEP_DETECTOR` with hardware batching to let the CPU sleep while you walk.
- **Pause/Resume:** Full control over your tracking sessions. Any steps taken during a pause are excluded from the session total.
- **Dynamic Goals:** Click the "Goal" text on the dashboard to set a custom daily target (e.g., 10,000 steps).
- **Session History:** Automatically saves your walks with the goal, steps, and duration in a local Room database.
- **Monochrome Branding:** A sleek, high-contrast white-on-black aesthetic.

---

## 🏗 Architecture (MVVM)

The app follows the **Model-View-ViewModel** architecture to ensure clean separation of concerns and a reactive UI.

### 1. Model (Data Layer)
- **Room Persistence:** Stores `TrackingSession` objects locally. Each session includes:
  - Start/End timestamps
  - Total steps taken
  - Duration in milliseconds
  - Step Goal set at the time of the session.
- **Repository Pattern:** `TrackingRepository` acts as a clean API for the ViewModel to interact with the database.

### 2. View (UI Layer)
- **Jetpack Compose:** A modern, declarative UI.
- **TrackerScreen:** A single-screen dashboard with:
  - A high-contrast circular progress indicator.
  - Reactive state updates for steps and active time.
  - A scrollable `LazyColumn` for session history.

### 3. ViewModel (Logic Layer)
- **TrackerViewModel:** Orchestrates the UI state by observing:
  - Live data from the `StepTrackingService`.
  - Historical data from the Room database.
- Handles user actions (Start, Pause, Resume, Stop) and communicates with the Foreground Service via Intents.

---

## ⚙️ Core Engineering: StepTrackingService

The "engine" of the app is a **Foreground Lifecycle Service**. This is critical for reliability:

- **Foreground Notification:** Keeps the service alive even when the user is in another app or the screen is off.
- **Sensor Management:**
  - **`Sensor.TYPE_STEP_COUNTER`:** Used for reliable, cumulative step counting.
  - **`Sensor.TYPE_STEP_DETECTOR`:** Provides immediate, real-time feedback in the UI for a better user experience.
- **Power Optimization:** Utilizes hardware-level sensor batching (5s latency) to minimize CPU wake-ups, drastically reducing battery drain.
- **Auto-Reset Logic:** Automatically resets live step counts and timers back to zero once a session is successfully saved to the database.

---

## 🤖 Attribution
This entire application was researched, architected, and written by **Gemini 3**.

---

## 📦 How to Build the APK

If you have Android Studio installed, you can generate the APK by running:

```bash
./gradlew assembleDebug
```

The APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
