---
paths:
  - "app/src/main/kotlin/**"
  - "domain/**"
  - "data/**"
  - "feature/**"
---

# Naming rules

Three combined sources: Uncle Bob (Clean Code), official Kotlin, and professional Android practice. On conflict, that is the priority order.

## Uncle Bob, applied to Kotlin/Android

| Rule | Bad | Good |
|---|---|---|
| Name reveals intent | `d`, `data`, `tmp` | `courseId`, `filteredStudents`, `elapsedMs` |
| No disinformation | `studentList` (it's a `List`) | `students` |
| Meaningful distinction | `getCourse` vs `fetchCourse` vs `retrieveCourse` | one verb per concept |
| Pronounceable | `genDtTmStmp` | `generatedAt` |
| Searchable (no magic literals) | `if (grade == 2)` | `if (grade == LiteralGrade.A)` |
| Classes: nouns | `DataProcessor`, `Manager` | `StudentRepository`, `CourseDetailViewModel` |
| Functions: verbs | `course()`, `data()` | `loadCourse()`, `buildState()` |
| One word per concept | `fetch` in one place, `get` in another | pick one and use it across the codebase |
| No humor or jargon | `whack()`, `eatMyShorts()` | `delete()`, `clear()` |

A name that needs a comment to be understood is the wrong name. Rename it instead of explaining it — see `kotlin-style.md`, comments.

## Official Kotlin

- **Classes / objects / interfaces**: `PascalCase` — `DashboardViewModel`, `MviState`
- **Functions / properties**: `camelCase` — `loadCourse()`, `isLoading`
- **Constants** (`const val`, companion, top-level): `SCREAMING_SNAKE_CASE` — `SEARCH_DEBOUNCE_MS`
- **Packages**: `lowercase.nounderscores`
- **Backing properties**: `_` prefix + same name — `_state` / `state`
- **Lambdas**: use `it` only if the context is obvious in 2 lines or fewer; otherwise name the parameter
- Prefer `val` over `var`; prefer extension functions over utility classes

## Patterns by layer

| Type | Pattern | Examples |
|---|---|---|
| ViewModel | `<Feature>ViewModel` | `DashboardViewModel` |
| UseCase | `<Verb><Subject>UseCase` | `GetCoursesUseCase`, `RecordAttendanceUseCase` |
| Repository (interface) | `<Entity>Repository` | `CourseRepository`, `EvaluationRepository` |
| Repository (impl) | `<Entity>RepositoryImpl` or `<Source><Entity>Repository` | `SqlDelightCourseRepository` |
| UiState | `<Feature>UiState` — data class, all fields `val` | `DashboardUiState` |
| UiIntent | `<Feature>UiIntent` — sealed interface, past tense or noun-verb | `QueryChanged`, `FilterToggled`, `SaveClicked` |
| UiEffect | `<Feature>UiEffect` — sealed interface, describes the effect | `NavigateBack`, `ShowMessage` |
| Exposed Flow/StateFlow | name without the `Flow` suffix | `val courses: Flow<List<Course>>`, not `coursesFlow` |
| Booleans | `is`, `has`, `can`, `should` prefix | `isLoading`, `hasSession`, `canSave` |
| Callbacks in a Composable | `on` prefix | `onIntent`, `onClick`, `onDismiss` |
| Suspend fun | name it as if it were synchronous | `fetchById()`, not `fetchByIdSuspend()` |

## Additional rules

- `UiIntent` names describe **what the user did**, not what the ViewModel should do: `DeleteClicked`, not `TriggerDelete`.
- `UiEffect` describes **the resulting effect**, not the action: `NavigateBack`, not `GoBack`.
- A private ViewModel function takes the `handle` prefix only when it groups several sub-cases. If it does one thing, name it directly: `loadCourse()`, not `handleLoadCourse()`.
- Avoid redundant prefixes inside a scope: inside `CourseDetailViewModel`, `loadCourse()`, not `loadCourseDetail()`.
