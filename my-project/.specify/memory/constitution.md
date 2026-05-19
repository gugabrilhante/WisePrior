<!--
Sync Impact Report
- Version change: 1.0.0 → 1.1.0
- List of modified principles:
    - I. Clean Architecture & Modular Boundaries (Expanded)
    - II. Test-Driven Development & Quality Gaps (Expanded with TDD cycle and determinism)
    - III. Total Testability & Dependency Management (Expanded with Composition over Inheritance)
    - IV. Code Quality & SOLID Principles (Renamed from Single Responsibility & SOLID, expanded with Documentation)
- Added sections:
    - User Experience (UX) Consistency (V. Design System & Theming, VI. System Integration & Polish)
    - Performance Standards (VII. Resource Stewardship & Efficiency, VIII. Perceived Performance)
- Templates requiring updates:
    - my-project/.specify/templates/tasks-template.md (✅ updated)
    - my-project/.specify/templates/spec-template.md (✅ updated)
- Follow-up TODOs: None.
-->

# WisePrior Constitution

## Core Principles

### I. Clean Architecture & Modular Boundaries
WisePrior follows a strict multi-module Clean Architecture pattern to ensure separation of concerns and maintainability.
- **Dependency Flow**: Dependencies must only point inwards (UI → Domain, Data → Domain). Domain must be pure Kotlin and have NO dependencies on Android frameworks or other modules.
- **Feature Isolation**: Feature modules (`:feature:*`) must never depend on each other. Shared logic must reside in `:core` modules.
- **Layer Integrity**: Data models (Entities/DTOs) must not leak into the UI. Use Domain models for business logic and UI models for presentation.

### II. Test-Driven Development & Quality Gaps (NON-NEGOTIABLE)
We prioritize correctness and reliability through automated testing and high coverage.
- **Coverage Mandatory**: Every new feature or logic change must include unit tests. We aim for >90% code coverage. Coverage gaps must be analyzed and addressed before finishing a task.
- **Red-Green-Refactor**: Follow the TDD cycle. Write failing tests first to define behavior, then implement the minimum code to pass, then refactor for quality.
- **Orphan Detection**: Every production class should have a corresponding Test file. Placeholder tests (e.g., `ExampleUnitTest`) must be removed.
- **CI First**: Any code that breaks the CI or introduces flakiness is a critical violation. Tests must be deterministic (no `Thread.sleep()`, use `TestDispatcher`).

### III. Total Testability & Dependency Management
The codebase must be 100% testable by eliminating static coupling and favoring composition.
- **Constructor Injection**: All dependencies must be passed via constructor. Avoid `object` singletons for business logic; inject them via Hilt.
- **Boundary Abstraction**: Direct access to system services (Time, Calendar, UUID, I/O) is prohibited in business logic. Inject abstractions (e.g., `TimeProvider`) to ensure determinism.
- **Composition over Inheritance**: Prefer building small, focused components and composing them rather than building deep inheritance hierarchies.

### IV. Code Quality & SOLID Principles
We write code for humans to read and machines to execute.
- **Single Responsibility**: A class or function must do exactly ONE thing. ViewModels should not exceed 300 lines; UseCases should be focused on a single business action.
- **Self-Documenting Code**: Use meaningful names for variables, functions, and classes. Use KDoc for public APIs to explain "why" rather than "how".
- **Interface Segregation**: Keep interfaces lean. If a Repository becomes too large, split it by domain entity.

## User Experience (UX) Consistency

### V. Design System & Theming
- **Design System First**: Use `WisePriorTheme` and shared components from `:core:designsystem` and `:core:ui`. Hardcoded colors, spacing, or typography are prohibited.
- **Adaptive UI**: Support Light/Dark mode and Dynamic Color. Ensure layouts are responsive to different screen sizes and orientations.
- **Accessibility (a11y)**: Every interactive element MUST have a proper `contentDescription`. Maintain minimum touch target sizes (48dp).

### VI. System Integration & Polish
- **Edge-to-Edge**: Full support for edge-to-edge display (SDK 35+) is mandatory. Handle system bars and IME insets correctly.
- **Feedback & Motion**: Provide immediate visual or haptic feedback for user interactions. Use standard Compose animations for state transitions to provide a "premium" feel.

## Performance Standards

### VII. Resource Stewardship & Efficiency
- **Off-Main-Thread**: All I/O and heavy computation must be offloaded to `IoDispatcher` or `DefaultDispatcher`. The Main thread is for UI updates only.
- **Memory Management**: Prevent memory leaks by using `collectAsStateWithLifecycle` and cleaning up resources in `onCleared`.
- **Database Optimization**: Use indices for frequently queried Room columns. Use selective projections (partial entities) to avoid loading unnecessary data.

### VIII. Perceived Performance
- **Optimistic Updates**: For local actions (like toggling a task), update the UI immediately before the database/network operation completes.
- **Graceful Loading**: Use skeletons or shimmer effects instead of generic progress bars for a smoother loading experience.
- **Startup Time**: Keep the Application class and Hilt initialization lean to minimize app launch time.

## Governance
- **Compliance**: All PRs and AI-generated code must be verified against this constitution.
- **Amendments**: Changes to these principles require documentation and a clear migration plan.
- **Supersedes**: This constitution supersedes all other ad-hoc development practices.

**Version**: 1.1.0 | **Ratified**: 2025-05-19 | **Last Amended**: 2025-05-19
