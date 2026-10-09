# Incident handling and notifications: physical-device validation

9 October 2026 (America/Lima); evidence timestamps are UTC. [PR #5](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-mobile/pull/5), integration base `3887c49b8b67f6582f4d4bb7bf399089ff68cf60`. Original reviewed source `8af3f12c8285d18dc48cfd9081f939a60309ed77`; corrected source commit `894a31b16823295e9fe194a7685db51063f41942`. Backend `08b07675720d378db7a552c44d493f9c386d8375`, synthetic local data only. The separate UI-design work is preserved and not combined.

## Device, installation and fixture

Huawei CLT-L29, Android 10/API 29; adb reported `device`. Reverse tcp:18082 was confirmed. `adb install -r` succeeded, retained app data, and the app restored the synthetic EMPLOYER profile.

The initial APK SHA-256 was `4cf22faa048066491cd5f466bea8063c0a66de08be1c8976194b2b3441686cc3`. Corrected-code APK SHA-256: **`6eae1bca314b12d8dcec84bbbe7f38328f047317743bb1df2787477365d49db1`**; this is the APK installed for the final error/retry check. These hashes identify built artifacts, not a newly generated preview or the unrelated UI-design APK.

Prepared incident 84, title `Physical handling test 20261009-062224`, synthetic company 35 / actor 212. Separate setup POST incidents returned 201; the setup session was logged out without revoking the phone session. Notification baseline before creation: 1; after creation: 2. No real location or GPS submitted.

## Main workflow: actual execution

| Step | Physical observation | Server evidence | Result |
| --- | --- | --- | --- |
| Open OPEN incident | Correct title, Open, Take responsibility visible | GET /api/v1/incidents/84 200 | Checked |
| Take | User confirmed successful action | POST assignments 201 at 06:35:29; detail/assignment refreshes 200 | Checked |
| Start | User confirmed successful action | POST /incidents/84/start 200 at 06:35:39 | Checked |
| Confirm close | User confirmed successful action | POST /incidents/84/close 200 at 06:36:03; independent read CLOSED and completionDate present | Checked |
| Notifications | User confirmed viewing; agent refreshed and observed non-empty screen | GET my-notifications 200 at 06:37:39-40; independent read at 06:36:52: 5 total / 4 new | Checked |
| Reopen CLOSED detail | Agent returned to list, selected 84; CLOSED visible and no transition actions | GET /incidents/84 200 at 06:46:25 | Checked |

The four new notifications were one creation plus take/start/close events because this account was both reporter and responsible. Counts are time-specific: later user activity added another incident and was preserved. Individual notification cards were not exhaustively inspected. Server roles/company/responsible rejection and recipient isolation were exercised by the real integration tests, not repeated physically for every negative actor.

## Close cancellation

Separate synthetic incident 86 was prepared through setup calls (creation 201, assignment 201, start 200); these are not physical take/start results. On device the detail showed IN_PROGRESS and Close incident. At 06:43:33 the close dialog was visible; at 06:43:42 Cancel dismissed it. Independent read: still IN_PROGRESS, responsible 212, no completionDate. **Zero close requests and zero extra notifications** relative to its post-setup baseline. The fixture remains preserved, not reset or closed.

## Basic accessibility and text scale

Cancel/Close dialog controls exposed labels, enabled/focusable semantics and 48 dp touch height. Normal-scale navigation/Refresh/Back controls also measured 48 dp. Real system font scale 1.5 was checked: title/body wrap and scroll; Close and Refresh remain accessible at 48 dp. Notifications wraps awkwardly in the old navigation row; improve it in the separate visual increment. A partially clipped Back node measured 40.7 dp at a viewport edge; this does not establish its full target size at that scale. Complete large-text coverage is not approved.

Original scale 1.0 was restored. TalkBack is installed but was not activated/traversed; these semantics/size checks are not a full accessibility audit. Raw synthetic-detail screenshots were privately captured in ignored review artifacts, not generated mockups or committed assets.

## Simulated fault and corrected empty state

Two attempts to remove adb reverse did not yield an observable failure; both remain **inconclusive**, not passed network-timeout checks. The tunnel was restored. A temporary proxy bound only to loopback port 18083 forwarded to the corrected local backend and injected 503 only for the notification query. No headers, credentials or bodies were logged. This is a simulated server fault exercised through the real phone, not an actual backend 503 or an additional real-backend suite.

On source 8af3f12, the 503 showed the generic safe error **and** an inaccurate empty-list message. Retry 200 recovered the list. Commit `894a31b16823295e9fe194a7685db51063f41942` changes only the NotificationScreen empty-state condition to require `state.error == null`, preserving session/transport/business behavior.

The corrected APK was then installed with data retained. At 06:54:56, the screen showed the generic server error, **no empty-state message** and no arbitrary simulated server detail. At 06:55:02, Refresh had returned 200, cleared the error and restored a non-empty list. Fault/proxy removed, verified proxy process stopped, direct reverse 18082 -> 18082 and font scale 1.0 restored.

## Automated revalidation and boundaries

The full Gradle verification after the fix completed BUILD SUCCESSFUL in 1m 7s: **31 tests / 0 failures / 0 errors / 0 skips**, three real integrations / 91 requests; app tests reran, unchanged business tasks were up-to-date. Debug and unsigned release built. Lint **0 errors / 9 existing warnings**. The independently induced physical 503 verifies the visible regression; no test merely mirroring the condition was added. See [integration verification](francisco-integration-on-test.md) for commands and warning categories.

Remaining physical checks: negative-role/foreign-company UI, session revocation on these screens, actual network-timeout/conflict recovery, locale/time-zone changes, full TalkBack traversal, all text/device sizes and other supported Android versions. No user secrets, JWTs, invitation proofs, personal data or real coordinates belong in this record.

The source fix and this validation record are included in the PR update; Francisco's commits retain their authorship and Carlos's follow-up uses his real configured identity. The PR stays open for final review. No merge, backend modification or deployment is performed, and the UI-design branch remains separate.
