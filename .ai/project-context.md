# Project Context: WisePrior

## Tech Stack
- **Language:** Kotlin (Targeting latest stable)
- **UI:** Jetpack Compose (UDF - Unidirectional Data Flow)
- **Asynchrony:** Coroutines & Flow
- **Architecture:** Clean Architecture + Feature-First Modularization
- **DI:** Constructor Injection (Standard)
- **Testing:** JUnit 5/4, MockK, Turbine (for Flows)
- **CI/CD:** GitHub Actions + JaCoCo (Target: >90% Coverage)

## Modularization Strategy
- `:app`: Entry point, DI graph, Navigation orchestration.
- `:feature:*`: Screen-specific logic, ViewModels, UI components.
- `:core:domain`: Pure Kotlin. UseCases, Repository Interfaces, Models. No Android dependencies.
- `:core:data`: Repository implementations, Room, Retrofit, Data sources.
- `:core:ui`: Design System, reusable Composables, Theme.
- `:core:common`: Utils, base classes, Coroutine dispatchers.

## Development Rules (The "Wise" Way)
1. **Domain is King:** No `android.*` imports in Domain layer.
2. **ViewModel Responsibility:** Only UI State management and Event handling. No business logic.
3. **Testability First:** If a class is hard to mock, it needs a boundary (Interface).
4. **Coverage:** Every PR must include unit tests. Use JaCoCo to verify.
5. **Compose:** Keep Composables stateless where possible (State Hoisting).
