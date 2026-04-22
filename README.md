# WisePrior

A production-ready Android task manager app inspired by Apple Reminders, built as a portfolio project to demonstrate modern Android architecture, modularization, and best practices.

---

## Screenshots

> _Coming soon_

---

## Architecture

This project follows **Clean Architecture** combined with **MVVM + MVI-like** state management, based on the [Now in Android](https://github.com/android/nowinandroid) reference architecture from Google.

### Layer diagram

```
┌─────────────────────────────────────────────────┐
│                    :app                          │
│         Navigation 3 · Hilt setup               │
└──────────────┬───────────────────────┬──────────┘
               │                       │
┌──────────────▼──────┐   ┌────────────▼──────────┐
│  :feature:tasklist  │   │  :feature:taskeditor   │
│  ViewModel · UI     │   │  ViewModel · UI        │
└──────────┬──────────┘   └────────────┬───────────┘
           │                           │
           └──────────┬────────────────┘
                      │
        ┌─────────────▼──────────────────────┐
        │            core modules             │
        │                                     │
        │  :core:domain  ←  :core:model       │
        │       ↑                             │
        │  :core:data  ←  :core:storage       │
        │                                     │
        │  :core:ui  ←  :core:designsystem    │
        │  :core:common                       │
        └─────────────────────────────────────┘
```

### Module responsibilities

| Module | Responsibility |
|---|---|
| `:app` | Application entry point, Hilt setup, Navigation 3 host |
| `:core:model` | Domain models (`Task`, `Priority`) — pure Kotlin, zero Android deps |
| `:core:domain` | `TaskRepository` interface + use cases (`GetTasks`, `GetTaskById`, `AddTask`, `UpdateTask`, `DeleteTask`) |
| `:core:data` | `TaskRepositoryImpl`, `TaskMapper`, Hilt `RepositoryModule` |
| `:core:storage` | Room database, `TaskEntity`, `TaskDao`, `TypeConverters`, `DatabaseModule` |
| `:core:designsystem` | `WisePriorTheme`, Material 3 colors, typography, shapes |
| `:core:ui` | Reusable Compose components: `TaskCard`, `ToggleRow`, `SectionHeader`, `EmptyState`, `PriorityBadge` |
| `:core:common` | Coroutine dispatcher qualifiers (`@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher`) |
| `:feature:tasklist` | Task list screen — `TaskListViewModel`, `TaskListUiState`, swipe-to-delete, animated list |
| `:feature:taskeditor` | Task editor screen — `TaskEditorViewModel`, `TaskEditorUiState`, all task fields |

### Dependency rules

- Feature modules depend **only** on `core` modules — never on each other
- `:core:domain` defines the repository contract; `:core:data` implements it
- `:core:storage` has no knowledge of domain models (mapping happens in `:core:data`)

---

## Tech stack

| Category | Library | Version |
|---|---|---|
| Language | Kotlin | 2.3.0 |
| UI | Jetpack Compose + Material 3 | BOM 2025.09.01 |
| Architecture | ViewModel + StateFlow (MVI-like UDF) | Lifecycle 2.10.0 |
| Dependency Injection | Hilt | 2.59 |
| Navigation | Navigation 3 | 1.0.0 |
| Database | Room | 2.8.3 |
| Annotation Processing | KSP (replaces KAPT) | 2.3.4 |
| Build | AGP 9.0.0 + Gradle 9.1.0 | — |
| Serialization | Kotlinx Serialization | 1.7.3 |

---

## Key features

### Task model
Each task supports: title, notes, URL, due date, time, urgent flag, priority (None / Low / Medium / High), tags, and a flagged marker.

### Task List screen
- `LargeTopAppBar` with collapse-on-scroll behavior
- `LazyColumn` with stable keys and `animateItem()` for smooth insert/remove
- Swipe-to-delete with `SwipeToDismissBox`
- Animated empty state
- Material 3 `ElevatedCard` per task with priority badge, flag icon, and due date

### Task Editor screen
- Apple Reminders-inspired sectioned layout
- Toggle rows (Date, Time, Urgent, Flag) using `Switch` via `ListItem`
- Priority selector with `SingleChoiceSegmentedButtonRow`
- Tag input with `FilterChip` and inline add/remove
- Borderless `OutlinedTextField` for title and notes
- IME-aware layout with `imePadding()`

### Edge-to-edge
`enableEdgeToEdge()` called before `setContent`, `windowSoftInputMode="adjustResize"`, insets handled by `Scaffold`.

---

## State management (UDF)

Each feature follows a strict Unidirectional Data Flow:

```
User action
    │
    ▼
ViewModel.onEvent(Event)
    │
    ▼
StateFlow<UiState> updated
    │
    ▼
UI recomposes
```

Example from `feature:taskeditor`:

```kotlin
// UiState
data class TaskEditorUiState(
    val title: String = "",
    val priority: Priority = Priority.NONE,
    val isSaved: Boolean = false,
    // ...
)

// Events
sealed interface TaskEditorEvent {
    data class TitleChanged(val title: String) : TaskEditorEvent
    data class PriorityChanged(val priority: Priority) : TaskEditorEvent
    data object Save : TaskEditorEvent
    // ...
}

// ViewModel
fun onEvent(event: TaskEditorEvent) {
    when (event) {
        is TaskEditorEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
        is TaskEditorEvent.Save -> save()
        // ...
    }
}
```

---

## Navigation 3

Uses the stable [Jetpack Navigation 3](https://developer.android.com/jetpack/androidx/releases/navigation3) library with type-safe routes via Kotlinx Serialization.

Routes implement `NavKey` and are defined inside each feature module:

```kotlin
// feature:tasklist
@Serializable
data object TaskListRoute : NavKey

// feature:taskeditor
@Serializable
data class TaskEditorRoute(val taskId: Long = -1L) : NavKey
```

Each feature exposes an `entryProvider` extension to keep `app` decoupled:

```kotlin
// app navigation host
NavDisplay(
    backStack = rememberNavBackStack(TaskListRoute),
    entryProvider = entryProvider {
        taskListEntries(onAddTask = { ... }, onEditTask = { ... })
        taskEditorEntries(onBack = { ... })
    }
)
```

---

## Build system

- **Version catalog** (`gradle/libs.versions.toml`) — single source of truth for all dependency versions
- **KSP** replaces KAPT for Hilt and Room — faster incremental builds
- **AGP 9 built-in Kotlin** — `org.jetbrains.kotlin.android` removed; `kotlin.jvmToolchain(17)` configures JVM target
- **Room Gradle plugin** (`androidx.room`) — manages schema export via `room { schemaDirectory }` block

---

## Project structure

```
WisePrior/
├── app/
│   └── navigation/         # WisePriorNavHost
├── core/
│   ├── common/             # Coroutine dispatchers
│   ├── data/               # Repository implementation + mapper
│   ├── designsystem/       # Theme, colors, typography
│   ├── domain/             # Use cases + repository interface
│   ├── model/              # Domain models
│   ├── storage/            # Room database
│   └── ui/                 # Shared Compose components
├── feature/
│   ├── taskeditor/         # Task creation/edit screen
│   └── tasklist/           # Task list screen
├── gradle/
│   └── libs.versions.toml  # Version catalog
└── .agents/skills/         # Android Agent Skills
    ├── navigation-3/
    ├── edge-to-edge/
    └── agp-9-upgrade/
```

---

## Getting started

### Requirements
- Android Studio Meerkat or later (bundled JDK 21 required — the build is configured to use it)
- Android SDK 36

### Clone and run
```bash
git clone https://github.com/<your-username>/WisePrior.git
cd WisePrior
./gradlew assembleDebug
```

> **Note:** The project uses the Android Studio bundled JDK 21 (`org.gradle.java.home` in `gradle.properties`). Make sure Android Studio is installed at `/Applications/Android Studio.app/` or update the path accordingly.

---

## Author

**Gustavo Brilhante** — [gugabrilhante@gmail.com](mailto:gugabrilhante@gmail.com)
