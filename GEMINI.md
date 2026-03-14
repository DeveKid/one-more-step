# GEMINI.md - One More Step 🚶‍♂️

This document provides architectural context, build instructions, and development conventions for the **One More Step** Android project.

---

## 📋 Project Overview

**One More Step** is a minimalist, battery-efficient step-tracking application built with modern Android standards. The primary engineering goal is to maximize battery life on OLED screens while providing reliable background tracking.

### Key Technologies:
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Declarative UI)
- **Database:** Room Persistence (Offline-first)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Background Processing:** Android Foreground Services (Health Type)
- **Dependency Management:** Manual Dependency Injection via `OneStepApplication` and `ViewModelProvider.Factory`.

---

## 🏗 System Architecture

The project follows a strict separation of concerns to ensure maintainability and testability.

### 1. Model (Data Layer)
- **Entities:** `TrackingSession` (com.example.onestep.data.model)
- **DAO:** `SessionDao` (com.example.onestep.data.db)
- **Database:** `AppDatabase` (com.example.onestep.data.db)
- **Repository:** `TrackingRepository` (com.example.onestep.data.repository) - The single source of truth for the ViewModel.

### 2. ViewModel (State Management)
- **`TrackerViewModel`:** Observes the Repository's Flow and handles communication with the `StepTrackingService` via Intents. It uses a `Factory` to receive the `TrackingRepository` dependency.

### 3. View (UI Layer)
- **`MainActivity`:** Entry point, handles runtime permissions (`ACTIVITY_RECOGNITION`, `POST_NOTIFICATIONS`).
- **`TrackerScreen`:** The main Compose dashboard.
- **Theme:** "True Black" theme (com.example.onestep.ui.theme) optimized for OLED pixels.

### 4. Background Service (The Engine)
- **`StepTrackingService`:** A `LifecycleService` that manages hardware sensors (`TYPE_STEP_COUNTER` and `TYPE_STEP_DETECTOR`). It runs as a Foreground Service to prevent OS termination.

---

## 🚀 Building and Running

### Prerequisites
- **Android SDK:** Compile SDK 34, Min SDK 26.
- **Gradle:** 8.2.0 or higher.

### Key Commands
- **Assemble Debug APK:**
  ```powershell
  ./gradlew assembleDebug
  ```
- **Run Unit Tests:**
  ```powershell
  ./gradlew test
  ```
- **Lint Check:**
  ```powershell
  ./gradlew lint
  ```

### Manual Installation
The generated APK can be found at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🛠 Development Conventions

### 1. Clean Coding Practices
- **String Externalization:** Never hardcode strings in UI or Services. Use `R.string` from `strings.xml`.
- **Logic Separation:** All formatting logic (e.g., time/date) must reside in `com.example.onestep.util.TimeUtils`.
- **Dependency Injection:** Provide dependencies via the Application class (`OneStepApplication`). Avoid manual instantiation of Repositories inside ViewModels.

### 2. Resource Naming
- **Icons:** `ic_*.xml` (e.g., `ic_logo.xml`, `ic_walking_man.xml`).
- **Composables:** Use PascalCase for `@Composable` functions (e.g., `SessionCard`).

### 3. State Management
- Prefer `StateFlow` over `LiveData` for architectural consistency.
- Observe state in Compose using `.collectAsState()`.
