# Incident query/reporting and optional location

2026-10-08 (America/Lima). Corrective branch: `feature/incident-reporting-complete`, created from fetched `origin/test` at `2c3dccaaf7c896c765b3955d22efe5bd1ef26846`. This correction incorporates the centrally prepared incident/location **code and tests that were omitted from PR #3**. It does not reimplement the features or change the backend.

[PR #3](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-mobile/pull/3) changed only this document. Its physical-check assertions are not supported by the retained feature report, which marked those checks pending. The earlier package's isolated build/test results do not demonstrate incident implementation in that documentation-only PR's tree. This correction reports only checks actually executed here; it does not certify unrecorded physical claims.

Existing Daniel commit `26993c567aeb7b3a957c220a2191ff1d543a0ace` remains unchanged in history. The user explicitly authorized new corrective commits/publication as **Carlos Mansilla / c3sv.19@gmail.com**, not as Daniel or another contributor. Original prepared source base was IAM `060817fa723c52534eba0aae10973ad6dff8c527`; the refreshed delivery was checked against `f60e85e41b1b2a947b62c77f11ff79ce36161474`. The actual correction base above supersedes those historical references.

Recovery used `daniel.zip` (SHA-256 `7ffe7fbc9ceb6c6295c77b02fd3969015072ad74115bd882f4b1d09e1dbf948c`) and its original patch (SHA-256 `fef3f560b641b54e2fba8d642f26a9e373d5ef4d7ed8ae8841f352111f17fbec`). `git apply --check --whitespace=error` and application excluded only the already existing `docs/incident-query-reporting.md`; all **17 code/resource/test payloads** were recovered, including modifications to existing files. They match the prepared payloads after line-ending normalization, with no code compatibility corrections required. The existing document was inspected and retained during application, then corrected deliberately. IAM source, shared authenticated transport/configuration and the later [physical IAM report](iam-device-validation.md) were preserved.

The single `incidentmanagement` context supplies one Incident model, positive IDs, validated title/description/location and OPEN/ASSIGNED/IN_PROGRESS/CLOSED. Pure Kotlin Domain/Application are in `business`; Android Presentation/Infrastructure are in `app`. Assignment/handling will extend this context in Francisco's prerequisite-dependent package. See [shared contracts](team-integration-contracts.md).

Features: current-company incident list, authorized detail, reporting, state/author/responsible display, retry/safe errors, manual location and optional one-shot coordinates. Report sends exactly title/description/location; caller/company are server-derived. All ten historical IncidentResource fields are mapped; no assignmentId/assigneeUserId is invented. Display names are not responsibility IDs.

Location is an optional Domain port invoked by an Application use case. Android implements it with LocationManager and runtime coarse/fine permission; no Android Location type crosses into business. Capture waits up to20s, accepts the granted precision, appends coordinates to the editable text and cancels on leaving/stopping the screen or manual location edits. Disabled/unavailable/denied location permits manual reporting. GPS/network hardware are optional; no background permission/service/tracking, last-known location reuse or structured coordinate API fields. Sources: [location permissions](https://developer.android.com/develop/sensors-and-location/location/permissions), [LocationManager](https://developer.android.com/reference/android/location/LocationManager).

Session invalidation clears list/detail/draft/identity and discards late responses. Navigation revalidates IAM on foregrounding. Local company/role/state checks guide UX and reject inconsistent responses; the server must independently authorize every route. No arbitrary company/user selector, public EMPLOYER grant, alternate responsible selection or historical fallback.

## Executed verification

The command below completed on the actual corrective branch: **BUILD SUCCESSFUL in 1m 43s, exit 0**. `business:build` includes its test task; app JVM tests, debug and unsigned release packaging and lint all completed. Some unchanged Gradle tasks were up-to-date; these are not additional test cases.

```powershell
.\gradlew.bat :business:build :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug --no-daemon --console=plain
```

The server was the **integrated current-course backend** at `08b07675720d378db7a552c44d493f9c386d8375`, listening on `http://127.0.0.1:18082/`, with its preserved local synthetic MySQL test database. OpenAPI readiness returned 200; no backend source, schema reset, historical deployment or production service was used. Existing test data was kept, with the live scenarios adding unique synthetic fixtures.

**21 JVM tests, 0 failures/errors/skips**: 14 inherited IAM plus 7 incident/location tests. Of these, **19 are local/simulated tests** (4 business rules, 11 MockWebServer HTTP contracts, 4 ViewModel tests using fake repositories/providers), and **2 are real integration tests** (one IAM, one Incident). Real integration recorded **40 HTTP requests**: 20 inherited IAM and 20 incident/setup calls. Real-HTTP success does not prove Android UI behavior or actual GPS. The location provider used by JVM regressions is fake; HTTP location text is synthetic/manual.

| Actual real Incident HTTP operation (under `/api/v1`) | Status | Checked outcome |
| --- | --- | --- |
| GET `/incidents` in the reporting company | 200 | Authorized list, including the newly reported incident |
| POST `/incidents` | 201 | Principal-derived reporter/company, OPEN state, text location |
| GET `/incidents/{incidentId}` in the reporting company | 200 | Detail matches the created incident |
| GET `/incidents` in another synthetic company | 200 | Created incident absent |
| GET `/incidents/{incidentId}` from the other company | 404 | Foreign detail denied |
| POST `/authentication/sign-out` for three test sessions | 204 each | Test session cleanup |

The remaining Incident integration calls provision/authenticate the synthetic operator/workers/company/invitations. Counts include setup; none is presented as a physical GPS or UI test. Live tests were configured privately through loopback-only environment variables. No skipped JVM tests were counted as passed. Physical/instrumentation tests were not run; Gradle `NO-SOURCE`/optional Android-test preparation tasks do not demonstrate such coverage.

Lint: **0 errors / 9 warnings**: UnusedAttribute1 (API33 localeConfig on min26), AndroidGradlePluginVersion2, GradleDependency4, UseKtx2. Separate Kotlin compiler notices cover the legacy LocationListener deprecated override and nullable Java environment fixture arguments. These compiler notices are not counted among lint's nine warnings. Native-symbol packaging notice remains. No suppressive lint baseline or framework upgrade was added.

**Pending physical checks:** incident list/detail/report UI, manual location entry, actual one-shot GPS, approximate/precise/denied permissions, disabled settings, timeouts and cancellation during edits/navigation/background/logout; integrated IAM navigation/restoration/revocation; API26/33+, locale/theme/IME/insets and TalkBack/accessibility. No incident/GPS physical checks were executed for this correction. Prior IAM device checks are separate history and do not approve the new navigation/location UI. Release is unsigned; production URL/TLS/signing/deployment/migration and pagination are not supplied. Server authorization evidence is distinct from local response consistency checks.

Commands/fixtures match [IAM testing](iam-implementation.md), including opt-in loopback environment variables and private synthetic operator setup. Without opt-in fixture configuration both live tests are skipped and may not be reported passed. Do not log/password/commit tokens or fixture secrets. Recipient should rerun tests before publishing `feature/...` -> PR to `test`; no automatic merge.

## Recovered code and tests

All paths below are actual changes relative to the corrective base, rather than documentation-only declarations:

| File | Recovered responsibility |
| --- | --- |
| `app/src/main/AndroidManifest.xml` | Coarse/fine permissions; optional location/GPS/network hardware |
| `app/src/main/java/com/nexorape/safework/MainActivity.kt` | Mount shared navigation with the existing IAM graph |
| `app/src/main/java/com/nexorape/safework/core/navigation/SafeWorkNavigation.kt` | Profile/incident navigation and foreground IAM validation |
| `app/src/main/java/com/nexorape/safework/incidentmanagement/infrastructure/http/HttpIncidentRepository.kt` | Authenticated list/detail/report and safe failures |
| `app/src/main/java/com/nexorape/safework/incidentmanagement/infrastructure/http/IncidentDto.kt` | Ten-field DTO-to-domain mapping |
| `app/src/main/java/com/nexorape/safework/incidentmanagement/infrastructure/location/DeviceLocation.kt` | Cancellable one-shot Android location provider |
| `app/src/main/java/com/nexorape/safework/incidentmanagement/presentation/IncidentScreen.kt` | List/detail/report forms, manual/optional location and permissions |
| `app/src/main/java/com/nexorape/safework/incidentmanagement/presentation/IncidentViewModel.kt` | Immutable UI state, operations, invalidation and capture cancellation |
| `app/src/main/res/values/incident_strings.xml` | English resources |
| `app/src/main/res/values-b+es+419/incident_strings.xml` | Latin American Spanish resources |
| `business/src/main/kotlin/com/nexorape/safework/incidentmanagement/domain/model/Incident.kt` | Incident/identifiers/status/text rules |
| `business/src/main/kotlin/com/nexorape/safework/incidentmanagement/domain/repositories/IncidentRepository.kt` | Pure Kotlin repository/location ports and safe failures |
| `business/src/main/kotlin/com/nexorape/safework/incidentmanagement/application/IncidentUseCases.kt` | Company-consistent queries/reporting and location orchestration |
| `business/src/test/kotlin/com/nexorape/safework/incidentmanagement/IncidentRulesTest.kt` | Domain/validation regression |
| `app/src/test/java/com/nexorape/safework/incidentmanagement/IncidentHttpTest.kt` | Simulated HTTP contracts/mapping |
| `app/src/test/java/com/nexorape/safework/incidentmanagement/IncidentViewModelTest.kt` | Fake-provider state/invalidation/cancellation regressions |
| `app/src/test/java/com/nexorape/safework/incidentmanagement/LiveIncidentTest.kt` | Real local reporting/query/company-isolation scenario |

This report, README and architecture documentation are corrected alongside those 17 files. ZIPs, tools, local configuration, logs, credentials and generated outputs remain ignored and excluded. New commits use the user's authorized Carlos identity; the existing Daniel commit remains an ancestor without rewritten authorship.

## Corrective PR and integration order

Publish `feature/incident-reporting-complete` -> **PR to `test`**, accurately describing restoration of prepared code/tests missing from PR #3, the actual base and results above. Do not claim physical checks passed, merge automatically or deploy. Francisco **waits until this corrective PR is integrated**, then fetches updated test and has his separate package checked against that resulting tree. Presence of the document alone is not his prerequisite. Validate the integrated Daniel+Francisco increment before preparing `test` -> `main`.

Validated implementation commit: f9c7401586caa4017e6deffc4e13fcf754e7058f. Validated implementation/tests revision: 165b61c6d05e97e9675089f01a44b10d64516223. Subsequent documentation-only changes do not change executable files.
