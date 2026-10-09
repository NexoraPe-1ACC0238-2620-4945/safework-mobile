# SafeWork Android

Native Android application for SafeWork, by NexoraPe. UPC course 1ACC0238, academic period 202620, NRC 4945.

## Current scope

The app implements IAM: login, invitation-based WORKER registration, own profile and profile editing, validated session restoration and server logout. Incident Management supports current-company list/detail/reporting, editable manual location with optional one-shot device coordinates, responsible self-assignment and start/close lifecycle actions. Notification Management adds the current authenticated recipient's notifications. SafeWork keeps localized English/Latin American Spanish UI, light/dark appearance and safe drawing insets.

- `app`: Android, Compose Presentation and HTTP/secure-storage Infrastructure.
- `business`: pure Kotlin Domain/Application, identity value objects, repository contracts and use cases.
- Dependency direction: `app` → `business`.
- Base package/application ID: `com.nexorape.safework`; Android 8.0/API26 minimum, compile/target35, JDK17.

Android ADMIN functions, refresh tokens, reassignment, push notifications and mark-as-read are not included. This is **not the complete TB1 delivery**. The corrected current-course backend runs locally for validation; no deployed API is supplied. The IAM and Daniel prerequisites are retained from `test`. PR #5 adds Francisco's incident handling and notifications, plus a follow-up fix that prevents a failed notification query from displaying a false empty state. Corrected-code verification passed 31 tests without failures or skips, including three real local integrations (91 HTTP requests), debug/unsigned-release builds and lint (0 errors / 9 existing warnings). Physical take/start/confirmed-close/notification consultation, cancellation, reopening and simulated 503 recovery were checked. Full TalkBack, broader large-text/device coverage and negative physical UI checks remain pending. See the validation records below.

The UI increment adds a shared violet design system with bundled Raleway/Montserrat fonts and retained OFL licenses, a compact SafeWork header and bottom navigation (Profile, Incidents, Alerts / Perfil, Incidentes, Avisos). IAM forms, incident cards/actions and notification cards reuse the theme; password visibility is accessible and temporary. Business rules, HTTP contracts and IAM sessions are retained. See [UI verification and physical review limits](docs/ui-design-system-validation.md) for this increment; earlier feature checks do not certify every redesigned screen.

## Documentation

- [Architecture and planned context packages](docs/architecture.md)
- [Reference comparison and toolchain rationale](docs/reference-review.md)
- [Current and historical backend contract review](docs/backend-contract-review.md)
- [Foundation verification record](docs/verification.md)
- [IAM implementation, configuration and verification](docs/iam-implementation.md)
- [IAM physical-device validation and remaining checks](docs/iam-device-validation.md)
- [Incident query/reporting correction, inventory and actual verification](docs/incident-query-reporting.md)
- [Incident handling and recipient notifications](docs/incident-handling-notifications.md)
- [Francisco integration and automated validation](docs/francisco-integration-on-test.md)
- [Francisco physical-device checks and remaining limitations](docs/francisco-device-validation.md)
- [Branded UI, previews, verification and physical review limits](docs/ui-design-system-validation.md)
- [Mobile API contract](docs/mobile-api-contract.md)

Documentation and default UI resources use English. Latin American Spanish uses `values-b+es+419`. Supported locale tags are `en-US` and `es-419`; the SafeWork brand name is not translated. Android selects resources from the device/app locale and falls back to English. No custom language-selection flow is implemented.

## Prerequisites

