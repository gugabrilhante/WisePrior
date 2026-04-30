# WisePrior

[![Build](https://github.com/gugabrilhante/WisePrior/actions/workflows/build.yml/badge.svg)](https://github.com/gugabrilhante/WisePrior/actions/workflows/build.yml)
[![Unit Tests](https://github.com/gugabrilhante/WisePrior/actions/workflows/unit_test.yml/badge.svg)](https://github.com/gugabrilhante/WisePrior/actions/workflows/unit_test.yml)
[![UI Tests](https://github.com/gugabrilhante/WisePrior/actions/workflows/ui_test.yml/badge.svg)](https://github.com/gugabrilhante/WisePrior/actions/workflows/ui_test.yml)
[![codecov](https://codecov.io/gh/gugabrilhante/WisePrior/branch/master/graph/badge.svg)](https://codecov.io/gh/gugabrilhante/WisePrior)

**Test Coverage: ~90% (JaCoCo + Codecov)**

---

## Overview

WisePrior is a production-grade Android task manager designed with a heavy focus on **Scalability**, **Modularization**, and **Quality Engineering**. Inspired by modern productivity tools, it serves as a technical showcase for Clean Architecture and robust background synchronization.

### Quick Navigation
- [Architecture & Modularization](#architecture)
- [Quality Engineering & Test Strategy](#quality-engineering--test-strategy)
- [Key Features](#features)
- [Tech Stack](#tech-stack)

---

## Demo

<img src="docs/wise_prior_demo_01.gif" width="33%" alt="Demo 01">  <img src="docs/wise_prior_demo_02.gif" width="33%" alt="Demo 02"> <img src="docs/wise_prior_demo_03.gif" width="33%" alt="Demo 03">
<img src="docs/wise_prior_demo_04.gif" width="33%" alt="Demo 04">  <img src="docs/wise_prior_demo_05.gif" width="33%" alt="Demo 05"> <img src="docs/wise_prior_demo_06.gif" width="33%" alt="Demo 06">
<img src="docs/wise_prior_demo_07.gif" width="33%" alt="Demo 07">  <img src="docs/wise_prior_demo_08.gif" width="33%" alt="Demo 08"> <img src="docs/wise_prior_demo_09.gif" width="33%" alt="Demo 09">

---

## Architecture

WisePrior is built on **Clean Architecture** principles, enforcing a strict separation of concerns between business logic, data handling, and UI. This approach was chosen to ensure the codebase remains maintainable as features grow and to enable high-fidelity testing of each component in isolation.

The project utilizes **MVVM** for UI logic and **MVI-inspired** state management for predictable, unidirectional data flow, following the [Now in Android](https://github.com/android/nowinandroid) best practices.

### Layer diagram

```
┌────────────────────────────────────────────────────────┐
│                        :app                             │
│           Navigation 3 · Hilt setup · Deep links        │
└──────────────┬────────────────────────┬────────────────┘
               │                        │
┌──────────────▼──────┐   ┌─────────────▼──────────────┐
│  :feature:tasklist  │   │    :feature:taskeditor       │
│  ViewModel · UI     │   │    ViewModel · UI            │
└──────────┬──────────┘   └─────────────┬───────────────┘
           │                            │
           └──────────────┬─────────────┘
                          │
        ┌─────────────────▼──────────────────────────┐
        │                core modules                  │
        │                                              │
        │  :core:domain  ←──────  :core:model          │
        │       ↑                                      │
        │  :core:data  ←────────  :core:storage        │
        │       ↑                                      │
        │  :core:notifications  (AlarmManager + WM)    │
        │                                              │
        │  :core:ui  ←──────────  :core:designsystem   │
        │  :core:common                                │
        └──────────────────────────────────────────────┘
```

### Module responsibilities

The project is decomposed into specialized modules, each adhering to the **Single Responsibility Principle (SRP)**. These modular boundaries prevent "spaghetti dependencies" and significantly reduce build times via parallel compilation.

| Module | Responsibility |
|---|---|
| `:app` | Entry point, Hilt setup, Navigation 3 host, notification deep link handling |
| `:core:model` | Domain models (`Task`, `Priority`, `RecurrenceType`) — pure Kotlin, zero Android deps |
| `:core:domain` | `TaskRepository` interface + use cases: `GetTasks`, `GetTaskById`, `AddTask`, `UpdateTask`, `DeleteTask` |
| `:core:data` | `TaskRepositoryImpl`, `TaskMapper`, Hilt `RepositoryModule` |
| `:core:storage` | Room database v4, `TaskEntity`, `TaskDao`, `TypeConverters`, schema migrations |
| `:core:notifications` | `AlarmManagerNotificationScheduler`, `AlarmReceiver`, `BootReceiver`, `RescheduleNotificationsWorker`, `NotificationHelper` |
| `:core:designsystem` | `WisePriorTheme`, Material You colors, typography, shapes |
| `:core:ui` | Shared Compose components: `TaskCard`, `ToggleRow`, `SectionHeader`, `EmptyState`, `PriorityBadge` |
| `:core:common` | Coroutine dispatcher qualifiers (`@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher`) |
| `:feature:tasklist` | Task list — `TaskListViewModel`, swipe-to-delete, animated list, notification cancellation on delete |
| `:feature:taskeditor` | Task editor — `TaskEditorViewModel`, date/time pickers, recurrence, notification scheduling on save |

### Dependency rules

- **Strict Boundaries**: Feature modules depend **only** on `core` modules — never on each other.
- **Contract First**: `:core:domain` defines the repository contract; `:core:data` implements it.
- **Data Isolation**: `:core:storage` has no knowledge of domain models; mapping is handled exclusively in `:core:data`.
- **Infrastructure**: `:core:notifications` depends on `:core:model` and `:core:domain` for scheduling logic.

---

## Tech stack

| Category | Library | Version |
|---|---|---|
| Language | Kotlin | 2.3.0 |
| UI | Jetpack Compose + Material 3 | BOM 2025.09.01 |
| Architecture | ViewModel + StateFlow (MVI-like UDF) | Lifecycle 2.10.0 |
| Dependency Injection | Hilt | 2.59 |
| Navigation | Navigation 3 (stable) | 1.0.0 |
| Database | Room | 2.8.3 |
| Background | AlarmManager + WorkManager | — / 2.10.0 |
| Annotation Processing | KSP | 2.3.4 |
| Build | AGP 9.0.0 + Gradle 9.1.0 | — |
| Serialization | Kotlinx Serialization | 1.7.3 |

---

## Features

### Task model
Each task carries: title, notes, URL, due date, time flag, urgent flag, priority (None / Low / Medium / High), tags, flagged marker, and recurrence type (None / Daily / Weekly / Monthly).

### Task List screen
- `LargeTopAppBar` with collapse-on-scroll behavior.
- `LazyColumn` with stable keys and `animateItem()` for smooth insert/remove animations.
- Swipe-to-delete with `SwipeToDismissBox` — automatically cancels associated alarms.
- Animated empty state and `ElevatedCard` UI with custom priority indicators.

### Task Editor screen
- Apple Reminders-inspired sectioned layout with IME-aware padding.
- **Date & Time**: Integrated Material 3 pickers with state preservation.
- **Recurrence**: Contextual recurrence selector visible only when a date is assigned.
- **Organization**: Priority segmented buttons and dynamic Tag input with `FilterChip`.

### Reminder notifications
- Exact alarm via `AlarmManager.setExactAndAllowWhileIdle()` — fires even in Doze mode
- Tap notification → navigates directly to the task editor
- Recurring reminders reschedule automatically from the **original** due time (no drift)
- Device reboot: `BootReceiver` enqueues a WorkManager job that re-fetches all tasks and restores alarms
- Recurring tasks whose alarm time passed during reboot are advanced to the next future occurrence

### Edge-to-edge
`enableEdgeToEdge()` before `setContent`, `windowSoftInputMode="adjustResize"`, insets via `Scaffold`.

---

## State management (UDF)

Each feature follows strict Unidirectional Data Flow. Navigation is triggered via a `Channel<Unit>` — never via a boolean in `UiState` — to guarantee exactly one delivery and prevent flickering.

```
User action ──▶ ViewModel.onEvent(Event) ──▶ StateFlow<UiState> update ──▶ UI Recompose
                                        └──▶ Channel<Unit>.send(Unit) ──▶ Navigation
```

---

## Quality Engineering & Test Strategy

At WisePrior, the architecture is the foundation of our testing strategy. By decoupling business rules from Android frameworks, we achieve a high degree of testability where every layer can be validated independently. Our suite provides fast feedback, prevents regressions, and ensures that critical user journeys remain functional across all SDK versions.

---

### 🏗️ Test Pyramid

The project follows the classic **Test Pyramid** principle: favour many fast, isolated unit tests at the base; a smaller set of integration tests in the middle; and targeted UI tests at the top for critical user flows.

```
         ▲
        /  \          UI / End-to-End
       / UI \         (Espresso — critical flows)
      /──────\
     /        \       Integration
    / Integrat.\      (Room in-memory, Repository + DataSource)
   /────────────\
  /              \    Unit tests
 /   Unit Tests   \   (Use Cases, ViewModels, Repositories, Mappers)
/──────────────────\
```

| Level | Scope | Speed | Cost |
|---|---|---|---|
| **Unit** | Business Logic, ViewModels, Mappers | ⚡ Milliseconds | Low |
| **Integration** | Persistence (Room), Repositories | 🕐 Seconds | Medium |
| **E2E / UI** | Full User Journeys (Compose + Espresso) | 🕑 Minutes | High |

---

### 🧪 Unit Tests

Unit tests target every layer of the Clean Architecture stack — use cases, repositories, mappers, and view models — using fakes and mocks to keep each test fully isolated.

**What is covered**

| Module | Classes under test |
|---|---|
| `core:domain` | All 9 use cases: `GetTasksUseCase`, `AddTaskUseCase`, `UpdateTaskUseCase`, `DeleteTaskUseCase`, `GetTaskByIdUseCase`, `GetTagsUseCase`, `AddTagUseCase`, `UpdateTagUseCase`, `DeleteTagUseCase` |
| `core:data` | `TaskRepositoryImpl`, `TagRepositoryImpl`, `TaskMapper`, `TagMapper` |
| `core:storage` | `Converters` (Room TypeConverter round-trips) |
| `feature:tasklist` | `TaskListViewModel` — state emissions, deletion, error handling, all 6 collection filters, tag editor with MAX_TAGS enforcement |
| `feature:taskeditor` | `TaskEditorViewModel` — task loading, all `TaskEditorEvent` types, save validation, date/time preservation, notification scheduling |

**Tools**

| Tool | Role |
|---|---|
| **JUnit 4** | Test runner and base assertions |
| **MockK 1.13** | Kotlin-idiomatic mocking — `mockk(relaxed = true)`, slot capture, `coVerify` |
| **Turbine 1.2** | `StateFlow` / `Flow` testing: `awaitItem()`, `cancelAndIgnoreRemainingEvents()` |
| **kotlinx.coroutines.test** | `runTest`, `UnconfinedTestDispatcher`, `Dispatchers.setMain/resetMain` |

**Naming convention** — every test name is self-documenting using the `given / when / then` format:

```kotlin
@Test
fun `given active ByTag collection, when that tag deleted, then selection falls back to All`()

@Test
fun `given tag count at maximum, when saveTag called for new tag, then error is set and tag is not added`()
```

**Coverage Overview**
- **Domain**: 100% of use cases validated, including complex task lifecycle rules.
- **UI Logic**: ViewModels tested for state emissions, event handling, and input validation.
- **Data**: Mappers and Converters verified for data integrity across transformations.

**Key Scenarios**
- Collection filters (Today, Scheduled, Flagged, etc.) verified for accuracy.
- Tag limit enforcement (MAX_TAGS = 5) and error state propagation.
- Date/Time logic cascades (e.g., clearing date automatically resets recurrence).

---

### 🔗 Integration Tests
We utilize an **in-memory Room database** to run integration tests that validate the real data flow from the repository down to the SQLite layer.

**Why In-Memory?**
- **Determinism**: Each test starts with a clean slate, eliminating shared state flakiness.
- **Realism**: Verifies SQL queries, migrations, and reactive Flow emissions against a real SQLite engine.

---

### 📱 E2E & UI Tests (Espresso + Compose)
End-to-End tests in the `:app` module exercise the full stack, including Hilt dependency injection and Navigation 3 transitions.

**Full Task Lifecycle Test**
1. App launches into an empty state.
2. User creates a new task via the FAB.
3. Task is edited (title change + priority update).
4. Task is marked as completed.
5. All UI states are verified to match the persistent data layer.

**Stability Features**
- **GrantPermissionRule**: Automatically handles runtime notification permissions to avoid UI blocking.
- **Declarative Synchronization**: Custom `waitUntil` helpers ensure tests wait for async database operations before asserting.

---

### ⚙️ CI/CD & Coverage
- **Continuous Integration**: Every PR triggers the full test suite via GitHub Actions.
- **Quality Gate**: Merges to `master` are blocked unless all Unit and UI tests pass.
- **Visibility**: Coverage is tracked via **JaCoCo** and reported through **Codecov** for transparent quality tracking.

---

## Getting started

### Requirements
- Android Studio Meerkat or later (bundled JDK 21 required)
- Android SDK 36

### Clone and run
```bash
git clone https://github.com/gugabrilhante/WisePrior.git
cd WisePrior
./gradlew assembleDebug
```

---

## Author

**Gustavo Brilhante** — [gugabrilhante@gmail.com](mailto:gugabrilhante@gmail.com)
