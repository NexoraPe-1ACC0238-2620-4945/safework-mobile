# Foundation reference review

Reviewed on 2026-10-07 (America/Lima). The requested scope is the Android foundation only. Attached course documents describe broader deliverables; example code supplies implementation references. Neither expands the authorized implementation scope.

## Repository inspection and preservation

`git remote -v` confirmed `https://github.com/NexoraPe-1ACC0238-2620-4945/safework-mobile.git` for fetch/push. The working directory is the existing mobile repository. The active branch was already `feature/android-foundation`, based on `df937475728099da938f4d1c36f29befb6d68fa3`. A fetch confirmed `main` and `origin/main` are aligned (ahead/behind `0/0`); no merge or branch recreation was needed.

Existing local branches: `main`, `feature/android-foundation`. Existing remote refs: `origin/main`, `origin/develop`, `origin/feature/android-tb1`, and symbolic `origin/HEAD` → `origin/main`. No `test` branch was created.

The prior foundation was uncommitted: README modified, plus Gradle/wrapper files, app sources/resources and business configuration untracked. Their contents were inspected; pre-review versions were copied to ignored `.tools/foundation-before-reference-review/`. The only removed source was the inspected, comment-only generic business package placeholder; no implemented behavior was removed.

Git identity inspection returned no configured `user.name` or `user.email`, including the available configuration origins. Neither value was changed. No commit, push, PR or merge was performed.

## Reference inventory

| Reference | Availability and relevance |
| --- | --- |
| `mobile-arquitecture-guide.pdf` | Read all three pages. Specifies root bounded contexts, four layers, inward dependencies, pure Domain/Application, entities/value objects and model translation. |
| `Trabajo Final_1ACC0238_202620.pdf` (V4.0, 52 pages) | Extracted text and reviewed foundation-relevant constraints, especially pp. 2, 24, 32–34. Kotlin native Android, DDD, English naming, English default, `en_US`/`es_419`, inclusion and collaboration are relevant. |
| ABET 7 Excel rubric | Read its populated worksheet. Evaluates individual acquisition/application of knowledge and continued professional learning; it does not prescribe Gradle modules or grant implementation scope. |
| Word cover template and Markdown cover template | Read. Word sample contains IdeaForge, NRC 3690 and period 202610; those are sample values, not NexoraPe's identity. Markdown uses placeholders and period 202620. No report cover was generated in this task. |
| NexoraPe TB1 report | No report found among supplied files or the inspected top-level Downloads/Documents/Desktop candidates. Its contents and SafeWork-specific requirements cannot be asserted. |
| Repeated pasted attachments | SHA-256 grouping found three distinct text contents repeated across attachments. They reproduce the task/context, not an independent TB1 report. The explicit user request governs this work. |
| EasyStore and EasyEvent | Accessible through GitHub repository/contents APIs after direct browser reads failed. Reviewed default `main` snapshots below. No example repository was modified or built. |
| SafeWork backend | Accessible but empty; see the [backend review](backend-contract-review.md). |

Example snapshots:

