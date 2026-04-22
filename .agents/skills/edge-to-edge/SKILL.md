---
name: edge-to-edge
description: >
  Implements edge-to-edge support in Jetpack Compose applications targeting
  SDK 35 or later. Covers enableEdgeToEdge(), system inset handling with
  Scaffold and WindowInsets, IME handling, navigation bar contrast, and
  per-component padding for lists, FABs, and text fields. Use this skill when
  asked to make the app draw behind the system bars, handle soft keyboards
  properly, or fix content being clipped by system UI.

license: Complete terms in LICENSE.txt

metadata:
  author: Google LLC
  source: https://github.com/android/skills/tree/main/system/edge-to-edge
  keywords:
    - edge-to-edge
    - WindowInsets
    - Scaffold
    - IME
    - system bars
    - Compose
    - Material3
---

# Edge-to-Edge in Jetpack Compose

## When to use this skill

Use this skill when the user asks to:
- Make the app draw behind the status bar or navigation bar
- Fix content hidden behind system bars
- Handle soft keyboard overlapping content
- Prepare the app for SDK 35 (edge-to-edge is enforced by default)

## Prerequisites

- Project **must** use Jetpack Compose
- Target SDK **must** be set to 35 or higher (`targetSdk = 35` in all modules)
- Uses `ComponentActivity` (already the case in WisePrior's `TaskManagerActivity`)

---

## Step 1: Audit the current state

Identify all `Activity` classes that lack edge-to-edge support. Pay special
attention to screens that contain:
- `LazyColumn` / `LazyRow` lists
- FABs
- Text input fields (`OutlinedTextField`, `TextField`)

---

## Step 2: Enable edge-to-edge in each Activity

```kotlin
import androidx.activity.enableEdgeToEdge

class TaskManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()   // must be called BEFORE setContent
        super.onCreate(savedInstanceState)
        setContent { ... }
    }
}
```

`enableEdgeToEdge()` from `ComponentActivity` automatically sets the correct
`isAppearanceLightNavigationBars` and `isAppearanceLightStatusBars` flags based
on the current theme — no manual `WindowCompat` calls needed.

Also add to `AndroidManifest.xml` for any Activity that hosts a soft keyboard:

```xml
<activity
    android:name=".TaskManagerActivity"
    android:windowSoftInputMode="adjustResize" />
```

---

## Step 3: Apply system insets — choose ONE method per screen

### Option A: Scaffold (recommended for full screens)

`Scaffold` in Material 3 automatically handles top and bottom insets via
`PaddingValues`. Pass the padding down to content:

```kotlin
Scaffold(
    topBar = { TopAppBar(title = { Text("Tasks") }) },
    floatingActionButton = { TaskFab() }
) { paddingValues ->
    TaskList(modifier = Modifier.padding(paddingValues))
}
```

### Option B: `safeDrawingPadding()` (for screens without Scaffold)

```kotlin
Box(modifier = Modifier.safeDrawingPadding()) {
    // content here
}
```

### Option C: Per-component insets (for granular control)

```kotlin
// LazyColumn: add padding at the bottom item so FAB doesn't overlap
LazyColumn(
    contentPadding = WindowInsets.safeDrawing.asPaddingValues()
) { ... }
```

> **Warning**: Never combine multiple methods on the same screen — this causes
> double padding.

---

## Step 4: Handle the IME (soft keyboard)

Prefer `Modifier.fitInside(WindowInsetsRulers.Ime.current)` over `imePadding()`
for screens with text fields — it avoids layout performance issues:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .fitInside(WindowInsetsRulers.Ime.current)
) {
    OutlinedTextField(...)
}
```

If `WindowInsetsRulers` is not yet available in the project's Compose version,
use `imePadding()` as a fallback:

```kotlin
modifier = Modifier.imePadding()
```

---

## Step 5: Update targetSdk

```toml
# gradle/libs.versions.toml — no change needed yet; add when bumping
# targetSdk to 35 in all modules
```

```groovy
// Each module's build.gradle
android {
    defaultConfig {
        targetSdk 35   // update from 34
    }
}
```

---

## Step 6: Navigation bar contrast

When NOT using `ComponentActivity.enableEdgeToEdge()` (e.g., using raw
`WindowCompat`), set manually:

```kotlin
WindowCompat.getInsetsController(window, view).apply {
    isAppearanceLightNavigationBars = !isDarkTheme
    isAppearanceLightStatusBars = !isDarkTheme
}
```

With `ComponentActivity.enableEdgeToEdge()` this is handled automatically.

---

## Verification checklist

- [ ] `enableEdgeToEdge()` called before `setContent` in every Activity
- [ ] `android:windowSoftInputMode="adjustResize"` for Activities with keyboards
- [ ] Only ONE inset method applied per screen (no double padding)
- [ ] Lists have bottom padding so FAB/nav bar doesn't overlap last item
- [ ] FAB accounts for navigation bar inset (Scaffold handles this automatically)
- [ ] Text fields push up when keyboard appears
- [ ] Both light and dark themes tested
- [ ] `targetSdk 35` set after verification
