# Android foundation architecture

## Decision and authority

Use two Gradle modules: `app` for Android and `business` for Kotlin/JVM. This split is NexoraPe's architectural decision. The supplied three-page `mobile-arquitecture-guide.pdf` requires bounded contexts, four layers and inward dependencies, but does not prescribe two modules.

The guide's requirements take precedence over example repository layout. The current user request limits this work to the Android foundation; later course deliverables and examples do not authorize implementing features now.

The JVM module prevents accidental compilation against Android's SDK through the Android module. It keeps business concepts independent of screens, storage and transport. Gradle declares only `app` → `business`; `business` has no Android, Compose, Retrofit, Room or DI framework dependencies. This boundary still needs code review when new dependencies and imports are introduced; it does not automatically enforce every future context boundary.

## Contexts and package names

Contexts belong immediately below `com.nexorape.safework` in both modules. The Gradle module name is not part of the package. Do not add `features/`, `business/` or a screen-named context between the root package and the context.

| Context package | Business scope | Module `business` | Module `app` |
| --- | --- | --- | --- |
| `com.nexorape.safework.iam` | Identity, authentication, user, company and roles | `domain`, `application` | `presentation`, `infrastructure` |
| `com.nexorape.safework.incidentmanagement` | Incident reporting, querying and handling, including Assignment | `domain`, `application` | `presentation`, `infrastructure` |
| `com.nexorape.safework.notificationmanagement` | User notifications | `domain`, `application` | `presentation`, `infrastructure` |

Future package layout, shown only as documentation:

```text
business/src/main/kotlin/com/nexorape/safework/
  iam/{domain,application}/
  incidentmanagement/{domain,application}/
  notificationmanagement/{domain,application}/

app/src/main/java/com/nexorape/safework/
  MainActivity.kt
  core/designsystem/theme/Theme.kt
  iam/{presentation,infrastructure}/
  incidentmanagement/{presentation,infrastructure}/
  notificationmanagement/{presentation,infrastructure}/
```

Create packages and classes as real use cases arrive. No placeholder entities, repositories, DTOs, ViewModels or feature screens are included. The existing business `package-info.kt` contained only a comment and a generic `...safework.business` package declaration; its contents were inspected and preserved in the ignored pre-review backup before replacement with module documentation.

`MainActivity` is the Android entry point and hosts the static foundation screen. `core/designsystem` is a technical shared concern explicitly allowed by the guide, not a business context. Device location will be an Android capability integrated into actual use cases; its feature branch does not make it an additional business context.

## Four layers across the modules

| Layer | Responsibility and dependency rule |
| --- | --- |
| Domain (`business`) | Entities/aggregate roots preserve invariants. Immutable value objects validate constrained values. Repository interfaces describe domain operations without storage/HTTP types. Domain depends on no other layer. |
| Application (`business`) | Use cases orchestrate entities and domain repository interfaces. Use constructor parameters for dependencies. Keep Android, network, storage and framework DI wiring out. |
| Presentation (`app`) | Compose renders state; future ViewModels invoke Application use cases and expose immutable UI state through a read-only `StateFlow`. UI input validation may guide interaction, but business invariants belong in Domain. |
| Infrastructure (`app`) | Implement Domain interfaces and translate DTO/local models into Domain objects in mappers before returning data to upper layers. Android capabilities, persistence, networking and DI wiring live here or in technical `core` packages. |

Compile-time dependencies point inward: Presentation → Application → Domain, and Infrastructure → Domain (and Application when needed). Composition in `app` supplies concrete implementations to use-case constructors. Neither Domain nor Application can import Presentation or Infrastructure. DTOs and local database entities must not cross the boundary into Presentation/Application/Domain.

The domain model is not determined by backend DTO shape. Cross-context business interactions must use deliberate contracts; do not turn shared technical `core` into a catch-all business package. Server authorization remains authoritative even when Android enforces useful local domain rules.

## Current visual and language foundation

`SafeWorkTheme` supplies explicit Material 3 light/dark colors. XML window themes and backgrounds follow system appearance during startup; Compose respects safe drawing insets and uses text resources. English is the default, with a Latin American Spanish welcome translation and supported `en-US`/`es-419` locale declarations. Previews aid manual inspection, but are not evidence of successful runtime or accessibility testing.

## Sources

- Supplied `mobile-arquitecture-guide.pdf`, pages 1–3: strategic decomposition, root contexts, four layers and dependency/purity rules.
- Supplied `Trabajo Final_1ACC0238_202620.pdf`, pages 24 and 32–33: English naming, inclusive design, locales and default language.
- [Reference comparison](reference-review.md): exact example snapshots and deviations.
