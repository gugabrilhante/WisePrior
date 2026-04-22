---
name: navigation-3
description: >
  Guides migration from Jetpack Navigation 2 to Navigation 3 and teaches how
  to implement Navigation 3 patterns: type-safe routes, multiple back stacks,
  deep links, scenes (dialogs, bottom sheets, list-detail), conditional
  navigation, returning results from flows, and integration with Hilt and
  ViewModel. Use this skill when working with navigation in this multi-module
  project or when asked to migrate from navigation-compose 2.x to Navigation 3.

license: Complete terms in LICENSE.txt

metadata:
  author: Google LLC
  source: https://github.com/android/skills/tree/main/navigation/navigation-3
  keywords:
    - Navigation 3
    - Jetpack Navigation
    - type-safe navigation
    - deep links
    - back stack
    - Hilt
    - ViewModel
    - multi-module
---

# Jetpack Navigation 3

## When to use this skill

Use this skill when the user asks to:
- Migrate from `navigation-compose:2.x` to Navigation 3
- Implement navigation in a new feature module
- Add deep links, multiple back stacks, or dialog scenes
- Wire navigation with Hilt or ViewModel in a modular project

## Project context

WisePrior currently uses `navigation-compose:2.6.0` in `:feature:taskmanager`.
Migration to Navigation 3 is the long-term upgrade path.

---

## Step 1: Update dependencies

```toml
# gradle/libs.versions.toml
[versions]
navigation3 = "1.0.0-alpha01"   # check latest at d.android.com/jetpack/androidx/releases/navigation

[libraries]
androidx-navigation3-compose = { group = "androidx.navigation3", name = "navigation3-compose", version.ref = "navigation3" }
androidx-navigation3-ui = { group = "androidx.navigation3", name = "navigation3-ui", version.ref = "navigation3" }
```

```groovy
// feature/taskmanager/build.gradle
dependencies {
    implementation libs.androidx.navigation3.compose
    implementation libs.androidx.navigation3.ui
}
```

---

## Step 2: Define type-safe routes

Navigation 3 uses `@Serializable` data classes or objects as routes instead of string paths:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
object TaskListRoute

@Serializable
data class TaskDetailRoute(val taskId: Long)
```

---

## Step 3: Create a NavDisplay

Replace `NavHost` with `NavDisplay` and provide a back stack:

```kotlin
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

@Composable
fun TaskManagerNavHost() {
    val backStack = rememberNavBackStack(TaskListRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<TaskListRoute> {
                TaskListScreen(
                    onTaskClick = { id -> backStack.add(TaskDetailRoute(id)) }
                )
            }
            entry<TaskDetailRoute> { route ->
                TaskDetailScreen(taskId = route.taskId)
            }
        }
    )
}
```

---

## Step 4: Hilt + ViewModel integration

In Navigation 3, obtain a `ViewModel` scoped to a back stack entry using
`viewModel()` inside the `entry` lambda — Hilt wires it automatically:

```kotlin
entry<TaskListRoute> {
    val viewModel: TaskManagerViewModel = hiltViewModel()
    TaskListScreen(viewModel = viewModel)
}
```

Ensure `:feature:taskmanager` still applies `alias(libs.plugins.hilt.android)`.

---

## Step 5: Deep links

```kotlin
entry<TaskDetailRoute>(
    deepLinks = listOf(navDeepLink<TaskDetailRoute>(basePath = "wiseprior://task"))
) { route ->
    TaskDetailScreen(taskId = route.taskId)
}
```

---

## Step 6: Scenes (dialogs / bottom sheets)

```kotlin
import androidx.navigation3.ui.DialogScene

entry<ConfirmDeleteRoute>(
    scene = DialogScene
) {
    ConfirmDeleteDialog(onDismiss = { backStack.removeLastOrNull() })
}
```

---

## Step 7: Modular navigation

For multi-module projects, define each module's routes in its own file and expose a composable that accepts the back stack as a parameter:

```kotlin
// :feature:taskmanager exposes:
fun NavEntryProviderBuilder.taskManagerEntries(backStack: NavBackStack) {
    entry<TaskListRoute> { ... }
    entry<TaskDetailRoute> { ... }
}

// :app wires everything:
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider {
        taskManagerEntries(backStack)
        // future feature entries here
    }
)
```

---

## Step 8: Return results from a flow

Use a shared `ViewModel` or `SavedStateHandle` to return results upstream:

```kotlin
// In a child destination
viewModel.sendResult(result)
backStack.removeLastOrNull()

// In the parent destination
val result by viewModel.result.collectAsStateWithLifecycle()
```

---

## Verification checklist

- [ ] All routes are `@Serializable`
- [ ] `NavDisplay` replaces `NavHost`
- [ ] Back stack is managed with `rememberNavBackStack`
- [ ] Hilt `hiltViewModel()` used inside `entry` lambdas
- [ ] Deep links updated to new `navDeepLink<Route>()` API
- [ ] Each feature module only depends on its own routes, not `:app`
