# Foundation verification

Reviewed on 2026-10-07 (America/Lima), on `feature/android-foundation`.

## Initial foundation checks

The adjusted source was built with the project's complete wrapper:

```powershell
.\gradlew.bat :app:assembleDebug :business:build :app:lintDebug --console=plain
```

Machine-local settings used for this execution:

```powershell
$env:JAVA_HOME = (Get-ChildItem .tools/jdk -Directory | Select-Object -First 1).FullName
$env:ANDROID_HOME = (Resolve-Path .tools/android-sdk).Path
$env:GRADLE_USER_HOME = Join-Path (Get-Location) '.tools/gradle-user-home'
```

Environment: Windows, Temurin JDK 17.0.20.1, Gradle 8.11.1, AGP 8.10.1, Kotlin/Compose Compiler 2.2.21, Android SDK 35 and Build Tools 35.0.0.

| Check | Observed result |
| --- | --- |
| `:app:assembleDebug` | Passed; generated the debug APK |
| `:business:build` | Passed; generated `business/build/libs/business.jar`; unit test tasks were `NO-SOURCE` |
| `:app:lintDebug` | Passed with **0 errors and 5 warnings** |
| Combined build | `BUILD SUCCESSFUL in 3m 36s`, process exit 0; 48 tasks executed |
| APK inspection using SDK `aapt2` | Package `com.nexorape.safework`, minimum SDK 26, target/compile SDK 35, launchable `MainActivity` |
| Packaged welcome resources | English `Welcome to SafeWork` and `es-419` `Bienvenido a SafeWork` present |
| Manifest permission inspection | No INTERNET, location or notification runtime permission requested; AndroidX contributes an internal dynamic-receiver signature permission |
| Module/source review | Only app depends on business; business declares Kotlin/JVM and JDK 17, without Android/network/storage dependencies or implemented context classes |
| Git whitespace | `git diff --check` passed; newly written sources/resources were also read during review |
| Ignore rules | Tools, backup, build output, Gradle caches and `local.properties` are ignored |
| Git refs | HEAD, main and origin/main remain at `df937475728099da938f4d1c36f29befb6d68fa3` |
| Git identity | `user.name`: not configured; `user.email`: not configured; unchanged |

Artifacts and local logs (ignored by Git):

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- HTML lint report: `app/build/reports/lint-results-debug.html`
- Text lint report: `app/build/reports/lint-results-debug.txt`
- Build log: `.tools/reference-review-build.log`
- Original pre-review foundation: `.tools/foundation-before-reference-review/`

APK SHA-256: `3d3ba4e77fd8f58f8f5daf9c02f5702c0e22e791fc7b506080f53a93ff2f935c`.
Wrapper JAR SHA-256: `2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046`.

## Warnings and interpretation

Four lint warnings report newer versions of Gradle, AGP, Compose BOM and Activity Compose. The selected versions remain pinned for the documented compatible foundation toolchain; those warnings are not suppressed.

One `UnusedAttribute` warning notes that `android:localeConfig` applies only on API 33+. Older devices ignore this metadata and select the English/Spanish resources using their device locale. Runtime selection on API 26 and API 33+ remains a manual verification item.

The build also reported that `libandroidx.graphics.path.so` was packaged without stripping symbols. This was non-blocking; the APK was successfully generated. No claim of release size optimization is made.

## Files changed in this reference-review task

Compared with the preserved pre-review foundation, rather than only with Git HEAD:

Modified:

```text
README.md
gradle/libs.versions.toml
app/src/main/AndroidManifest.xml
app/src/main/java/com/nexorape/safework/MainActivity.kt
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
```

Added:

```text
.gitattributes
app/src/main/java/com/nexorape/safework/core/designsystem/theme/Theme.kt
app/src/main/res/values-b+es+419/strings.xml
app/src/main/res/values/colors.xml
app/src/main/res/values-night/colors.xml
app/src/main/res/values-night/themes.xml
app/src/main/res/xml/locales_config.xml
business/README.md
docs/architecture.md
docs/backend-contract-review.md
docs/reference-review.md
docs/verification.md
```

Removed after inspection and preservation in the ignored backup:

```text
business/src/main/kotlin/com/nexorape/safework/business/package-info.kt
```

That file contained only a documentation comment and generic package declaration. No implemented business behavior was removed. Existing `.gitignore`, wrapper files, root/module Gradle configurations, app icon and backup rules remain part of the uncommitted foundation.

Git HEAD only tracks the original README, so `git diff` alone does not show all foundation files. Use `git status` and `git ls-files --others --exclude-standard` when reviewing untracked content. No files were staged, and no commit, push, PR, merge or author change was performed.

## Checks not executed and their requirements

- Android Studio sync and Compose preview rendering: not executed in this command-line review; require a Studio GUI session with a compatible installation and JDK 17.
- Additional emulator/device coverage: API 26 and API 33+ behavior, theme transitions, locale selection, insets, font scaling and TalkBack remain pending. Installation and launch on one physical Android 10 device are verified below; that does not validate the full supported-device range or accessibility.
- Business unit and Android UI tests: no implemented business features or test suite exist. `NO-SOURCE` is not a passing functional test suite.
- Current-course backend endpoint, JWT, role, company-boundary and state-transition tests: not executed because that repository is empty and no running deployment/OpenAPI URL was provided. The historical backend was inspected as source only; see backend-contract-review.md.
- NexoraPe TB1 report comparison: unavailable among the supplied/inspected files. This foundation does not satisfy or claim completion of TB1.
- Linux/macOS wrapper execution: not executed on this Windows host; the README uses `sh ./gradlew` to avoid relying on a Windows filesystem executable bit.

Future contributors must validate their actual feature behavior and server contracts, using their own identities and genuine work evidence.

## Publication review and physical-device validation

The initial checks above record the earlier preparation state, including the then-missing Git identity. Before the authorized foundation publication:

- Branch confirmed: `feature/android-foundation`; remote: `NexoraPe-1ACC0238-2620-4945/safework-mobile`.
- Configured local Git identity: `Carlos Mansilla <c3sv.19@gmail.com>`. The agent did not set or change either value.
- Repeated `:app:assembleDebug :business:build :app:lintDebug --console=plain`: `BUILD SUCCESSFUL in 1s`, exit 0; 47 actionable tasks, 1 executed and 46 up-to-date. Business test tasks remain `NO-SOURCE`.
- Existing lint report: 0 errors and 5 warnings, explained above. No versions were changed or warnings suppressed for publication.
- USB device changed from `unauthorized` to `device` after the user accepted the debugging authorization.
- `:app:installDebug --console=plain`: installed on one CLT-L29 physical device running Android 10; `BUILD SUCCESSFUL in 37s`, exit 0.
- `adb shell am start -W -n com.nexorape.safework/.MainActivity`: `Status: ok`, cold launch, activity `com.nexorape.safework/.MainActivity`.
- The user confirmed that the welcome screen installed and opened correctly.
- Ignore checks confirmed that `.tools/`, `local.properties`, module build outputs and conventional secret/signing files are excluded. The complete Gradle Wrapper JAR is a required versioned project file, not an APK/build output.

Only the foundation and its documentation are authorized for publication. No application feature or backend integration is included. The foundation PR targets `main`; merging and creation of `test` remain subsequent user-controlled steps.
