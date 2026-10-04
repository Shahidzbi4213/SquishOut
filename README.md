# 🍓 Squish Out — Kawaii Directional Unblocking Puzzle

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF.svg?logo=kotlin)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.12.1-4285F4.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-brightgreen.svg)]()
[![Database](https://img.shields.io/badge/Room%20KMP-2.8.5-34A853.svg?logo=sqlite)](https://developer.android.com/kotlin/multiplatform/room)
[![DI](https://img.shields.io/badge/Koin-4.2.2-FF5722.svg)](https://insert-koin.io/)
[![Tests](https://img.shields.io/badge/Tests-100%25%20Passing-success.svg)]()
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> A vibrant, juicy evolution of classic directional unblocking puzzles (like *Arrow Puzzle*). Replace cold abstract arrows with playful, translucent gelatin blobs (**"Squishies"**) featuring multi-stop radial gloss shaders, expressive eye states, squish-and-stretch launch physics, and a rich metagame.

---

## ✨ Features

- **🍓 Tactile Gummy Jelly Physics & Shaders**:
  - Translucent multi-stop radial gloss gradients with white specular arcs.
  - Expressive eyelid states: awake star-sparkling pupils `( ✦‿✦ )` when an exit path is clear, sleepy curved lids `( ˘◡˘ )` when obstructed.
  - Directional teardrop crown/snouts indicating escape vectors.
  - Sinusoidal refusal wobbles on blocked taps and smooth squish-and-stretch tweens on launch.
- **🧩 Mathematical Reverse-Assembly Solver**:
  - Pure zero-dependency grid engine (`:core-engine`) guaranteeing **100% solvable boards**.
  - Dynamic reverse-slide puzzle generation with calibrated difficulty curves across 50 stages.
  - Multi-tile pieces (1x1 blobs and 1x2 Grape Eel Duo).
  - Raycasting exit path detection and greedy solver verification.
- **🗺️ Saga Progression Map**:
  - Vertical winding stepping-stone map featuring 50 handcrafted stages.
  - Biome milestones: *🌸 Sweet Meadow* (Lv 1–20), *🌊 Soda Lagoon* (Lv 21–40), and *🍯 Honeycomb Valley* (Lv 41+).
  - Active stage marker with crowned mascot pin and `PLAY! ▶` pulsing badge.
- **🐾 Jelly Dex & Companion Showcase**:
  - Hero mascot spotlight with floating pedestal glow and perk descriptions.
  - Collectible character cards: *Strawberry Blobby* (Common), *Blueberry Duo* (Common), *Lemon Spark* (Rare), *Kiwi Hopper* (Rare), *Grape Monarch* (Mythic).
  - Candy unlock and one-tap skin equipping.
- **💾 Offline-First Room KMP Persistence**:
  - Powered by AndroidX Room 2.8.5 with KSP symbol processing and `BundledSQLiteDriver`.
  - Tracks player currency (Candies & Gems), unlocked stages, and 1–3 star ratings.
  - Automatic heart regeneration timer: restores 1 life every 20 minutes (up to 5 max).
- **⚡ Booster Dock & Modals**:
  - Floating frosted capsule booster dock: `Undo`, `Hint`, and `Magic Wand`.
  - Celebratory **Level Victory Modal** with 3D embossed stars, score recap, and lightweight Canvas confetti particle bursts.
  - Empathy-driven **Out of Hearts Modal** featuring a teary sad blue mascot, rewarded ad continue (`+3 ❤️`), and gem revive options.

---

## 🏗️ Architecture & Modules

```
SquishOut/
├── core-engine/         # Pure Kotlin (KMP) zero-dependency engine
│   ├── model/           # Position, Direction, Jelly, Board, Obstacles
│   ├── generator/       # ReverseAssemblyGenerator & mathematical solver
│   └── GameEngine.kt    # Unidirectional game state machine
│
├── shared/              # 100% Shared UI & Data (Compose Multiplatform)
│   ├── commonMain/
│   │   ├── board/       # Skia Custom Canvas, JellyRenderer, TrayRenderer, LaunchAnimation
│   │   ├── data/        # Room Database, LevelDao, JellySkinDao, UserSessionDao, GameRepository
│   │   ├── di/          # Koin Modules (DatabaseModule, EngineModule, ViewModelModule)
│   │   ├── presentation/# GameViewModel with reactive StateFlows
│   │   ├── theme/       # SquishColors, SquishTypography, SquishOutTheme
│   │   └── ui/          # SagaMapScreen, GameScreen, JellyDexScreen, BoosterDock, Modals
│   ├── androidMain/     # Android DatabaseFactory & AudioPlayer (SoundPool + Haptics)
│   └── iosMain/         # iOS DatabaseFactory (NSDocumentDirectory) & AudioPlayer (CoreAudio)
│
├── androidApp/          # Native Android Application entry point & Manifest
└── iosApp/              # SwiftUI iOS Application hosting Shared.framework
```

---

## 🛠️ Tech Stack

| Technology | Version | Role |
| :--- | :--- | :--- |
| **Kotlin** | `2.4.20` | Multiplatform language runtime |
| **Compose Multiplatform** | `1.12.1` | Declarative shared UI across Android & iOS |
| **Android Gradle Plugin** | `9.1.1` | Android build tooling |
| **AndroidX Room KMP** | `2.8.5` | Relational offline database persistence |
| **AndroidX SQLite** | `2.6.2` | Bundled C-SQLite driver for iOS & Android |
| **Google KSP** | `2.3.12` | Kotlin Symbol Processing for Room code-gen |
| **Koin Multiplatform** | `4.2.2` | Dependency injection |
| **Kotlinx Coroutines** | `1.10.1` | Asynchronous flows and test dispatchers |

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17+** (JDK 17 or Azul Zulu 21 recommended)
- **Android Studio Ladybug / Meerkat** (with Android SDK 35+)
- **Xcode 16+** (for macOS iOS builds and simulators)

### Clone & Build
```bash
git clone https://github.com/Shahidzbi4213/SquishOut.git
cd SquishOut
```

#### Run All Unit Tests
```bash
./gradlew :core-engine:jvmTest :shared:testAndroidHostTest
```

#### Assemble Android Debug APK
```bash
./gradlew :androidApp:assembleDebug
# Output APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

#### Link iOS Framework
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
# Output Framework: shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework
```

---

## 🧪 Testing

The repository maintains **100% test coverage** for all game logic, level generation, and data persistence:
- `ReverseAssemblyGeneratorTest`: Mathematical verification that every generated board is 100% solvable.
- `BoardRaycasterTest`: Corner raycasts, obstacle blocking, multi-cell piece collision detection.
- `GameEngineTest`: Direct tap state transitions, undo stacks, hint solvers, and star calculations.
- `GameRepositoryTest`: Room DAO operations, 20-minute heart refill timer calculations, and currency economies.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.