- JDK 17, both as `JAVA_HOME` and Android Studio's Gradle JDK.
- An Android Studio release compatible with AGP 8.10.1 and the Kotlin 2.2 plugin. The [Studio compatibility table](https://developer.android.com/studio/releases) lists AGP 8.10 support starting with Meerkat Feature Drop 2024.3.2; a compatible newer Studio can be used.
- Android SDK Platform 35, Build Tools 35.0.0 and Platform-Tools, with their licenses accepted.
- Internet access for the first Gradle/plugin/dependency download.

The complete Gradle 8.11.1 wrapper is included with a distribution SHA-256 checksum. A global Gradle installation is unnecessary. Versions are pinned in `gradle/libs.versions.toml`: AGP 8.10.1, Kotlin/Compose Compiler 2.2.21, Compose BOM 2025.04.01 and Activity Compose 1.10.1. See the reference review for compatibility and upgrade considerations.

## Open and run in Android Studio

1. Choose **Open** and select the repository root containing `settings.gradle.kts`.
2. Go to **Settings > Build, Execution, Deployment > Build Tools > Gradle** and select a JDK 17 installation as **Gradle JDK**. Use the project's Gradle wrapper.
3. In **SDK Manager**, install Platform 35, Build Tools 35.0.0 and Platform-Tools. Review and accept the SDK licenses.
4. Sync the project with Gradle. Android Studio normally writes the SDK path to the ignored `local.properties` file.
5. Create an API 26+ emulator in **Device Manager**, or connect a physical Android 8.0+ device with USB debugging enabled.
6. Select the `app` run configuration and the device, then click **Run**.
7. Configure the current-course local backend or an approved HTTPS endpoint as described below; test IAM and localized light/dark UI.

Tools provisioned locally for this review are under ignored `.tools/`: JDK 17 under `.tools/jdk/` and SDK under `.tools/android-sdk/`. They are machine-local conveniences and are not distributed with the repository.

## Build from the command line

Set `JAVA_HOME` to JDK 17 and `ANDROID_HOME` to the SDK installation. Alternatively, set `sdk.dir` in `local.properties`, for example `sdk.dir=C:/Users/your-user/AppData/Local/Android/Sdk` on Windows.

From the repository root in PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :business:build :app:lintDebug
```

On Linux/macOS:

```sh
sh ./gradlew :app:assembleDebug :business:build :app:lintDebug
```

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Lint report: `app/build/reports/lint-results-debug.html`.
- Install on a connected device: `gradlew.bat :app:installDebug` on Windows, or `sh ./gradlew :app:installDebug` on Linux/macOS. Then launch SafeWork from the device.

`business:test` runs IAM rule tests; `:app:testDebugUnitTest` runs transport/ViewModel tests. The real HTTP test is opt-in and needs synthetic local backend fixtures; see the IAM report. `NO-SOURCE` and skipped tests do not demonstrate executed coverage.

## Backend configuration

Release has no default API URL. Supply an approved root HTTPS URL with `-Psafework.apiUrl=https://YOUR_APPROVED_HOST/`. No historical fallback exists. Debug defaults to `http://127.0.0.1:18082/`, for an explicitly started local current-course backend; it does not start a server.

For a USB phone, run `adb reverse tcp:18082 tcp:18082` before launching. For the Android emulator, build with `-Psafework.localApiUrl=http://10.0.2.2:18082/`. Cleartext is limited to localhost/127.0.0.1/10.0.2.2 in debug and prohibited in release. Do not put credentials in Gradle properties or build configuration.

Registration needs an invitation issued outside the mobile app by a permitted server operator for the same email/company. Roles and company cannot be selected publicly. Signup does not create a session; log in afterwards. Logout always clears this device; network failure is explicitly shown as unconfirmed server revocation, not a successful server logout. A revoked/expired session returns to login on restoration or the next protected request. See [IAM details](docs/iam-implementation.md).

## Git workflow and contribution

1. Foundation: `feature/android-foundation` → PR to `main`.
2. After approval and integration, create `test` from `main`.
3. Features: `feature/...` → PR to `test`.
4. Validation in `test` → PR to `main`.

Planned feature areas have included `feature/iam`, `feature/incident-query`, `feature/incident-reporting`, `feature/incident-management`, `feature/notifications` and `feature/device-location`. **For this Francisco delivery, the requested combined branch is `feature/incident-handling-notifications`, based on updated `origin/test`, with the PR targeted to `test`.** Branch names describe work allocation; they do not define bounded contexts. Assignment belongs to Incident Management.

Use Conventional Commits. The proposed foundation commit is:

```text
chore(android): initialize project foundation
```

Before committing, review `git status`, `git diff`, untracked files, `git config user.name` and `git config user.email`. Each contributor reviews, implements and validates their own work and commits with their own Git identity. Do not fabricate contributions or change authors to create evidence.

The Android foundation is already integrated in `main`, and mobile `test` exists. IAM targets `test`; validation in `test` precedes a PR to `main`. The separate backend foundation is integrated, and its `main`/`test` share commit `08b07675720d378db7a552c44d493f9c386d8375`. Existing branches are preserved; no automatic merge is performed.
