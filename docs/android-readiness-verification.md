# Android readiness increment: executed verification

13 November 2026 (America/Lima). Base: `test` at `da1068e1a45cb54b7b25599cdd16e0389cbbb84f`. Branch: `feature/android-readiness`.

## Implemented increment

- Company/personal-report selection and all/open/assigned/in-progress/closed status filtering. Combined filters survive detail navigation, a document save and a refresh.
- Existing HTTPS document-reference editing through `PATCH /api/v1/incidents/{id}/document`. Input validation happens before transport; returned incident ID, company and reference are checked.
- Explicit external-map and validated document opening. An unavailable external application produces a safe message.
- English/Spanish resources, including quantity-aware report counts. Existing modules, session handling, API shapes and role model are retained.
- Completion matrix, learning notes and a backend contract proposal. The backend proposal is not implemented server functionality.

## Executed build and tests

Pinned Gradle 8.11.1, JDK 17, Android SDK Platform 35 and Build Tools 35.0.0. The downloaded Gradle distribution matched the repository wrapper's SHA-256 checksum. No project dependency or toolchain version was upgraded.

Equivalent repository command:

```sh
sh ./gradlew :business:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug \
  -Psafework.localApiUrl=https://safework-backend-q6id.onrender.com/ \
  --no-daemon --console=plain
```

The full run completed **BUILD SUCCESSFUL in 5m56s**, 57 tasks executed. An app-only rerun after plural-resource corrections completed in 1m10s, including app tests. The final Spanish `many` resource correction was checked with APK packaging and lint in 1m01s; Kotlin source/test code was unchanged for that final resource-only run.

| Suite | Tests | Passed | Skipped | Failures/errors |
| --- | ---: | ---: | ---: | ---: |
| business | 8 | 8 | 0 | 0 |
| app | 30 | 27 | 3 | 0 |
| Total | 38 | 35 | 3 | 0 |

The three skipped tests are `LiveIdentityTest`, `LiveIncidentTest` and `LiveHandlingNotificationTest`. They require an explicitly configured local backend and synthetic credentials. No local/live backend or credentials were supplied for this run. **The mock-server tests do not demonstrate a successful request to Render.**

Seven added tests exercise URL acceptance/rejection, the authenticated PATCH contract and its exact body, validation before transport, unexpected response identity/company/reference, permission/session handling, filter persistence across the workflow, cancellation and late-response rejection after invalidation. Updated existing repository fakes satisfy the added contract explicitly.

Lint: **0 errors, 4 warnings**: `UnusedAttribute` 1, `UseKtx` 3. The UseKtx recommendations cover two existing preference edits and the new URI conversion; no warnings are hidden or baselined. Existing location deprecation and nullable live-fixture compiler notices remain. The native-library stripping notice did not prevent packaging.

`git diff --check`, XML parsing, resource-name parity and Spanish plural-category checks passed.

## Candidate build identity

Path: `app/build/outputs/apk/debug/app-debug.apk`.

- SHA-256: `ff2d639ca147f3ae9337a987a75cc59d29ef787393cea122485ed34ea20156b7`.
- Size: 12,074,071 bytes.
- Application ID: `com.nexorape.safework`.
- Version code/name retained: `1` / `0.1.0`; the hash identifies this specific candidate.
- Minimum Android: API 26; target/compile API 35.
- Generated debug `BuildConfig.API_URL` verified as `https://safework-backend-q6id.onrender.com/`.
- Debug signed. It is not a signed production release or a store publication. To update an already installed APK while preserving data, use the same signing key as that installation.

## Public service and device boundary

Two read-only GET attempts to the public `/v3/api-docs` timed out after 65 and 45 seconds, with no response bytes. Current public availability was **not confirmed**. This result alone does not establish that the deployed service is down.

`adb devices -l` found no attached device in this workspace. No APK installation, physical UI test, new registration, real incident creation or document mutation against Render was performed. The October phone/HTTP records remain historical evidence for their own builds and environments.

Before a demonstration, follow the [device checklist](android-readiness-plan.md#demonstration-gate) with the actual phone and intended server. In particular, test the new external activities and permission-denied document save, then repeat the complete report/handling/logout flow.

This increment does not establish complete native-backlog delivery or a "99.9%" completion measure. File upload, push, internal notes, department and the other scope decisions remain in the [backend proposal](backend-completion-proposal.md).
