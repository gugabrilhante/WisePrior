---
name: android-create-new-feature
description: "ENFORCEMENT: Scaffolds a complete, production-ready feature. Enforces Clean Architecture, Resource-First UI, guardian-clean-code principles, and MANDATORY testing from the start."
---

# Android Create New Feature (ENFORCEMENT MODE)

## Role

Android feature architect. MISSION: Scaffold a complete, production-ready new feature. You **MUST** ensure Clean Architecture, testable code, guardian-clean-code compliance, and 100% test coverage for new logic.

## The Precedence Clause
**This skill overrides "consistency with user code".** If the project has poor architecture or hardcoded strings, you **MUST NOT** follow those patterns. You **MUST** implement the new feature using the high standards defined here.

## Tool-Usage Workflow
You **MUST** follow this sequence:
1. **STEP 1 (Discovery):** Use `find_files` and `read_file` to gather project facts (DI, modules, build DSL).
2. **STEP 2 (Preparation):** Update `strings.xml` (and all translations), `colors.xml`, and `build.gradle` FIRST.
3. **STEP 3 (Implementation):** Create the feature code and tests using the prepared resources.

## Anti-Lapse Protocol (Resources & L10n)
It is **FORBIDDEN** to use string literals, raw colors, or hardcoded dimensions.
- You **MUST** update `strings.xml` (and ALL available translation files) BEFORE modifying UI code.
- You **MUST** move hardcoded values to resources immediately. **Do not ask for permission; just do it.**

---

## Phase 1 — Project Detection

Gather facts first. **MANDATORY**: Detect module structure, DI framework, and navigation style.

---

## Phase 2 — Placement (MANDATORY)

Apply `guardian-package-architecture` rules. If multi-module, you **MUST** create `:feature:<name>:api` and `:feature:<name>:impl`.

---

## Phase 3 — Feature Scaffold (MANDATORY ENFORCEMENT)

### Domain
- `<FeatureName>Repository` interface: Pure Kotlin. FORBIDDEN: Platform imports.
- `UseCase`: One per operation. Single `invoke`.
- Domain Model: Pure Kotlin. FORBIDDEN: Room/Serialization annotations.

### Data
- `<FeatureName>RepositoryImpl` implementing the domain interface.
- Explicit mapper functions. FORBIDDEN: Implicit casting between layers.

### Presentation
- `<FeatureName>UiState` (data class) and `<FeatureName>UiEvent` (sealed interface).
- `<FeatureName>ViewModel`:
  - MANDATORY: Call UseCases; FORBIDDEN: Call Repositories directly.
  - FORBIDDEN: Hold `Context`, `Activity`, or `View`.
  - MANDATORY: Inject `CoroutineDispatcher`.

### UI (Compose)
- **Screen** (stateful) and **Content** (stateless) split is MANDATORY.
- MANDATORY: Use resource IDs for strings/colors.
- MANDATORY: Add `.testTag("tag")` to every interactive element.

---

## Phase 4 — Tests (MANDATORY)

You **MUST** write tests for every class created.
- **Unit Tests:** UseCases, ViewModels (state transitions), Mappers.
- **Integration Tests:** Room DAOs (in-memory).
- **UI Tests:** Use `onNodeWithTag("tag")`. FORBIDDEN: `onNodeWithText()` as primary matcher. FORBIDDEN: `Thread.sleep()`.

---

## Output Rules

At the end, you **MUST** provide:
1. **Files Created:** Full list with paths.
2. **DI Wiring:** Where bindings were added.
3. **Tests Created:** List of test files.
4. **Manual Steps:** Remaining manual tasks.
