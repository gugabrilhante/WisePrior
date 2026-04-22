# WisePrior — Claude Code Context

## Project overview

WisePrior is a multi-module Android task manager built with Jetpack Compose and Clean Architecture, inspired by Apple Reminders.

## Module structure

```
app/                         → Application entry point, Hilt setup, Navigation 3
core/
  model/                     → Domain models: Task, Priority (no Android deps)
  domain/                    → Repository interface + use cases (GetTasks, AddTask, UpdateTask, DeleteTask, GetTaskById)
  designsystem/              → Material 3 theme, colors, typography, shapes (WisePriorTheme)
  ui/                        → Shared Compose components: TaskCard, ToggleRow, SectionHeader, EmptyState, PriorityBadge
  common/                    → Coroutine dispatchers (IoDispatcher, DefaultDispatcher, MainDispatcher)
  storage/                   → Room database, TaskEntity, TaskDao, TypeConverters, DatabaseModule
  data/                      → TaskRepositoryImpl, TaskMapper, RepositoryModule (binds domain interface)
feature/
  tasklist/                  → Task list screen (MVVM + MVI, LazyColumn, SwipeToDismiss)
  taskeditor/                → Task creation/editing screen (Apple Reminders-inspired)
```

**Dependency rule**: features → core:domain/ui/designsystem; core:data → core:domain + core:storage; no feature-to-feature deps.

## Tech stack

| Layer | Library | Version |
|---|---|---|
| UI | Jetpack Compose + Material 3 | BOM 2025.09.01 |
| DI | Hilt | 2.59 |
| Navigation | Navigation 3 | 1.0.0 |
| Database | Room (KSP) | 2.8.3 |
| Build | AGP 9.0.0, Kotlin 2.3.0, KSP 2.3.4, Gradle 9.1.0 |

## Build environment

- **JDK**: Android Studio bundled JDK 21 (configured in gradle.properties via `org.gradle.java.home`)
- **System JDK**: Java 24 — NOT used for Gradle (Groovy 3.x ASM incompatibility with Java 24)
- **compileSdk / targetSdk**: 36 (required by Navigation 3 1.0.0)
- **minSdk**: 24

## Key architecture decisions

- **Navigation 3**: `entryProvider` and `entry<T>` are in `androidx.navigation3.runtime` (NOT ui)
- **AGP 9 built-in Kotlin**: `org.jetbrains.kotlin.android` plugin removed; use `kotlin.jvmToolchain(17)` instead of `kotlinOptions`
- **KSP**: replaces KAPT for Hilt and Room; `androidx.room` Gradle plugin handles schema export
- **MVI state**: `TaskListUiState` / `TaskEditorUiState` data classes + sealed `Event` interfaces

## AI Skills

The following [Agent Skills](https://agentskills.io/) are installed in `.agents/skills/`.

| Skill | Path | When to activate |
|---|---|---|
| `navigation-3` | `.agents/skills/navigation-3/SKILL.md` | Migrating to Navigation 3, deep links, scenes |
| `edge-to-edge` | `.agents/skills/edge-to-edge/SKILL.md` | System bars, IME handling, SDK 35/36 |
| `agp-9-upgrade` | `.agents/skills/agp-9-upgrade/SKILL.md` | AGP upgrade reference (already on AGP 9) |