- [EasyStore `bddfc283b8120818dcd861665695a21afb6e26a4`](https://github.com/upc-pre-202620-1acc0238-4945/easystore-mobile-android/tree/bddfc283b8120818dcd861665695a21afb6e26a4)
- [EasyEvent `1b32f455642f8904dd9d4842f7676ebe91194340`](https://github.com/upc-pre-202620-1acc0238-4945/easyevent-mobile-android/tree/1b32f455642f8904dd9d4842f7676ebe91194340)

## Requirements, example decisions and adjustments

| Requirement or team decision | Example observation | Prior SafeWork state | Foundation adjustment |
| --- | --- | --- | --- |
| Guide: contexts directly below root, no generic `features/` | Both examples use `features/`; EasyEvent uses `features/home` as a screen-oriented grouping. | No context implementation; comment-only `...safework.business` placeholder. | Document `iam`, `incidentmanagement`, `notificationmanagement` at root in both modules; remove the inspected placeholder. |
| Guide: four layers, dependencies inward | Both have layer packages. EasyEvent defines a repository interface in Domain and maps `EventDto` in Infrastructure. | Kotlin/JVM `business`, Android `app` → `business`. | Keep the module boundary and specify layer responsibilities and future packages. |
| Guide: pure Domain/Application | Reviewed models are Kotlin; example use cases also import `javax.inject.Inject`. This is a DI annotation decision, not an Android SDK import. | Business has no framework dependencies or implemented logic. | Keep business constructor dependencies neutral; future DI wiring stays in app. |
| Guide: entities/value objects own invariants | Reviewed `Cart`, `CartItem`, `User` and `Event` are primitive-heavy data classes; reviewed Cart lacks the aggregate operations described by the guide. | No domain entities. | Document validation/value object and aggregate rules; do not invent business classes now. |
| Guide: immutable state via use-case-driven ViewModels | Reviewed Login/Home ViewModels call use cases and expose `StateFlow` through `asStateFlow`; UI state fields use `val`. | Static Compose screen, no stateful use case. | Retain a static shell; defer ViewModels until real use cases require them. |
| Team decision: two Gradle modules | Both examples include only `:app`. | `app` and `business`. | Keep and justify both modules; module count is not a course mandate. |
| Proposed toolchain: JDK 17, minimum API 26 | Examples use daemon JDK 25, Java source/target 11, minimum API 24, SDK 37, Gradle 9.6.0 and AGP 9.4.1. These are separate settings and example choices. | JDK/bytecode 17, API 26, SDK 35, Gradle 8.11.1, AGP 8.9.2, Kotlin 2.1.20. | Align AGP/Kotlin support ranges using AGP 8.10.1 and Kotlin 2.2.21; retain JDK 17, API 26 and SDK 35. |
| Only dependencies used by the foundation | Examples include Retrofit, Hilt/KSP, Room, DataStore, Coil and other feature libraries. | Activity Compose and Compose UI/Material/tooling only. | Keep the small dependency set. |
| Statement pp. 32–33: English default and Latin American Spanish | EasyEvent contains `values-es`; that qualifier does not specifically declare `es-419`. | Spanish README, brand-only resource. | English documentation/default strings, `values-b+es+419`, supported locale metadata and localized welcome. |
| Basic visual setup | Examples have a technical design-system theme package. | Default MaterialTheme and light-only startup XML. | Add a SafeWork light/dark theme under `core/designsystem`, matching startup resources and previews. |

Reviewed code supporting these observations includes each example's `settings.gradle.kts`, `app/build.gradle.kts`, version catalog, wrapper and daemon JVM properties; EasyStore's LoginUseCase/LoginViewModel, Cart/CartItem/User and ProductRepositoryImpl; and EasyEvent's EventRepository/GetEventsUseCase/EventMapper/HomeViewModel/HomeUiState. Observations concern these snapshots and files, not a full correctness audit of the examples.

## Version rationale

The prior project compiled, but compilation alone does not establish a fully supported plugin combination. [Kotlin's Gradle compatibility table](https://kotlinlang.org/docs/gradle-configure-project.html) places Kotlin 2.1.20–2.1.21's fully supported AGP range up to 8.7.2, below the previous AGP 8.9.2. Kotlin 2.2.20–2.2.21 supports Gradle 8.11.1 and AGP 8.10.1 within its published ranges.

| Setting | Foundation choice | Reason |
| --- | --- | --- |
| Gradle wrapper | 8.11.1 with distribution SHA-256 | Matches AGP 8.10's required Gradle version and preserves the complete existing wrapper. |
| AGP | 8.10.1 | Patch release in the compatible 8.10 line; supports SDK 35, Build Tools 35.0.0 and JDK 17. |
| Kotlin and Compose Compiler plugin | 2.2.21 for Android/JVM/Compose | Fits the selected Gradle/AGP range; compiler plugin uses the same version as Kotlin. |
| JVM toolchain and bytecode | 17 in app/business | Agreed proposal and Gradle/Android tooling compatibility. |
| `minSdk` | 26 | Team proposal: Android 8.0 and newer. This minimum is not a course mandate. |
| `compileSdk` / `targetSdk` | 35 / 35 | Retains the provisioned SDK and verified foundation target. Revisit target requirements for a later distribution/release task. |
| Compose BOM / Activity Compose | 2025.04.01 / 1.10.1 | Preserve the existing small UI stack. The BOM aligns UI libraries; it does not select the Kotlin compiler or Activity version. These are pinned versions, not claims of latest releases. |

Compatibility sources: [AGP 8.10 release notes](https://developer.android.com/build/releases/agp-8-10-0-release-notes), [Kotlin/AGP compiler support](https://developer.android.com/build/kotlin-support), [Compose compiler setup](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler), [BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping) and [Android resource qualifiers](https://developer.android.com/guide/topics/resources/providing-resources).

Newer dependency versions may be reported by lint. They should be evaluated as a coordinated toolchain/UI upgrade, with build and runtime validation, rather than copied automatically from the professor's examples.

## Course delivery boundary

The statement's TB1 section (p. 34) requires more than a foundation: updated report artifacts, deployed landing page, backend progress and core application screens. The overall project also includes persistence, a device capability, REST integration, an external service, autonomous learning and physical-device demonstration. None is implemented or marked complete here. Contributors must validate their own work and produce genuine evidence and commits under their own identities.
