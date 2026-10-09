# Android foundation architecture

## Decision and authority

Use two Gradle modules: `app` for Android and `business` for Kotlin/JVM. This split is NexoraPe's architectural decision. The supplied three-page `mobile-arquitecture-guide.pdf` requires bounded contexts, four layers and inward dependencies, but does not prescribe two modules.

The guide's requirements take precedence over example repository layout. The initial foundation scope is preserved as history; the subsequent explicit implementation authorization adds IAM. Other bounded contexts are prepared as separate teammate change packages, not published as Carlos's contributions.

The JVM module prevents accidental compilation against Android's SDK through the Android module. It keeps business concepts independent of screens, storage and transport. Gradle declares only `app` → `business`; `business` has no Android, Compose, Retrofit, Room or DI framework dependencies. This boundary still needs code review when new dependencies and imports are introduced; it does not automatically enforce every future context boundary.

## Contexts and package names

Contexts belong immediately below `com.nexorape.safework` in both modules. The Gradle module name is not part of the package. Do not add `features/`, `business/` or a screen-named context between the root package and the context.

| Context package | Business scope | Module `business` | Module `app` |
| --- | --- | --- | --- |
| `com.nexorape.safework.iam` | Identity, authentication, user, company and roles | `domain`, `application` | `presentation`, `infrastructure` |
| `com.nexorape.safework.incidentmanagement` | Incident reporting, querying and handling, including Assignment | `domain`, `application` | `presentation`, `infrastructure` |
| `com.nexorape.safework.notificationmanagement` | User notifications | `domain`, `application` | `presentation`, `infrastructure` |

Context layout (IAM is implemented; Incident/Notification packages are separate upcoming integrations):

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

IAM now contains validated identity value objects, Domain repository interfaces, Application use cases, HTTP DTO-to-domain mapping, Keystore session storage and a Compose/ViewModel presentation. `business` still has no Android/transport dependency. No empty folders or fabricated feature classes are used.

`MainActivity` hosts IAM navigation. `SafeWorkApplication` owns one application-scoped composition graph so configuration changes do not create inconsistent session stores. `core/designsystem`, `core/network` and `core/configuration` are technical shared concerns, not business contexts. Network DTOs, bearer credentials and encrypted preferences remain outside Domain/Application. Session invalidation prevents late protected responses from restoring a signed-out UI.

## Four layers across the modules

| Layer | Responsibility and dependency rule |
| --- | --- |
| Domain (`business`) | Entities/aggregate roots preserve invariants. Immutable value objects validate constrained values. Repository interfaces describe domain operations without storage/HTTP types. Domain depends on no other layer. |
| Application (`business`) | Use cases orchestrate entities and domain repository interfaces. Use constructor parameters for dependencies. Keep Android, network, storage and framework DI wiring out. |
| Presentation (`app`) | Compose renders state; ViewModels invoke Application use cases and expose immutable UI state through a read-only `StateFlow`. UI input validation may guide interaction, but business invariants belong in Domain. |
| Infrastructure (`app`) | Implement Domain interfaces and translate DTO/local models into Domain objects in mappers before returning data to upper layers. Android capabilities, persistence, networking and DI wiring live here or in technical `core` packages. |

Compile-time dependencies point inward: Presentation → Application → Domain, and Infrastructure → Domain (and Application when needed). Composition in `app` supplies concrete implementations to use-case constructors. Neither Domain nor Application can import Presentation or Infrastructure. DTOs and local database entities must not cross the boundary into Presentation/Application/Domain.

The domain model is not determined by backend DTO shape. Cross-context business interactions must use deliberate contracts; do not turn shared technical `core` into a catch-all business package. Server authorization remains authoritative even when Android enforces useful local domain rules.

## Current visual and language foundation

`SafeWorkTheme` supplies explicit Material 3 light/dark colors. XML window themes and backgrounds follow system appearance during startup; Compose respects safe drawing insets and uses text resources. English is the default, with a Latin American Spanish welcome translation and supported `en-US`/`es-419` locale declarations. Localized IAM forms use scroll/IME insets and accessible labels; physical-device and accessibility checks require separate execution evidence.

## Sources

- Supplied `mobile-arquitecture-guide.pdf`, pages 1–3: strategic decomposition, root contexts, four layers and dependency/purity rules.
- Supplied `Trabajo Final_1ACC0238_202620.pdf`, pages 24 and 32–33: English naming, inclusive design, locales and default language.
- [Reference comparison](reference-review.md): exact example snapshots and deviations.
