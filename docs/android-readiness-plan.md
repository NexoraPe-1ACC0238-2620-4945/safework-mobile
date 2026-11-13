# Android completion and Flutter preparation

Review date: 13 November 2026 (America/Lima).

## Goal and baseline

Bring the native Android app to a demonstrable, agreed completion scope, then develop a substantial Flutter increment. "99.9%" is not a measured completion percentage. Each required story needs working behavior and evidence.

Baseline: `test` at `da1068e1a45cb54b7b25599cdd16e0389cbbb84f`. `main` still points to the initial foundation, `3f6fa5519cb60798063fdce496a658458b23b8ad`. Work from the integrated `test` branch. Do not replace it with the foundation, rewrite history or merge unverified work into `main`.

The current report on `TB1`, reviewed at `469906ba3126a39d78216a370c1ee61541a59dc4`, contains older execution statements. Its product backlog remains a scope reference, but those statements do not override the actual integrated code or later device results. This increment does not alter the report or claim that its omissions have been approved.

## Coding and learning agreement

- Discuss work and explain code in English.
- Follow the professor's feature-oriented separation of Domain, Application, Infrastructure and Presentation. The existing Android module boundary (`app` -> `business`) stays intact.
- Primary Flutter reference: [easybook-mobile-flutter](https://github.com/upc-pre-202620-1acc0238-4945/easybook-mobile-flutter/tree/e7047168a059824970f731a3b0872de153474e5d). Observed conventions include repository interfaces, use cases, DTO mapping, `ChangeNotifier`, `ListenableBuilder`, `get_it`, `http` and `sqflite`.
- Explain the purpose, execution flow and relevant syntax of each increment. Classroom navigation-only login is not real authentication; SafeWork must use its existing authenticated API.
- Discuss substantial restructuring, feature-scope changes and backend changes with the user before performing them.
- Keep credentials out of source, logs, screenshots and APK assets. Preserve existing session, company and responsible-user checks.

## Scope matrix

| Report story | Reviewed Android baseline | Completion work |
| --- | --- | --- |
| US04 registration | Invitation-based WORKER registration exists | Repeat on the intended demo environment; compare all agreed acceptance criteria. |
| US05 login | Login, session restoration and logout exist | Repeat on the physical phone against public HTTPS. |
| US06 roles | Server grants roles; no public privilege selection | Reconcile the report's role-selection wording. Do not add privilege selection to public registration. |
| US09 accident report | Text and optional GPS exist | Photo evidence requires an upload/storage design; not completed by a URL reference. |
| US10 incident report | Text and optional GPS exist | Repeat device submission and subsequent query. |
| US11 external reference | Location and documentUrl exist in the server contract | Add explicit external-map opening and existing-document reference editing; device check pending. |
| US12 report review | Company-scoped list/detail exist | Confirm any additional prioritization acceptance criteria with the team. |
| US13 responsibilities | EMPLOYER self-assignment exists | Reassignment or selection of another responsible user requires a separate agreed API change. |
| US14 state updates | OPEN -> ASSIGNED -> IN_PROGRESS -> CLOSED exists | Repeat complete responsible-user workflow on the final APK. |
| US15 status visibility | List/detail display status | Repeat after state changes. |
| US16 personal report history | Company list existed | Add "My reports" filtering by authenticated user ID; this is not a timestamped event timeline. |
| US21 push notifications | In-app HTTP notification queries only | Requires device-token registration, delivery provider and backend integration. |
| US23 technical documents | Existing server HTTPS document reference endpoint | Add reference editor/opening; actual file upload remains pending. |
| US24 photo evidence | No capture/upload flow | Requires file storage, media metadata and authorized upload/read lifecycle. |
| US25 internal case notes | No reviewed notes API or client | Requires an agreed data model, access rules and backend endpoints. |
| US27 report details | Detail and responsible-user actions exist | Verify new references alongside existing actions. |
| US28 administrator report screen | ADMIN does not have global mobile incident access | Discuss the intended administrator use case and company scope before changing permissions. |
| US32 area/department | Own profile edits name/phone only | Requires a persisted profile field and compatible request/response changes. |
| US36 required fields | Domain validation and server validation exist | Check visible validation and keyboard/scroll behavior on the phone. |
| US39 status filter | Not present in baseline UI | Add all/open/assigned/in-progress/closed filters; combine with personal-report filter. |
| US40 audit | Some server administrative operations are audited | This does not implement an audit screen or a history of every platform action. Define scope and API first. |

## Ordered work

1. **Compatible Android increment:** status and personal-report filters; existing HTTPS document reference; external-map opening. Keep current backend contracts and architecture. Verify domain, HTTP and ViewModel regressions, compile, lint and APK configuration.
2. **Backend proposal for discussion:** photo/document upload and storage; push; internal notes; department; any required administrator/audit views. Define authorization and persistence before implementation. Do not silently remove stories to declare completion.
3. **Finish agreed native scope:** implement approved backend/client changes, add meaningful tests and complete phone verification. Reconcile the report with the implemented behavior and evidence.
4. **Integration and demonstration:** feature -> PR to `test` -> integrated checks -> reviewed promotion to `main`. Identify the APK by SHA-256 and source revision. Use public HTTPS, without localhost or `adb reverse`.
5. **Flutter increment:** create a separate Flutter project/repository while retaining native Android. Propose the precise feature folders before authoring. Build a vertical flow of login, profile, incident list/detail/report and logout using the professor's conventions, then add the remaining agreed features. A Kotlin-to-Dart rewrite does not require replacing the backend.

## Backend changes that need discussion

| Capability | Concrete proposal to review |
| --- | --- |
| Media | Authorized file-upload metadata API plus a storage provider; per-company access; type/size limits; private read links; cleanup behavior. Android selects/captures a file and uploads it through the approved flow. No storage credential in the client. |
| Push | Store token against authenticated user/device; server delivery on selected incident events; token revocation; distinguish in-app records from accepted and delivered push. Select a provider before adding dependencies. |
| Notes | Company- and incident-scoped note records; create/list endpoints for authorized SST users; define whether workers can read them. |
| Department | Optional persisted department field in own profile; validation limits and a compatible migration; extend profile DTOs and PATCH deliberately. |
| Audit/admin | Decide which actions and which company-scoped readers are in scope; add a protected query API if required. Existing ADMIN must not become an unrestricted mobile incident reader by accident. |

These are proposals only. No backend deployment, database modification or provider setup belongs to this compatible Android increment.

## Demonstration gate

- Install the exact reviewed APK on a physical phone; record its hash and source revision.
- Confirm the published backend is available and uses the expected contract.
- Register with a fresh invitation; login; view/edit profile; restart and restore session.
- Report a synthetic incident with manual location and optional GPS. Deny location and verify manual entry still works.
- Filter by owner/status, including an empty result. Refresh and return from details without losing the selected filters.
- Open the location externally; cancel and return to the app. Verify behavior if no compatible external app is installed.
- Save/open an HTTPS document reference, cancel an edit, replace a reference and reopen the incident. An authorized reporter/responsible employer succeeds; another actor must be rejected by the server.
- EMPLOYER takes, starts and closes an incident. Verify notifications and logout. Repeat WORKER and foreign-company rejection with synthetic accounts.
- Check background/recreation, loading/error recovery, keyboard, larger text, English/Spanish and light/dark screens. Record untested cases honestly.
- Compare every required backlog acceptance criterion with evidence. A successful core demo alone does not establish full backlog completion.
