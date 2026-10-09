# Francisco: integrating the existing `test` tree

Integration prepared on 8 October 2026 (America/Lima) against **the user-supplied** `safework-mobile-test.zip`. A GitHub `origin/test` reference could not be fetched/verified here because this is a repository ZIP without `.git` and without remote authentication. The package README identifies the known historic test commit `3887c49b8b67f6582f4d4bb7bf399089ff68cf60`; this is **provenance, not independent verification of the supplied ZIP SHA**. Do not infer that the remote is still at that SHA.

## Integration executed

- Applied *only* the Francisco `changes.patch` with `git apply --check --whitespace=error` then `git apply --whitespace=error`. Daniel's patch was not reapplied.
- Source package checksum passed and all 24 modified/new package files match the original `manifest.json` SHA-256.
- Source payload: responsible self-assignment, assignment state checks, incident start and close with confirmation, current authenticated recipient notification list, localized resources, and related Kotlin tests. No alternate assignee selection, ADMIN screen, read marking, or push delivery.
- Updated this README and added the integration document to reflect this **combined** source tree; no source code behavior was changed beyond the unmodified Francisco patch.
- Checked XML resource well-formedness and reference consistency. Gradle tests/build/lint and physical device validation **were not executed** on this merged ZIP: the runner has neither Android SDK/JDK17 nor a cached Gradle 8.11.1 distribution, and the wrapper cannot resolve `services.gradle.org`.

## Import changes into your real Git checkout

**Safest approach:** use the accompanying `francisco-sobre-test.patch` against a *clean* local checkout of the real and freshly fetched `origin/test`. Review differences before applying. The patch includes the 24 Francisco files, README changes, and this integration documentation. Do not use a `main` base.

```powershell
# Inside your locally cloned safework-mobile repository (PowerShell)
git status --short                 # clean worktree required
git remote -v                      # confirm correct repository
git fetch origin
git rev-parse origin/test         # record actual SHA; verify it includes Daniel
git switch -c feature/incident-handling-notifications origin/test
# Update the path below to where the patch was extracted.
git apply --check --whitespace=error "C:\ruta\francisco-sobre-test.patch"
git apply --whitespace=error "C:\ruta\francisco-sobre-test.patch"
git diff --check
git diff --stat
```

If this patch doesn't apply because `test` changed after the uploaded ZIP, **do not force it or replace existing source files**: inspect and resolve the differences manually before proceeding. Alternatively, open the provided integrated source tree in Android Studio for review, without treating the ZIP as a Git branch.

## Validate before commit and pull request

Use JDK17, Android SDK Platform/Build Tools35, Gradle wrapper8.11.1 and the **current corrected backend** with only synthetic fixture credentials when running opt-in live tests. Follow the current backend configuration in `docs/iam-implementation.md`. Test with a valid local API or approved HTTPS endpoint, not the old backend version.

```powershell
.\gradlew.bat :business:test :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug --no-daemon --console=plain
if ($LASTEXITCODE -ne 0) { throw "Validation failed - do not publish as passed" }
git diff --check
```

Check at least role/company ownership, real persisted transitions, notification recipient isolation, revoked sessions, and physical phone confirmation/refresh/error states. Some live integration tests are opt-in and **skip without their environment values**; look at the test report, not merely the Gradle `BUILD SUCCESSFUL` line.

```powershell
git config user.name
git config user.email
# Configure your OWN real name/email if not already present; never use another contributor's identity.
git add README.md docs/incident-handling-notifications.md docs/francisco-integration-on-test.md app/ business/
git diff --cached --check
git diff --cached --stat
git commit -m "feat(incidents): add lifecycle handling and recipient notifications"
git push -u origin feature/incident-handling-notifications
# Create a GitHub PR: base=test; compare=feature/incident-handling-notifications
```

Do not make the commit or push if tests fail or there are unexpected files/secrets staged. Confirm the real branch base and PR destination. No commits, pushes, PRs, merges, actual Android build, live backend calls, or device validation were performed during this offline ZIP integration.
