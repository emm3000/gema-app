---
paths:
  - "app/src/main/kotlin/**"
  - "core/*/src/*/kotlin/**"
  - "feature/*/src/*/kotlin/**"
---

# Design principles

Selected for what this project actually is: a single-developer, offline-first, single-device Android app with no backend, no login, no external consumers and no public API. Principles that pay off in a large team or a published library are not automatically worth their cost here.

When two principles collide, the order below decides.

## 1. YAGNI — first, and not by accident

Do not build for a requirement that does not exist yet. No extension points for hypothetical variants, no configuration nobody sets, no abstraction with one implementation.

Weigh every "we might need it later" against the app's own history of overbuilding and later deletion.

## 2. KISS

The simplest design that satisfies the requirement wins. Clever wins nothing.

## 3. Make illegal states unrepresentable

In Kotlin this buys more than most classic acronyms.

- Model identifiers and constrained strings as value classes, not raw `String` — `CourseId`, `StudentId`.
- Model mutually exclusive states as a `sealed interface`, not as a set of nullable flags. If two booleans can never both be true, they should not both exist.
- Never derive identity from displayed content. Two elements that render the same text are not the same element. Key shared state by an explicit id the caller owns, never by the text itself.

## 4. Fail fast

An object that exists is valid. Validate in `init` with `require` and reject the construction otherwise.

Do not return a silently degraded object and let the caller discover the problem later.

## 5. SLAP — one level of abstraction per function

A function either orchestrates named steps or performs one step. Never both. This is the rule that most directly serves readability.

## 6. SOLID

| Letter | Status here |
|---|---|
| **SRP** | Adopted. One reason to change per class. A ViewModel that also formats strings and also talks to two repositories has three. |
| **ISP** | Adopted. Small, purpose-built interfaces. |
| **DIP** | Adopted, and it is the layer seam. The domain declares the interface, the infrastructure implements it — see `architecture.md`. |
| **OCP** | Adopted **only after the second real variant appears**. Applied early it is YAGNI with a respectable name: an extension point for a variant that never arrives is dead complexity. Rule of three — duplicate twice, abstract on the third. |
| **LSP** | Low ceremony. Inheritance is rare here; sealed hierarchies and composition make it nearly moot. Do not build type hierarchies to satisfy it. |

## 7. DRY — with the caveat that matters

DRY is about **one source of truth for a piece of knowledge**, not about code that looks alike.

In a layered architecture the two get confused constantly. A domain model, a SQLDelight row, a network DTO and a `UiState` will often carry the same field names. Collapsing them "because DRY" couples the layers and destroys the architecture: a database schema change then reaches the UI directly.

Those four shapes change for different reasons, so they are not duplication. Mapper files exist precisely to keep them apart. Do not "fix" them.

Real DRY violations are duplicated **rules**: the same due-date calculation in two places, the same validation in three. Those get extracted.

## 8. Supporting principles

- **Composition over inheritance.** Idiomatic in Kotlin and required by Compose.
- **CQS.** A function either changes state or answers a question, never both. This maps onto MVI directly: intents command, state answers.
- **Tell, don't ask.** Give the domain model the behavior instead of pulling its fields out and deciding elsewhere.
- **Principle of least astonishment.** A reader should be able to guess what a name does and be right.

## Deliberately not adopted

- **Boy Scout Rule as usually stated.** It conflicts with the work-unit commit discipline: opportunistic cleanup pollutes the diff and makes the commit unreviewable. Bounded version — cleanup is allowed inside a file you are already changing for the work unit's own reason. Anything else becomes its own commit.
- **Law of Demeter as a law.** In Compose and MVI, reading `state.course.name` is normal, and enforcing the law produces wrapper bloat. Treat it as a smell worth noticing, not a rule to obey.
- **Comprehensive KDoc.** See `kotlin-style.md`. The code explains itself or it gets renamed.

## Patterns

Use a design pattern when it names a problem you actually have. Do not introduce one to demonstrate that you know it. A pattern applied without the problem is complexity with a nice label.

Already worth staying consistent with: Repository, Use Case (command object), Mapper, Factory (value class `invoke`), Observer (`Flow` / `StateFlow`).
