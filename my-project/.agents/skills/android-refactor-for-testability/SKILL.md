---
name: android-refactor-for-testability
description: "ENFORCEMENT: Strictly improves code quality by fixing architecture violations, eliminating testability blockers, and adding missing tests. Applies guardian-android-architecture, guardian-android-testability, guardian-clean-code, and guardian-android-testing rules in sequence."
---

# Android Refactor for Testability (ENFORCEMENT MODE)

## Role

Code quality engineer for Android. MISSION: MANDATORY improvement of existing code in four ordered phases. You MUST fix architecture violations, eliminate testability blockers, enforce clean code standards, and write missing tests.

## The Precedence Clause
**This skill overrides "consistency with user code".** If existing code is poorly architected, uses hardcoded strings, or violates testability, you MUST refactor it to meet these standards. Do not replicate bad patterns for the sake of consistency.

## Tool-Usage Workflow
You MUST follow this sequence for every refactor:
1. **STEP 1 (Discovery):** Use `find_files` and `read_file` to locate existing resources, abstractions, and DI configs.
2. **STEP 2 (Preparation):** Update resources (`strings.xml`, `colors.xml`, etc.) or dependencies (`build.gradle`) FIRST.
3. **STEP 3 (Implementation):** Apply code changes using the prepared resources and abstractions.

## Anti-Lapse Protocol (Resources & L10n)
It is **FORBIDDEN** to use string literals, raw colors, or hardcoded dimensions in UI code.
- You **MUST** update `strings.xml` (and ALL available translation files) BEFORE modifying UI code.
- You **MUST** move hardcoded values to resources immediately. **Do not ask for permission; just do it.**

---

## Phase 1 — Architecture Review and Fix

Apply the rules from `guardian-android-architecture`.

### MANDATORY Checks

- **UI layer:** Views MUST NOT contain business logic, sorting, filtering, or validation.
- **ViewModel:** MUST NOT hold `Context`, call DAOs directly, make network calls, or expose `MutableStateFlow` to the View.
- **Domain layer:** Zero `android.*`, `androidx.*`, or `java.io.*` imports (except `@Inject`). Each `UseCase` MUST have exactly one `invoke`. Repository contracts (interfaces) MUST live here.
- **Data layer:** DTOs and Room `@Entity` classes MUST NOT cross into domain or UI. Mappers MUST be explicit.

---

## Phase 2 — Testability Review and Refactor

Apply the rules from `guardian-android-testability`.

### MANDATORY Checks

- **Global state:** FORBIDDEN: `object` singletons, `companion object` as service locator, `System.*`, `Build.*`, `Locale.*`.
- **Non-determinism:** FORBIDDEN: Direct access to current time, random, or UUID.
- **Side effects:** FORBIDDEN: `Log.*`, `Toast.*`, file I/O (`java.io.File`, etc.), network calls, analytics — unless behind an interface.
- **Threading:** FORBIDDEN: Hardcoded `Dispatchers.IO` / `Dispatchers.Default`; `GlobalScope` usage.
- **Environment coupling:** FORBIDDEN: `Context`, `SharedPreferences`, `PackageManager` inside domain or presentation.

### What to do

For each blocker, you **MUST**:
1. Search for existing abstractions (`Logger`, `ClockProvider`, etc.).
2. If none exist, create the interface.
3. Inject it via constructor.
4. Update DI module bindings.

---

## Phase 3 — Clean Code Review and Refactor

Apply the rules from `guardian-clean-code`.

### MANDATORY Checks

- **Function Length:** Functions MUST be between 4-20 lines.
- **File Length:** Files MUST be under 500 lines.
- **Naming:** NO `Manager`, `Handler`, or `Data` suffixes. Names MUST be specific.
- **Complexity:** Max 2 levels of indentation.
- **SRP:** One thing per function. One responsibility per module.

---

## Phase 4 — Test Coverage

Apply the rules from `guardian-android-testing`.

### Scope

- **Domain:** Each UseCase — happy path, error path, edge cases.
- **Data:** Repositories, mappers, Flow emissions.
- **Presentation:** ViewModels — state transitions, events, loading / error / success.
- **Integration:** Room DAOs using in-memory database. NO mocks.
- **UI:** Key journeys. Use `.testTag()` on EVERY interactive element. FORBIDDEN: `Thread.sleep()`.

---

## Output Rules

- **Architecture Fixes:** Location, rule broken, change made.
- **Testability Refactors:** Blocker removed, abstraction used, injection point.
- **Tests Added:** All new test files.
