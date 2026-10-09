# IAM validation on a physical Android device

2026-10-08 (America/Lima; runtime evidence uses UTC on 2026-10-09). The requested guided registration/login/profile/restoration/logout/administrative-revocation checks were completed on the connected device. This report distinguishes captured HTTP results, tester observations, independent helper checks and remaining coverage. No credentials, invitation proofs, JWTs or real personal data belong in this document.

## Integrated backend and preserved local state

After `git fetch origin`, backend local and remote `main` and `test` all point to `08b07675720d378db7a552c44d493f9c386d8375`, the merge of [backend PR #1](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-backend/pull/1). The validated foundation commit `00a05cec8339e91c1422c7249ceeafa10d5d7079` is an ancestor. The complete Git trees are identical (`d201a46823081c7021cb98e218987d32b9142297`); no executable or documentation change was introduced by the merge. Backend is checked out on `test` and had no tracked/untracked changes before updating.

The existing MySQL instance was restarted using its existing private configuration and data directory on `127.0.0.1:33317`. It was not initialized, emptied or replaced. The existing JAR was started on `127.0.0.1:18082` with `DDL_AUTO=validate`, and `GET /v3/api-docs` returned **200**. Successful login/profile lookup of the existing synthetic operator additionally demonstrates that the prior test identity and company are still present. New invitations/accounts/session changes used in this guided test are intentional additions, not restoration of an empty database.

The prior complete Maven and HTTP results remain in the backend's `docs/verification.md`. The full suite was **not rerun**, because the integrated tree is identical to the validated tree. Server/data stay local; no deployment is implied.

## Confirmed USB installation and startup

Mobile remains on `feature/iam`, at `060817fa723c52534eba0aae10973ad6dff8c527` before this report. [Mobile PR #2](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-mobile/pull/2) targets `test` and has not been merged as part of this task. The working tree was clean before documentation changes.

The existing SDK is `.tools/android-sdk`, with its `platform-tools/adb.exe`. The existing debug APK uses `http://127.0.0.1:18082/`; there is no historical-host fallback.

| Check | Observed result |
| --- | --- |
| `adb devices -l` | One authorized Huawei CLT-L29, state `device` |
| Android release / SDK level | Android 10 / API 29 |
| `adb reverse tcp:18082 tcp:18082` | Successful; reverse listing shows the mapping |
| `adb install -r app/build/outputs/apk/debug/app-debug.apk` | `Success`; existing app data preserved |
| `adb shell am start -W -n com.nexorape.safework/.MainActivity` | `Status: ok`, cold launch, activity reported |
| Process and UI observation | App process present; SafeWork application nodes and an account-creation label present in the UI hierarchy |
| Installed APK SHA-256 | `6F5C51501698AE4E5AE23C06311B00F1CF2FAEE69D126274C5B732CD0F03227B` |

Only known UI labels/booleans are retained as evidence. UI field values and screenshots are not published. Installation/startup do not prove registration, login, secure restoration or revocation.

## HTTP setup actually executed

These calls were made by the local operator helper, **not by the phone**. Bearer credentials were held in memory and excluded from evidence. A fresh single-use, email/company-bound invitation was saved in a private, ignored runtime file for delivery to the tester. The helper did not register the user.

| Method and path (under `/api/v1`) | Status | Result |
| --- | --- | --- |
| POST `/authentication/sign-in` | 200 | Existing synthetic operator authenticated |
| GET `/users/me` | 200 | Existing operator/company confirmed |
| POST `/companies/{companyId}/invitations` | 201 | New synthetic device-test invitation issued |
| POST `/authentication/sign-out` | 204 | Only operator helper session closed |

## Guided functional checks — actual results

| Scenario | Procedure and expected result | Actual result |
| --- | --- | --- |
| Invitation registration | Use the private fixture's name, email, password and invitation code. Register; return to login with account-created notice. New user must be WORKER in the invitation company. | Tester confirmed "Account created" and return to login. Independent helper login/profile returned 200 and verified email/company/WORKER. Phone signup HTTP status was not captured. |
| Correct/incorrect login | Incorrect password must fail without showing a profile. Correct credentials must load the authoritative own profile. | Correct login confirmed by tester and access evidence: sign-in 200, own profile 200. Physical-device incorrect-password check not executed. |
| Profile | Edit the synthetic name and optional phone, save, then refresh; values must persist. Email/company/roles must not be editable. | Tester confirmed save/refresh; phone PATCH 200 and saved notice observed. Independent helper verified the edited name and synthetic phone (digits match, formatting differs), persisted across process restoration. Separate manual refresh HTTP event not identified. |
| Process restoration | While logged in, force-stop and reopen the app without clearing storage. Profile must be restored only after server validation, with no password re-entry. | Confirmed: force-stop/reopen succeeded; no credentials entered, GET `/users/me` 200 at 01:30:57 UTC, profile visible at 01:31:00 UTC. Actual stored credential successfully restored on this device; hardware/tamper properties are not proved. |
| Logout | Log out, then force-stop/reopen. Login must remain visible. Confirm server-side revocation independently; a UI return alone does not prove HTTP 204. | Tester confirmed return to login; access evidence shows sign-out 204 at 01:32:08 UTC. Force-stop/reopen kept login visible at 01:32:44 UTC, without a restore/profile request. |
| Administrative revocation | Log in again. Authorized backend operator changes this synthetic user's roles from WORKER to WORKER+EMPLOYER. On refresh/foreground restoration, old phone session must return to login. A new login must show updated roles. | New phone login/profile 200 at 01:33:22 UTC. Operator role change 200 at 01:33:44 UTC. Phone refresh 401 at 01:34:00 UTC; tester and UI observation confirmed return to login with session-invalid notice. Final login/profile both 200 at 01:34:55 UTC; tester and UI observation confirmed WORKER+EMPLOYER. |

The phone access evidence contains **10 IAM requests** (eight 200, one 204, one 401), excluding the earlier signup, whose status was not captured:

| UTC on 2026-10-09 | Method/path under `/api/v1` | Status | Phone operation |
| --- | --- | --- | --- |
| 01:29:26 | POST `/authentication/sign-in` | 200 | Initial login |
| 01:29:26 | GET `/users/me` | 200 | Authoritative initial profile |
| 01:30:09 | PATCH `/users/me` | 200 | Save edited profile |
| 01:30:57 | GET `/users/me` | 200 | Restore saved session after force-stop |
| 01:32:08 | POST `/authentication/sign-out` | 204 | Logout |
| 01:33:22 | POST `/authentication/sign-in` | 200 | New session before revocation |
| 01:33:22 | GET `/users/me` | 200 | New session profile |
| 01:34:00 | GET `/users/me` | 401 | Old session rejected after role change |
| 01:34:55 | POST `/authentication/sign-in` | 200 | New login with updated roles |
| 01:34:55 | GET `/users/me` | 200 | WORKER+EMPLOYER profile |

The local helper separately recorded **20 HTTP requests** across invitation preparation (4), registration-state verification (6), edited-profile verification (6), and administrative revocation (4). Ten of those precede access-log activation; ten also appear in the access file. They are counted once and are not phone requests. Two later OpenAPI startup probes are excluded from both IAM totals. No full battery or fabricated test cases are included in these counts.

For restoration, use the same connected device and SDK:

```powershell
$adbExe = Join-Path $PWD '.tools/android-sdk/platform-tools/adb.exe'
& $adbExe shell am force-stop com.nexorape.safework
& $adbExe shell am start -W -n com.nexorape.safework/.MainActivity
```

Do not use `pm clear`, uninstall, delete the Keystore credential or reset the test database. Keep the USB connection/reverse mapping and local server available. Force-stopping is performed only after the tester confirms the preceding step is complete.

Administrative revocation is a backend-only operator action, not an Android ADMIN screen. It uses `PATCH /api/v1/administration/users/{syntheticUserId}/roles` with `{"roles":["WORKER","EMPLOYER"]}`. This must actually change roles: a no-op update does not revoke sessions. Never target a different existing test user or publish the operator's credentials. A subsequent 401/profile return to login is the expected result; 403 with a valid session is a permission denial and should not be treated as logout.

After the tester confirmed registration, a separate helper login/profile validated the new account and closed its own session (200/200/204), independently of the phone. The operator helper also logged in/read its profile/logged out (200/200/204). This is server-state evidence, not a second registration or proof of phone login. Assisted field entry used the private fixture and left submission to the tester.

Before the phone login, the same integrated JAR was restarted with Tomcat access evidence enabled and JVM timezone UTC. The existing database was kept running and `DDL_AUTO=validate` retained. The private access pattern is `%t %m %U %s`: UTC timestamp, method, path without query, status; no headers, bodies, credential values or JWTs. Startup OpenAPI probes are setup checks, not IAM scenario calls. Registration preceded this access evidence, so its status cannot be retroactively claimed. Subsequent helper calls are distinguished from phone actions by their documented execution intervals.

## Remaining scope

The requested guided flows above passed with the recorded scope. Exact HTTP status codes for phone operations are not inferred from a UI success message; helper HTTP checks and persisted server state are identified separately. Registration succeeded according to the tester and independent server-state verification, but its phone HTTP status was not captured. Wrong-password UI behavior was not tested on this phone; previous backend/JVM regression evidence remains separate. API 26/33+ devices, Keystore tamper/hardware properties, offline/local-only logout, accessibility/TalkBack, additional locales and release signing remain unverified here. GPS/incidents/notifications are absent from this IAM APK.

No full suite rerun or executable code modification was necessary. Sanitized documentation is updated on `feature/iam` for the existing PR #2; no merge or deployment is performed. Backend and MySQL remain running on loopback for further local use, with the synthetic device user now holding WORKER+EMPLOYER and a newly validated phone session. Existing data, original branches and private ignored evidence/fixtures are preserved. `.tools`, `.local`, `local.properties`, generated APKs/build outputs and credential values are excluded from publication.
