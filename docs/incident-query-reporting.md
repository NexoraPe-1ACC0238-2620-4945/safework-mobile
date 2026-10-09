# Incident query/reporting and optional location

Daniel integration package, prepared centrally with Carlos/Codex on an isolated worktree. Base: IAM `060817fa723c52534eba0aae10973ad6dff8c527`. This document is package content, not evidence of a Daniel-authored commit. Daniel must incorporate, review, validate and commit with his own identity. No incident code is published on Carlos's IAM branch.

The single `incidentmanagement` context supplies one Incident model, positive IDs, validated title/description/location and OPEN/ASSIGNED/IN_PROGRESS/CLOSED. Pure Kotlin Domain/Application are in `business`; Android Presentation/Infrastructure are in `app`. Assignment/handling will extend this context in Francisco's prerequisite-dependent package. See [shared contracts](team-integration-contracts.md).

Features: current-company incident list, authorized detail, reporting, state/author/responsible display, retry/safe errors, manual location and optional one-shot coordinates. Report sends exactly title/description/location; caller/company are server-derived. All ten historical IncidentResource fields are mapped; no assignmentId/assigneeUserId is invented. Display names are not responsibility IDs.

Location is an optional Domain port invoked by an Application use case. Android implements it with LocationManager and runtime coarse/fine permission; no Android Location type crosses into business. Capture waits up to20s, accepts the granted precision, appends coordinates to the editable text and cancels on leaving/stopping the screen or manual location edits. Disabled/unavailable/denied location permits manual reporting. GPS/network hardware are optional; no background permission/service/tracking, last-known location reuse or structured coordinate API fields. Sources: [location permissions](https://developer.android.com/develop/sensors-and-location/location/permissions), [LocationManager](https://developer.android.com/reference/android/location/LocationManager).

Session invalidation clears list/detail/draft/identity and discards late responses. Navigation revalidates IAM on foregrounding. Local company/role/state checks guide UX and reject inconsistent responses; the server must independently authorize every route. No arbitrary company/user selector, public EMPLOYER grant, alternate responsible selection or historical fallback.

## Executed verification

Against current-course backend `00a05cec8339e91c1422c7249ceeafa10d5d7079`, isolated synthetic MySQL/runtime, the full Gradle business/app tests, debug/release packaging and lint completed successfully. **21 tests, failures0/errors0/skips0**:14 inherited IAM plus1 Incident rule,3 HTTP contract,2 ViewModel and1 real Incident test. **40 recorded live HTTP requests**:20 inherited IAM and20 new incident/setup requests. New evidence: report201; list/detail200; a second synthetic company list excludes the incident and foreign detail404; all three test sessions logout204. Coordinate text in these HTTP fixtures is synthetic/manual, not a physical GPS reading.

The final navigation/optional-hardware-only adjustment was separately rebuilt/linted without repeating unchanged live tests. Lint0errors/9warnings (same version/API/KTX notices as IAM). A legacy LocationListener status callback generates a deprecation-override Kotlin warning; it supports API26 and does not bypass permission. Native-symbol packaging notice remains. No suppressive lint baseline was added.

Pending: no USB device is connected. Actual GPS/approximate permission/denial/settings/timeouts/lifecycle cleanup, UI interaction, API26/33+, locale/theme/IME/TalkBack must be tested on a phone/emulator. JVM location tests use a fake provider, not real GPS. Release is unsigned, no production URL/TLS/deployment/migration or pagination supplied. Server proof is distinct from local response consistency checks.

Commands/fixtures match [IAM testing](iam-implementation.md), including opt-in loopback environment variables and private synthetic operator setup. Without opt-in fixture configuration both live tests are skipped and may not be reported passed. Do not log/password/commit tokens or fixture secrets. Recipient should rerun tests before publishing `feature/...` -> PR to `test`; no automatic merge.

Recipient integration base, checked after git fetch: 
