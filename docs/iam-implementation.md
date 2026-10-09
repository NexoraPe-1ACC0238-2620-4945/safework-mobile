# Android IAM implementation

2026-10-08 (America/Lima). Branch `feature/iam`, based on updated `test` at `3f6fa5519cb60798063fdce496a658458b23b8ad`. Current-course backend foundation revision: `00a05cec8339e91c1422c7249ceeafa10d5d7079`, [backend PR #1](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-backend/pull/1). This is validated local integration while the server PR is under review, not deployment or historical API availability.

## Behavior and boundaries

- Login normalizes email, preserves the password, obtains the historical three-field authentication response, then validates the authoritative `/users/me` profile before saving a session or showing signed-in UI.
- Registration sends only `fullName`, `emailAddress`, `password`, `invitationToken`; no role/company override. The server validates the email/company-bound invitation and creates WORKER. Registration returns to login; it does not assume a token was returned.
- Profile reads and edits `/users/me`. Name and optional phone are editable; email/company/roles/password are not. Blank phone means omission/keep, matching the backend; clearing phone is deferred.
- Restoration reads the protected credential and calls the server before exposing a signed-in profile. Offline/server failure leaves a retry state, without trusting a cached user. No automatic renewal exists.
- Logout calls the server with no payload and always clears this device's credential. A 204 or already-invalid server 401 confirms the current session is unusable; network/server failure returns to login with an explicit local-only logout notice, without claiming server revocation. That server session may remain valid until expiry/administrative revocation. Other independent sessions remain valid.
- Protected 401 clears the matching credential and returns to login; 403 preserves a valid session and reports denied permission. App foregrounding refreshes the profile. Server expiry/revocation is observed on restoration/the next protected request; there is no idle timer claiming offline server validation.
- Late protected responses cannot restore signed-in UI after a session invalidation. Busy operations prevent duplicate UI submissions.

Contexts are directly below `com.nexorape.safework`. `business/iam/domain` owns validated identity values/profile/repository and safe failures; `business/iam/application` orchestrates operations. `app/iam/infrastructure` owns DTO mapping and protected storage; `app/iam/presentation` owns immutable read-only StateFlow/ViewModel and localized Compose navigation. Technical HTTP/session ports are in `core`, not business DTOs.

## Credentials and transport

AES-256-GCM encrypts the saved bearer credential with an Android Keystore key, random IV and authenticated version label. Only ciphertext is in private preferences; the root API URL is bound inside it to prevent reuse against another host. Passwords, invitation proofs and profile details are not persisted. Backup/device transfer are excluded by the existing manifest/rules. Corrupted/unreadable encrypted credentials are cleared, requiring login; forgetting an unreadable credential cannot claim server revocation.

No JWT/password/body/header logging, historical fallback, redirects or automatic request retries. Responses are size-limited; only fixed safe error categories are displayed. Request cancellation cancels the underlying call and closes responses. Failed validation/storage of a newly issued token attempts server logout; an unreachable server can still leave that new session until server expiry/revocation. Device compromise and Keystore hardware availability cannot be proved by JVM tests.

Release has no default endpoint and accepts root HTTPS URLs only:

```powershell
.\gradlew.bat :app:assembleRelease -Psafework.apiUrl=https://YOUR_APPROVED_HOST/
```

The placeholder is not a deployed API. Debug uses an explicitly started current-course local server at `http://127.0.0.1:18082/`. For USB, `adb reverse tcp:18082 tcp:18082`. For an emulator, `-Psafework.localApiUrl=http://10.0.2.2:18082/`. Only localhost/127.0.0.1/10.0.2.2 cleartext is allowed in debug; release prohibits cleartext. No credentials belong in Gradle properties.

Existing Gradle/Kotlin/Compose/SDK/JDK versions are retained. Added dependencies are used: Lifecycle2.8.7 for lifecycle-aware state/ViewModels, coroutines1.10.2 for Android/background cancellation, OkHttp4.12.0 for HTTP including PATCH, Gson2.13.2 for Infrastructure JSON, JUnit4.13.2/MockWebServer/coroutines-test for regressions. These are pinned compatible choices, not a copy of every professor library or a claim to latest versions. Sources: [Lifecycle releases](https://developer.android.com/jetpack/androidx/releases/lifecycle), [coroutines1.10.2](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.10.2), [Gson2.13.2](https://github.com/google/gson/releases/tag/gson-parent-2.13.2), [Android Keystore](https://developer.android.com/privacy-and-security/keystore), [network security configuration](https://developer.android.com/privacy-and-security/security-config).

## Verification and remaining checks

Final executed command: `:business:test :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug --no-daemon --console=plain`, BUILD SUCCESSFUL in50s, exit0. Three business rule tests, eight HTTP contract tests, two ViewModel regressions and one real local integration test: **14 tests, failures0/errors0/skips0**. The live test recorded **20 HTTP requests**, methods/paths/statuses only, against the backend revision above: invitation/signup201; login/profile/edit/restore200; incorrect login401; independent-session logout204 with the other profile200; role change200 followed by revoked profile401; new-role login200 and logout204.

Validated application/regression revision: `df8006a3e28993e3ada515f1329590f96f5d13dd` (including the local-only logout regression). Subsequent documentation edits do not change executable files.

Debug APK and unsigned release APK were packaged. Lint: **0 errors, 9 warnings** (retained/newer-version notices, API33 localeConfig on min26, and two suggestions to use KTX preference editing). Direct synchronous preference commits deliberately inspect the boolean persistence result; no warning baseline or suppression was added. Packaging retained a native library without symbol stripping; multiple Kotlin daemon sessions were reported. These warnings are not failed functional checks.

The initial lint pass found three missing includeSubdomains attributes in debug network exceptions; they were corrected with false, then reverified. After an interrupted subsequent command, the final command was run to completion; only its completed results are counted. Private logs/reports remain ignored under `.tools` and module build directories. Normal tests are run with:

```powershell
.\gradlew.bat :business:test :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug
```

The opt-in real HTTP test requires `SAFEWORK_LIVE_API_URL` pointing explicitly to loopback and `SAFEWORK_TEST_ADMIN_EMAIL`/`SAFEWORK_TEST_ADMIN_PASSWORD` containing synthetic operator fixtures. These must be supplied privately to a single-use Gradle process (`--no-daemon`), never versioned/logged. Without them the live test is skipped and cannot be claimed as run. It issues its own one-use invitation and unique synthetic user, and logs only HTTP methods/paths/statuses.

Pending physical checks: `adb devices` returned no connected device. IAM installation, interactive forms/navigation, actual Android Keystore restart/tamper behavior, API26/33+ coverage, accessibility/TalkBack and locale/theme/IME behavior on a device are not executed by JVM HTTP tests. Release packaging produces an unsigned APK; production signing/TLS/deployment are not supplied. Server operations follow-ups remain in backend `docs/verification.md`.

Incident/location/assignment/notification code is prepared in separate teammate packages; it is not part of the IAM publication or an ADMIN feature.
