---
name: agp-9-upgrade
description: >
  Step-by-step guide for upgrading an Android project from AGP 8.x to AGP 9.
  Covers dependency requirements (KSP 2.3.6+, Hilt 2.59.2+), migration from
  kapt to KSP, built-in Kotlin migration, new AGP 9 DSL changes, BuildConfig
  handling, and gradle.properties cleanup. Use this skill when asked to upgrade
  the Android Gradle Plugin to version 9, migrate from kapt to KSP, or adopt
  the new built-in Kotlin support in AGP 9. Does NOT apply to Kotlin
  Multiplatform projects.

license: Complete terms in LICENSE.txt

metadata:
  author: Google LLC
  source: https://github.com/android/skills/tree/main/build/agp/agp-9-upgrade
  keywords:
    - AGP 9
    - Gradle Plugin upgrade
    - KSP
    - kapt
    - Hilt
    - Room
    - built-in Kotlin
    - DSL migration
---

# AGP 9 Upgrade Guide

## When to use this skill

Use this skill when the user asks to:
- Upgrade from AGP 8.x to AGP 9
- Migrate from `kotlin-kapt` to KSP
- Adopt `androidx.room` Gradle plugin
- Prepare the project for Gradle 9.0 (warnings visible in current build output)

## Project context

WisePrior is currently on:
- AGP `8.9.2` (all modules)
- Kotlin `1.9.23` + `kotlin-kapt` plugin
- Hilt `2.48` (requires upgrade to `2.59.2+` for AGP 9)
- Room `2.6.1` (KSP-ready)

---

## Step 0: Verify current AGP version

Before proceeding, confirm AGP < 9 in `gradle/libs.versions.toml`:

```toml
agp = "8.9.2"   # ← must be < 9 to use this guide
```

If AGP is already ≥ 9, stop — this skill does not apply.

---

## Step 1: Update minimum dependency versions

These versions are **required** by AGP 9:

```toml
# gradle/libs.versions.toml
[versions]
agp = "9.0.0"               # or latest stable
kotlin = "2.0.21"           # AGP 9 requires Kotlin 2.x for built-in Kotlin
ksp = "2.0.21-1.0.25"       # must be 2.3.6+ equivalent for Kotlin 2.x
hilt = "2.56"               # must be 2.59.2+ for AGP 9 (check latest)
room = "2.6.1"              # already at correct version; KSP support included
```

> Check latest versions at [d.android.com/jetpack/androidx/releases](https://developer.android.com/jetpack/androidx/releases).

---

## Step 2: Migrate to built-in Kotlin

AGP 9 includes Kotlin as a built-in component. Remove the explicit Kotlin
plugin declaration from the root `build.gradle` and follow the
[built-in Kotlin migration guide](https://d.android.com/build/agp-upgrade-assistant).

```groovy
// build.gradle (root) — BEFORE
plugins {
    alias(libs.plugins.kotlin.android) apply false   // remove this line
    ...
}
```

Update `gradle/libs.versions.toml` — the `kotlin-android` plugin entry becomes
unnecessary after built-in Kotlin is configured.

---

## Step 3: Migrate kapt → KSP

**Add KSP plugin to the version catalog:**

```toml
# gradle/libs.versions.toml
[versions]
ksp = "2.0.21-1.0.25"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

**Apply KSP in each module that uses kapt:**

```groovy
// Each module build.gradle that had: id 'kotlin-kapt'
plugins {
    alias(libs.plugins.ksp)        // replace kotlin-kapt
    alias(libs.plugins.hilt.android)
}
```

**Replace `kapt` configurations with `ksp`:**

```groovy
// core/storage/build.gradle
dependencies {
    // BEFORE:
    // kapt libs.androidx.room.compiler
    // AFTER:
    ksp libs.androidx.room.compiler
}

// All modules with Hilt:
dependencies {
    // BEFORE:
    // kapt libs.hilt.compiler
    // AFTER:
    ksp libs.hilt.compiler
}
```

**Add `androidx.room` Gradle plugin** (required for KSP schema export):

```toml
# gradle/libs.versions.toml
[plugins]
androidx-room = { id = "androidx.room", version.ref = "room" }
```

```groovy
// core/storage/build.gradle
plugins {
    alias(libs.plugins.androidx.room)
}

android {
    // Remove kapt javaCompileOptions block if present
}

room {
    schemaDirectory "$projectDir/schemas"
}
```

This replaces the old `kapt { arguments { arg("room.schemaLocation", ...) } }`
pattern and silences the schema export warning.

---

## Step 4: Adopt the new AGP 9 DSL

Review the [AGP 9 release notes](https://developer.android.com/build/releases/gradle-plugin)
and [gradle-recipes](https://github.com/android/gradle-recipes) for DSL changes.

Common changes in AGP 9:
- `packagingOptions { }` → `packaging { }` (already deprecated in AGP 8)
- `buildTypes { release { minifyEnabled false } }` → `minify { enabled = false }` in some DSL forms

Update each module's `build.gradle`:

```groovy
// Replace deprecated packagingOptions
android {
    packaging {              // was: packagingOptions
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}
```

---

## Step 5: Handle BuildConfig fields

If any module uses custom `buildConfigField` entries, verify they still work
under the new AGP DSL. AGP 9 changed how `BuildConfig` is generated in
library modules.

For WisePrior, no custom `buildConfigField` entries are used — this step can
be skipped.

---

## Step 6: Clean up gradle.properties

After successful migration, remove any temporary flags:

```properties
# Remove if present:
# android.builtInKotlin=true            (temporary opt-in, now default)
# android.newDsl=true                   (temporary opt-in, now default)
# android.nonTransitiveRClass=true      (may now be enforced by default)
```

---

## Step 7: Verify the migration

```bash
./gradlew help               # must succeed without errors
./gradlew build --dry-run    # must complete without failures
./gradlew assembleDebug      # full build verification
```

---

## Known incompatibilities

- **Paparazzi < 2.0.0-alpha04** is incompatible with AGP 9. Upgrade Paparazzi
  before or alongside AGP 9 if screenshot tests are added.

---

## Verification checklist

- [ ] AGP updated to 9.x in `libs.versions.toml`
- [ ] Kotlin updated to 2.x (required for built-in Kotlin)
- [ ] KSP version is 2.3.6+ compatible
- [ ] Hilt updated to 2.59.2+
- [ ] All `id 'kotlin-kapt'` removed from module build files
- [ ] All `kapt libs.*` replaced with `ksp libs.*`
- [ ] `androidx.room` plugin applied in `:core:storage`
- [ ] `room { schemaDirectory }` configured (replaces kapt arg)
- [ ] `packagingOptions` renamed to `packaging`
- [ ] `./gradlew assembleDebug` passes cleanly
- [ ] Gradle 9.0 deprecation warnings resolved
