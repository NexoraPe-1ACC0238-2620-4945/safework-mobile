# Incident handling and notifications: integration validation

9 October 2026 (America/Lima). Evidence timestamps use UTC. [PR #5](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-mobile/pull/5) targets `test` and remains open for review; no merge or deployment is performed by this update.

## Reviewed code and contribution history

- Integration base, confirmed after fetch: `3887c49b8b67f6582f4d4bb7bf399089ff68cf60` (`origin/test`).
- Francisco's reviewed head: `8af3f12c8285d18dc48cfd9081f939a60309ed77`. His commits `4f4fea9` and `8af3f12` are preserved without rewriting or changing authors.
- Follow-up source correction: `894a31b16823295e9fe194a7685db51063f41942` (`fix(notifications): hide empty state when a query fails`), committed with Carlos Mansilla's configured identity after explicit authorization.
- This PR includes the correction and updated validation documentation. The separate uncommitted `feature/ui-design-system` worktree and original incident-device documentation changes were preserved and are excluded from this publication.

The published increment adds actual models/rules/use cases, HTTP DTO mapping, screens/ViewModels, navigation, resources and tests. Daniel's incident model/query/report/GPS code is already in the base; do not reapply the delivery patches or bulk-copy package files. Assignment remains in Incident Management, Notification Management is separate, and both use shared IAM sessions. Domain/Application remain pure Kotlin in `business`; Android Presentation/Infrastructure remain in `app`.

The earlier offline ZIP preparation was not a Git/SDK execution. Its original source base was `060817fa723c52534eba0aae10973ad6dff8c527`. At initial review, 21 of 24 original payload files matched byte-for-byte; the differences were two explicit IPv4 mock-server bindings and historical-verification qualification. That provenance does not replace the fetched base or current executed results.

## Verified behavior and correction

EMPLOYER self-assignment sends `incidentId` only; the server derives the responsible user. Start and close use the shared incident state progression and responsible/company authorization. The mobile never authorizes by comparing display names. Notifications query only the authenticated recipient, without a selector, read marking, push or invented incident link.

A physical simulated 503 exposed a misleading presentation state: the notification screen displayed both the safe error and "No notifications" after a failed query. The follow-up changes only the empty-state condition to require `state.error == null`. It does not change sessions, transport, contracts, business rules or backend code. After the fix, the same induced failure showed only the generic error; the arbitrary simulated server message was hidden, and Refresh recovered the list on HTTP 200.

## Actual automated verification

```powershell
.\gradlew.bat :business:test :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug --no-daemon --console=plain
```

Initial published-head run: BUILD SUCCESSFUL in **1m 43s**, 105 tasks executed. Correction run: BUILD SUCCESSFUL in **1m 7s**, 22 tasks executed / 83 up-to-date; app tests reran and unchanged business tasks were up-to-date. Both report **31 tests, 0 failures, 0 errors, 0 skips**: 28 local/simulated tests and all three real local integration tests.

| Real local suite | Requests per verification run | Result |
| --- | ---: | --- |
| LiveIdentityTest | 20 | Passed |
| LiveIncidentTest | 20 | Passed |
| LiveHandlingNotificationTest | 51 | Passed |
| Total | 91 | No integration skipped |

Corrected current-course backend: `08b07675720d378db7a552c44d493f9c386d8375`, loopback port 18082, existing synthetic local MySQL on port 33317. Configuration/data were preserved; tests added isolated synthetic fixtures. No production database, deployed historical backend or historical fallback. Fixture credentials were supplied privately through the process environment and removed afterward; evidence contains methods/paths/statuses only.

Debug and **unsigned** release built. Lint: **0 errors / 9 existing warnings**: UnusedAttribute 1, AndroidGradlePluginVersion 2, GradleDependency 4, UseKtx 2. Existing LocationListener deprecation, nullable Java fixture and native-symbol packaging notices remain. No baseline/suppression. Staged whitespace and final documentation-link checks passed.

| Actual backend scenario | HTTP result |
| --- | --- |
| EMPLOYER takes OPEN incident | POST assignments 201; caller responsible |
| WORKER takes or starts | 403 |
| Another EMPLOYER repeats take | 409 |
| Same-company non-responsible starts/closes | 403 |
| Foreign-company detail/start/close | 404 |
| Responsible premature/repeated close | 409 |
| Responsible starts then closes | 200 / 200; state progression preserved |
| Read completed assignment | 200; CLOSED and persisted completionDate |
| Query current-recipient notifications | 200; reporter 1, responsible 3, peer/foreign 0; disjoint IDs |

Participants share synthetic display names to ensure authorization uses IDs. Sequential rejected operations did not change state or create extra notifications; this is not a concurrent-race test. Creation notifies reporter; take/start/close notify acting responsible. `isRead` remains a placeholder, not implemented read tracking.

## Physical execution and repeatable setup

Huawei CLT-L29, Android 10/API 29, authorized USB. Corrected APK installed with data retained. User-confirmed take/start/confirmed-close/notifications, agent-executed cancellation/reopen and basic text/touch/error checks are recorded in [physical validation](francisco-device-validation.md). Preparation HTTP requests are separate from physical actions.

```powershell
# At the reviewed worktree; adb must be available from your Android SDK:
adb devices
adb reverse tcp:18082 tcp:18082
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.nexorape.safework/.MainActivity
```

Use synthetic EMPLOYER fixtures and manual locations. Check OPEN -> take -> ASSIGNED -> start -> IN_PROGRESS; cancel close before confirming; then CLOSED and persisted completion date. Reopen from the list and query current-recipient notifications against the prior baseline. Never submit real GPS or expose tokens, passwords or invitation proofs in evidence.

The temporary loopback fault proxy injected 503 only for the phone notification request. This was a simulated fault on the real device, separate from the three real-backend integration suites. It was removed and the direct USB tunnel restored afterward.

## Remaining validation and team integration

Pending physical negative-role/company UI, session revocation on these screens, real network-timeout/conflict recovery, locale/time-zone changes, full TalkBack traversal, complete large-text matrix and other Android devices. Tests/labels/touch measurements do not certify every lifecycle race or accessibility scenario. Two tunnel-removal attempts were inconclusive, not successful network-error tests.

The separate UI work and this increment both touch SafeWorkNavigation. After PR #5 is reviewed/integrated into `test`, reconcile the visual tabs with PROFILE/INCIDENTS/NOTIFICATIONS and the existing callbacks; never overwrite Francisco's navigation with the older two-tab file. Follow feature -> test -> integrated validation -> later test -> main. No automatic merge or test-to-main PR now.
