# FitMax 🏃‍♂️⚡

FitMax is a modern, high-precision step tracker and daily wellness Android application inspired by **Samsung Health** and **One UI 7**, built 100% with **Jetpack Compose** and **Material 3 Expressive motion**.

---

## 🌟 Highlights & Features

- **Samsung Health Active Workout Card**: Real-time workout metrics with frosted pill badges displaying active timer, live cadence steps, and calorie expenditure.
- **3-Layer Concentric Activity Ring**: Geometrically clean concentric rings tracking daily Steps (Green), Active Time (Cyan), and Burned Calories (Pink) with animated sweep progression.
- **One UI 7 Step Goal Progress Card**: Vibrant green goal card with animated horizontal progress track and dynamic completion percentage pill.
- **Health & Vitals 2-Column Suite**:
  - **Vitals Card**: Sleep score and resting recovery metrics with decorative radar ripple canvas artwork.
  - **Daily Cardio Load Card**: Optimal training zone and acute training load with custom speedometer gauge canvas.
- **One UI 7 Quick Stats Cards**: Rich gradient cards for Calories, Active Time, Distance, and Elevation with custom geometric corner vector illustrations.
- **Floating Island Navigation Bar**: Floating capsule pill navigation bar with tactile haptic bounce, paired with a companion floating action button for instant workout logging.
- **Hardware Sensor Dual-Engine**: Hardware step counter sensor (`TYPE_STEP_COUNTER`) fallback with high-performance accelerometer step detection.
- **Material 3 Expressive Motion**: Spring bounce physics (`m3BounceClick`) and low-latency haptic feedback inspired by `aro-music`.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material 3 Expressive)
- **Architecture**: Clean Architecture / MVVM with StateFlow
- **Sensor Tracking**: Android Sensor Framework with `ForegroundService` and `SensorEventListener`
- **Database**: Room Database with SQLite for historical day summaries
- **Asynchronous**: Kotlin Coroutines & Flow

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug | 2024.2+
- JDK 17
- Android SDK 34 (Android 14) minimum SDK 26 (Android 8.0)

### Build & Run
```bash
# Clone the repository
git clone https://github.com/sahilcodexx/fitmax.git
cd fitmax

# Compile Debug APK
./gradlew assembleDebug

# Install via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
FitMax is released under the [MIT License](LICENSE).
