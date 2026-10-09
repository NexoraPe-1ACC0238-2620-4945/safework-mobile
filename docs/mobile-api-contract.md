# SafeWork mobile API target contract

**Draft v0.4 — first-increment target and implementation checkpoint, updated 2026-10-08 (America/Lima).** The user-selected policies below replace the v0.1 selection-of-another-responsible-user proposal. The target specification remains subject to the publication and integration gates below. **No deployed current-course API is claimed to be available.** Local implementation evidence is identified separately from historical behavior and remaining proposals.

## Evidence and scope

- [Current-course backend](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-backend): main was initialized with README and .gitignore at `3bdeee9d941ab12b9b9746ff4331e13ef3ece94f`. Corrected source is published on feature/backend-foundation at `00a05cec8339e91c1422c7249ceeafa10d5d7079`, in [backend PR #1](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-backend/pull/1) to main; not merged or deployed. There is no deployed runtime base URL.
- [Historical backend](https://github.com/NexoraPe-1ASI0732/backend-safework), pinned [snapshot](https://github.com/NexoraPe-1ASI0732/backend-safework/commit/778fe1ec8e999b27e8d0340eb26fef50d1a49683): use the existing [historical audit](backend-contract-review.md), especially its [DTO inventory](backend-contract-review.md#3-implemented-historical-dtos), [routes](backend-contract-review.md#4-implemented-historical-routes) and [evidenced problems](backend-contract-review.md#7-problems-evidenced-in-historical-code). This document does not repeat that code audit.
- Originally prepared on `feature/mobile-api-contract` from updated `test` at `3f6fa5519cb60798063fdce496a658458b23b8ad`. Its existing contents are preserved while the separately authorized backend implementation proceeds. The preserved draft is carried into feature/iam. Android IAM integration uses the explicitly configured validated current-course local runtime; mobile execution evidence is recorded separately.
- All IDs, people, addresses, timestamps and token/password/invitation strings below are **synthetic fixtures**. Placeholder tokens are deliberately invalid. No example specifies a deployment host; relative routes are not a configured Android API URL.

### Validated current-course backend checkpoint

The foundation is implemented and locally validated; publication is a PR to main, not a deployment. Its backend docs/verification.md records the source/test revision and actual Maven, MySQL, HTTP, restart and bootstrap results. Android IAM is implemented and locally tested on feature/iam; its separate [implementation report](iam-implementation.md) identifies executable revisions and mobile results. Teammate packages are prepared separately.

The current implementation retains historical success DTO fields and adds invitationToken for WORKER-only signup. Proofs are company/email-bound, opaque, single-use and expire after 24 hours. Own-company EMPLOYER or backend-only ADMIN can issue them. ADMIN-only role/company/enablement changes revoke all target sessions and audit in the same transaction.

JWT uses HS512, issuer safework-backend, audience safework-mobile and required sub/userId/companyId/jti/iat/exp. jti is a persisted session UUID; lifetime defaults to seven days, configurable by JWT_EXPIRATION (1-365 days). No refresh exists. Every protected request checks an active, unexpired session and current account/company/roles/security version; writes recheck transactionally. No raw JWT is stored by the server.

POST /api/v1/authentication/sign-out now revokes only the presented session. Invalid sessions401; valid sessions without required permission403. Actual packaged-server restarts verified persistent revocation and surviving independent sessions. This is new corrected behavior, absent historically. No deployed API URL is supplied; authorized local integration may use the validated current-course commit while its PR is under review, with no historical fallback.

### Compatibility labels

| Label | Meaning |
| --- | --- |
| **Compatible with historical backend** | The cited historical route, successful DTO and relevant ownership predicate can be retained. This does not certify the historical backend's whole security/error/date profile or availability in the current repository. Common target requirements still apply. |
| **Requires backend correction** | Retain useful historical routes/fields, but server behavior, authorization, mapping or error handling must change before integration. |
| **Proposal pending decision** | A new field/schema or remaining protocol detail needs backend agreement and implementation. A complete example is illustrative, not an available API or a published server schema. |

All schemas, errors and security rules describe the first-increment **target**. The selected policies below are fixed planning directions; the [decision register](#decision-register) identifies remaining protocol/implementation details, not a choice between self-assignment and another-responsible selection. Publishing current backend source, approved OpenAPI and passing server tests is the integration gate.

## First-increment decisions versus historical DTOs

| Direction | Audited historical contract | Exact backend delta |
| --- | --- | --- |
| Invitation/affiliation | SignUpResource companyId/fullName/emailAddress/password/roles; company existence check only | Add validated company-bound proof processing and corresponding DTO/schema (invitationToken remains proposed until implemented). Reject body company/roles overrides; consume/replay policy and issuer authority must be defined. |
| WORKER / administrative EMPLOYER / no Android ADMIN | Omitted/empty roles default WORKER but supplied privileged roles are retained; UserResource already has roles; JWT takes first role | Always assign WORKER during public signup; administrative grant/revoke process outside Android with authorization/audit. Deterministic current-role checks; no ADMIN tenant bypass on mobile routes. Corrected ADMIN-only APIs/audit are implemented as described in the checkpoint; they are not Android functions or historical routes. |
| EMPLOYER self-assignment | CreateAssignmentResource only incidentId; responsible ID from principal. Service already checks persisted EMPLOYER and matching company | **No assignment request/response field extension.** Keep existing checks, add active/current membership, OPEN-only/no-existing-assignment/atomic uniqueness and mapped errors. Old assignTo permits ASSIGNED/IN_PROGRESS regression. |
| Responsible-only start/close | No-body routes; aggregate enforces ASSIGNED -> IN_PROGRESS -> CLOSED, but service uses global ID and actor ID only for event | Add company and persisted responsible==principal checks before mutation, current EMPLOYER authority and atomic state tests. Set/persist existing completionDate on close; old column is non-updatable and never set by that path. |
| No automatic renewal | id/username/token login DTO, principal-based /users/me, signed expiry. No historical refresh/revocation flow | No refresh DTO/route. Corrected server implements persisted sessions identified by JWT jti, selective logout, current security-context checks and transactional administrative revocation. Historical logging/fallback are removed. |
| Own-recipient notifications / reading later | Recipient query already scoped; DTO has id/subject/body/createdAt/isRead, with hard-coded false and no mark-read route | Preserve polling shape/scope; make dates zone-aware and enforce authorized-company contents. Retain disclosed false placeholder; no read tracking or mark-read backend work is required in this increment. |
| Zoned dates / errors / explicit limits | Historical Date/LocalDateTime zones and limits not verified; mixed/unmapped errors | Add explicit serialization/source-zone conversion, shared sanitized envelope/status mappings and published validators/OpenAPI limits; do not assert they already exist. |
| Text location / GPS and manual entry | CreateIncidentResource/IncidentResource location: String only | Keep DTO unchanged; validate/store bounded text. GPS can compose the string; no latitude/longitude field, server coordinate parser or geocoding is required. |

## Shared target conventions

### Transport and data

Use HTTPS at a deliberately supplied current-course environment URL. Never fall back to the historical backend. JSON request/response bodies use `application/json`; authenticated calls carry a bearer token. Credentials and tokens never appear in query strings or logs.

IDs are positive 64-bit JSON integers, carried as Kotlin Long without narrowing; notification IDs are UUID strings. Use exact uppercase role/status/priority values. Target timestamps are ISO-8601 with an explicit time zone (Z or numeric offset); examples use UTC Z. Specify precision/canonical serialization in OpenAPI, with nulls for absent optional fields. Historical Date/LocalDateTime wire zones are unverified: resolve the source zone and convert instants rather than appending Z to an unzoned local time.

For the first increment, incident/assignment/notification lists remain plain arrays as in the historical DTOs; empty lists return 200 with []. Incident ordering, pagination and filters remain D7, not invented query parameters. Notification order remains newest first. Unknown sensitive fields such as role/company/recipient overrides must be rejected, rather than silently changing behavior.

### Identity, roles and authorization

Selected first-increment directions:

| Operation | WORKER | EMPLOYER | ADMIN authority alone |
| --- | --- | --- | --- |
| Login/current user/own profile | Own identity | Own identity | Generic identity is not an Android administrative capability |
| Read/create incidents | Own company | Own company | No global mobile business access |
| Take an OPEN incident | Denied | Self only, own company | Denied |
| Start/close | Denied in this self-assignment increment | Only persisted responsible user, own company | Denied |
| Own assignments/notifications | Own legitimate records | Own legitimate records | No Android administrative functionality |

Android exposes WORKER/EMPLOYER flows, with no ADMIN screens, role-grant or company-administration operations. Generic backend authentication may represent an ADMIN account, but that authority never grants global access to the mobile business routes. An account also carrying EMPLOYER must satisfy its normal company/responsible checks.

Resolve current active identity, membership, company and roles on the server. These are verified corrections in the current-course foundation, not protections supplied by the historical filter. Deterministic current-role handling replaces arbitrary first-role selection; no new roles-array JWT field is assumed.

Before writes check allowed current role, company-scoped incident, persisted assignment.userId == principal userId, and state. An absent/foreign-company incident returns equivalent 404; another same-company EMPLOYER receives 403 NOT_RESPONSIBLE for start/close. A WORKER receives 403 ROLE_FORBIDDEN. No manager override or cross-company bypass is supported.

### Session profile

Keep sign-in's historical id/username/token response and GET /api/v1/users/me restoration. No automatic renewal: invalid, expired, unknown or revoked session returns401 SESSION_INVALID; clear local state and return to login without refresh.

Approved persisted sessions are independent and identified by JWT jti. Required profile: HS512, issuer safework-backend, audience safework-mobile, sub/userId/companyId/jti/iat/exp. JWT/session dates are UTC. Default lifetime seven days, configurable JWT_EXPIRATION=1-365 whole days. Current roles come from persistence, not a JWT role or proposed roles-array claim.

POST /api/v1/authentication/sign-out takes no body and revokes only the presented jti, returning204. Role/company membership/user enablement changes revoke all target sessions transactionally with a security-version change and audit. No-op administrative values retain sessions. Invalid sessions401; valid sessions without role/responsible permission403.

Android clears identity/token/cache state even when server logout fails and prevents in-flight responses from repopulating a signed-out account. Local clearing is not confirmed server revocation. No refresh or historical fallback.

Server persistence contains UUID/identity/company/security-version/roles/dates/revocation metadata only. Raw JWTs, passwords and invitation proofs are not logged. Keys come from configuration; absent/weak keys fail safely. Raw client tokens remain in IAM Infrastructure, outside Domain.

### Error contract

Uniform target body, including authentication failures:

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Resource not found.",
  "fieldErrors": {},
  "requestId": "synthetic-request-001"
}
```

`code` is stable for Android mapping; `message` is safe English diagnostic text, not the UI's localization source. `fieldErrors` maps submitted field names to arrays of safe messages; `requestId` is an opaque correlation string with no credentials or personal data. All operations share 400 malformed/invalid input, 401 for invalid session when protected, and 500 INTERNAL_ERROR with no stack trace. Tables below list operation-specific codes in addition to those common failures.

| HTTP | Stable code / semantics |
| --- | --- |
| 400 | VALIDATION_ERROR; invalid JSON, field types/limits, positive IDs, forbidden override fields |
| 401 | INVALID_CREDENTIALS on sign-in; SESSION_INVALID on protected calls; no distinction between missing/deleted/expired/revoked account credentials |
| 403 | Implemented ROLE_FORBIDDEN or NOT_RESPONSIBLE with a valid session. MEMBERSHIP_REQUIRED is not an implemented separate membership-state response. |
| 404 | RESOURCE_NOT_FOUND for absent or company-invisible resources |
| 409 | Implemented EMAIL_UNAVAILABLE or STATE_CONFLICT; ASSIGNMENT_CONFLICT is not a separate implemented code. |
| 422 | INVITATION_INVALID; generic missing/invalid/expired/replayed/wrong-recipient invitation proof |
| 429 | RATE_LIMITED; local single-process authentication limit 60 per IP/minute, Retry-After: 60. Shared deployment limits remain an operations task. |
| 500 | INTERNAL_ERROR; sanitized failure, not broad mapping of all runtime exceptions to 404 |

Examples of invalid transitions use 409, unlike historical start/close's 400 branch. These errors are target corrections, not promises about the old controller catches. Do not retry mutations blindly after a timeout; refresh the relevant resource and resolve the outcome. Atomic transitions prevent repeated or concurrent requests from creating duplicate assignments/events.

## Operation catalogue

| ID | Method and route | Classification | Shared response |
| --- | --- | --- | --- |
| IAM-01 | POST /api/v1/authentication/sign-in | Requires backend correction | AuthenticatedUserResource |
| IAM-02 | POST /api/v1/authentication/sign-up | Requires backend correction historically; invitationToken is implemented in the corrected foundation | UserResource |
| IAM-03 | GET /api/v1/users/me | Compatible with historical backend, subject to common target profile | UserResource |
| IAM-04 | PATCH /api/v1/users/me | Compatible with historical backend, subject to common target profile | UserResource |
| IAM-05 | POST /api/v1/authentication/sign-out | New approved operation, implemented in corrected backend; absent historically | 204, empty body |
| INC-01 | GET /api/v1/incidents | Requires backend correction | Array of IncidentResource |
| INC-02 | GET /api/v1/incidents/{incidentId} | Requires backend correction | IncidentResource |
| INC-03 | POST /api/v1/incidents | Requires backend correction | IncidentResource |
| ASN-02 | POST /api/v1/assignments | Compatible with historical backend for self-assignment DTO; state/atomicity/error corrections required | AssignmentResource |
| ASN-03 | GET /api/v1/assignments | Compatible with historical backend, subject to common target profile | Array of AssignmentResource |
| INC-04 | POST /api/v1/incidents/{incidentId}/start | Requires backend correction; responsible/company checks required | IncidentResource |
| INC-05 | POST /api/v1/incidents/{incidentId}/close | Requires backend correction; responsible/company checks required | IncidentResource |
| NOT-01 | GET /api/v1/notifications/my-notifications | Compatible with historical backend for recipient/DTO; company-content/date corrections apply; read mutation deferred | Array of NotificationResponse |

The removed v0.1 selector is not part of this increment. ASN-02/ASN-03 keep their IDs for team continuity. No responsible-user selector/directory is required.

### Shared DTO commitments

Retain UserResource: id, companyId, fullName, email, phoneNumber, createdAt, updatedAt, roles; AuthenticatedUserResource: id, username, token. Sign-in uses email; registration retains emailAddress. Null phoneNumber is permitted; no password/hash/token belongs in UserResource.

**First-increment IncidentResource retains the historical ten fields:** id, userId, companyId, title, description, location, status, documentUrl, reporterName, assigneeName. userId identifies the reporter, not the responsible user. assigneeName is display-only, nullable while unassigned, and never an authorization identity.

**assignmentId and assigneeUserId remain proposals pending server implementation.** They are absent from historical IncidentResource and excluded from required first-increment schemas/examples. Adopting them needs backend DTO/mapping/OpenAPI changes and published validation. Do not require these fields before implementation.

Use GET /assignments to obtain the authenticated user's AssignmentResource.id, incidentId and userId. Correlate its incidentId with IncidentResource.id for UX, and userId with the IAM principal. The server independently checks the persisted assignment on every command. These historical fields support self-assignment without optional incident-ID extensions or name-based identity matching.

AssignmentResource retains id/incidentId/userId/incidentTitle/status/assignedAt/priority/completionDate. userId is the authenticated taking EMPLOYER; status comes from the incident; priority remains LOW/MEDIUM/HIGH, default MEDIUM. completionDate is null before close and a real zone-aware instant afterward. Historical close never sets it and the column is non-updatable; the backend must correct persistence/mapping lifecycle.

NotificationResponse retains id/subject/body/createdAt/isRead. isRead=false is a **nonfunctional historical compatibility placeholder**, not implemented read tracking. No mark-read control or reliable unread count belongs in this increment.

### Explicit validation limits

The following limits are **target validators**, not protections verified in historical source. Backend must implement and publish identical OpenAPI constraints before integration.

| Field | First-increment target |
| --- | --- |
| Numeric IDs | Positive signed 64-bit integer; reject zero, negative, fractional and overflowing values |
| fullName | Trimmed nonblank string, 1–120 Unicode code points |
| email / emailAddress | Valid email syntax, at most 254 characters; publish normalization/uniqueness policy |
| password | 12–64 characters and at most 64 UTF-8 bytes; never trim/truncate; reject before hashing outside limits |
| phoneNumber | Optional; non-null value 7–32 characters: digits, spaces, hyphens, parentheses and optional leading +; reject blank. Null/omission keeps the historical value. Clearing is deferred. |
| title | Trimmed nonblank string, 1–120 Unicode code points |
| description | Trimmed nonblank string, 1–4000 Unicode code points |
| location | Trimmed nonblank text, 1–500 Unicode code points; manual or GPS-composed, with no backend coordinate parser implied |
| invitationToken | Implemented opaque proof up to 2048 characters; missing/invalid proof422; server validates email/company/expiry/single use. |

The conservative password/byte limit is a target choice, not an audited existing hash validator. Corrected server uses Unicode code points for stripped names/title/description/location; passwords are untrimmed and additionally bounded by UTF-8 bytes. Email is trimmed/lowercased internally; submit valid syntax. Phone needs a digit and permits + only at the beginning. Sensitive extra fields are rejected with 400. New pagination/filter/phone-clearing/coordinate fields are outside this increment.

## IAM operations

### IAM-01 — Login

**Requires backend correction.** Retain historical POST /api/v1/authentication/sign-in and SignInResource / AuthenticatedUserResource. Correct the historical uncaught invalid-password/user-not-found failures, token policy and logging; do not promise its optional 404 branch as the target.

Authentication: public, any active approved role may sign in. Request contains only email/password. Server validates credentials and current account/membership; it never accepts role/company from login fields. Inactive accounts follow the same generic 401 behavior as invalid credentials.

Example request body:

```json
{
  "email": "worker101@example.invalid",
  "password": "SYNTHETIC_PASSWORD_NOT_VALID"
}
```

200 response:

```json
{
  "id": 101,
  "username": "worker101@example.invalid",
  "token": "SYNTHETIC_ACCESS_TOKEN_NOT_VALID"
}
```

Specific errors: 401 INVALID_CREDENTIALS; 429 RATE_LIMITED. Unknown email and wrong password use the same status/body. The next call is IAM-03 to obtain authoritative identity/company/roles. Company onboarding uses validated invitation; supported mobile work flows require current approved membership. No ADMIN-specific mobile capability is granted by a login token.

### IAM-02 — Registration

**Requires backend correction.** Server-validated company invitation and WORKER-only registration are selected first-increment directions. Reuse POST /api/v1/authentication/sign-up and UserResource. **invitationToken is implemented in the corrected foundation, absent historically. No deployed API is claimed.**

Historical request includes companyId/fullName/emailAddress/password/roles. Target keeps fullName/emailAddress/password and obtains company from server-validated invitation rather than client companyId. Corrected issuer authority is own-company EMPLOYER or backend-only ADMIN. Proof is opaque, company/email-bound, single-use, expires after 24 hours, and stored as a SHA-256 digest only. Consumption and WORKER creation are transactional.

Example target body (synthetic password/invitation, deliberately invalid):

```json
{
  "fullName": "Synthetic Reporter",
  "emailAddress": "worker101@example.invalid",
  "password": "SYNTHETIC_PASSWORD_NOT_VALID",
  "invitationToken": "SYNTHETIC_INVITATION_NOT_VALID"
}
```

201 response for a generated test fixture with valid server-issued test proof:

```json
{
  "id": 101,
  "companyId": 701,
  "fullName": "Synthetic Reporter",
  "email": "worker101@example.invalid",
  "phoneNumber": null,
  "createdAt": "2026-10-08T12:00:00Z",
  "updatedAt": "2026-10-08T12:00:00Z",
  "roles": ["WORKER"]
}
```

Authentication: public; no prior session/role required. Validate invitation/active company/recipient, uniqueness and WORKER grant atomically. Reject companyId, roles and equivalent privilege overrides with 400 VALIDATION_ERROR, even with proof. A known company ID or registration code is not enough. Invitation replay must not create multiple memberships.

Specific errors: 422 INVITATION_INVALID, 409 EMAIL_UNAVAILABLE, 429 RATE_LIMITED, plus common validation/internal failures. No sign-up token is returned; login through IAM-01. Administrative EMPLOYER provisioning is outside Android; the corrected ADMIN-only grant/revoke process audits changes and revokes all target sessions. Its HTTP route cannot grant ADMIN and rejects self/ADMIN targets. No such administrative route or invitation protection is claimed to exist historically.

### IAM-03 — Current user and session restoration

**Compatible with historical backend** for GET /api/v1/users/me, UserResource and principal-derived self lookup. Current account/membership checks, deterministic roles, UTC dates and uniform errors remain target corrections.

Example request, no body:

```http
GET /api/v1/users/me
Authorization: Bearer SYNTHETIC_ACCESS_TOKEN_NOT_VALID
```

200 response:

```json
{
  "id": 101,
  "companyId": 701,
  "fullName": "Synthetic Reporter",
  "email": "worker101@example.invalid",
  "phoneNumber": null,
  "createdAt": "2026-10-08T12:00:00Z",
  "updatedAt": "2026-10-08T12:00:00Z",
  "roles": [
    "WORKER"
  ]
}
```

Authentication: bearer token; all approved roles access only themselves. Neither userId nor companyId is accepted from the caller. Return current server identity/company/roles, not stale unverified token copies. Unusable account/session or inactive company returns 401 SESSION_INVALID. No separate pending membership-state API or MEMBERSHIP_REQUIRED response is implemented. No other user's profile may be returned.

### IAM-04 — Own profile update

**Compatible with historical backend** for PATCH /api/v1/users/me and UpdateProfileResource, limited to the historical non-null name/phone update. Common target validation/session/errors apply.

Example body, bearer required:

```json
{
  "fullName": "Synthetic Updated Reporter",
  "phoneNumber": null
}
```

200 response:

```json
{
  "id": 101,
  "companyId": 701,
  "fullName": "Synthetic Updated Reporter",
  "email": "worker101@example.invalid",
  "phoneNumber": null,
  "createdAt": "2026-10-08T12:00:00Z",
  "updatedAt": "2026-10-08T12:05:00Z",
  "roles": [
    "WORKER"
  ]
}
```

Any approved role edits only the principal's account. Only fullName/phoneNumber are allowed; omitted or null fields keep their values. Clearing phoneNumber requires a later agreed explicit convention (D7). Reject userId, companyId, email/password and roles overrides with 400 VALIDATION_ERROR. Re-check active account/membership; 401/403 follow IAM-03. No generic update-user-by-ID endpoint is assumed.

### IAM-05 — Server logout

New approved operation, implemented in the corrected backend; absent historically.

```http
POST /api/v1/authentication/sign-out
Authorization: Bearer SYNTHETIC_ACCESS_TOKEN_NOT_VALID
```

No body or user/company selectors. Valid session204, no body; only its jti is revoked. Missing, expired, unknown, altered or revoked session401 SESSION_INVALID. Unexpected body400. No refresh. Android clears local state regardless of network/server outcome; a network failure is not confirmed server revocation.

## Incident Management operations

### INC-01 — Incident list

**Requires backend correction.** Retain GET /api/v1/incidents and the array response. Historical non-ADMIN company filtering is useful; remove any automatic global ADMIN access from the mobile policy without requiring proposed assignment IDs.

Example request, no body:

```http
GET /api/v1/incidents
Authorization: Bearer SYNTHETIC_ACCESS_TOKEN_NOT_VALID
```

200 response:

```json
[
  {
    "id": 501,
    "userId": 101,
    "companyId": 701,
    "title": "Synthetic walkway obstruction",
    "description": "Fictional material blocks a marked passage.",
    "location": "Synthetic area A",
    "status": "OPEN",
    "documentUrl": null,
    "reporterName": "Synthetic Reporter",
    "assigneeName": null
  }
]
```

Allowed first-increment roles: WORKER/EMPLOYER with active membership in the company. Derive scope on the server; no caller-supplied companyId/userId selects another tenant. List only that company's incidents. An empty visible set is [], not 404. Company-wide WORKER/EMPLOYER visibility is the initial documented scope; a later narrower read policy must change both list and detail explicitly.

Specific errors: 403 ROLE_FORBIDDEN or MEMBERSHIP_REQUIRED. No filter, pagination parameter or sort order is silently assumed.

### INC-02 — Incident detail

**Requires backend correction.** Retain GET /api/v1/incidents/{incidentId}; replace historical global ID lookup with the exact list visibility/company predicate and return IncidentResource.

Example request, no body:

```http
GET /api/v1/incidents/501
Authorization: Bearer SYNTHETIC_ACCESS_TOKEN_NOT_VALID
```

200 response:

```json
{
  "id": 501,
  "userId": 101,
  "companyId": 701,
  "title": "Synthetic walkway obstruction",
  "description": "Fictional material blocks a marked passage.",
  "location": "Synthetic area A",
  "status": "ASSIGNED",
  "documentUrl": null,
  "reporterName": "Synthetic Reporter",
  "assigneeName": "Synthetic Employer"
}
```

Allowed roles/company are identical to INC-01. Same-company visibility does not grant mutation permission. This DTO carries reporter ID and display names; get the authenticated user's responsible identity from own AssignmentResource records, not names or unimplemented incident-ID extensions. Specific errors: 403 ROLE_FORBIDDEN; 404 RESOURCE_NOT_FOUND for missing/foreign-company/invisible ID, with indistinguishable safe bodies.

### INC-03 — Report an incident

**Requires backend correction.** Retain POST /api/v1/incidents and CreateIncidentResource (title, description, location); verify current membership and return the historical ten-field IncidentResource.

Example body, bearer required:

```json
{
  "title": "Synthetic walkway obstruction",
  "description": "Fictional material blocks a marked passage.",
  "location": "Synthetic area A"
}
```

201 response:

```json
{
  "id": 501,
  "userId": 101,
  "companyId": 701,
  "title": "Synthetic walkway obstruction",
  "description": "Fictional material blocks a marked passage.",
  "location": "Synthetic area A",
  "status": "OPEN",
  "documentUrl": null,
  "reporterName": "Synthetic Reporter",
  "assigneeName": null
}
```

Allowed first-increment roles: WORKER/EMPLOYER with active membership. Derive reporter/company from the authenticated current account. Body cannot set userId, companyId, status, assignment/responsible IDs or priority. New incidents start OPEN with no assignment and nullable documentUrl. Authorization plus persistence is atomic with respect to membership changes.

Specific errors: 403 ROLE_FORBIDDEN/MEMBERSHIP_REQUIRED; 400 VALIDATION_ERROR for blank/invalid fields or forbidden overrides. Location remains text. Manual input and GPS-composed text use the same historical location field; GPS permission denial/unavailability must leave manual entry usable. No latitude/longitude DTO fields, server parser, geocoding or actual GPS implementation are added.

Alternative synthetic request body using coordinates composed as text, not structured geographic fields:

```json
{
  "title": "Synthetic walkway obstruction",
  "description": "Fictional material blocks a marked passage.",
  "location": "GPS coordinates (synthetic fixture): 0.000000, 0.000000"
}
```

The backend stores/returns the text within the declared length limit. Android may allow manual editing of GPS-composed text; that does not establish a server geographic-data contract.

### ASN-02 — Take an incident by self-assignment

**Compatible with historical backend** for POST /api/v1/assignments, CreateAssignmentResource {incidentId}, AssignmentResource and principal-derived responsible user. **Requires backend correction** for OPEN-only behavior, atomic uniqueness/current membership and consistent errors.

Example body from authenticated EMPLOYER 103 in company 701, bearer required:

```json
{
  "incidentId": 501
}
```

201 response:

```json
{
  "id": 801,
  "incidentId": 501,
  "userId": 103,
  "incidentTitle": "Synthetic walkway obstruction",
  "status": "ASSIGNED",
  "assignedAt": "2026-10-08T12:10:00Z",
  "priority": "MEDIUM",
  "completionDate": null
}
```

Server derives responsible userId from the authenticated principal: 103 in this fixture. Historical service already checks persisted EMPLOYER and incident/user company equality; preserve these real checks. Revalidate active membership/current EMPLOYER at write time. Actor and responsible are the same user, distinct from reporter 101.

Take only OPEN with no existing assignment. Persist one assignment and ASSIGNED atomically. Repeated/competing take or ASSIGNED/IN_PROGRESS/CLOSED returns 409; never overwrite the responsible user or reset progress. No selection of another user, candidate directory, reassignment or manager override is included.

Reject body userId, assigneeUserId, companyId or role overrides with 400 VALIDATION_ERROR; this rejection is a target requirement, not an audited historical validator. The authenticated caller is the only possible responsible user.

Specific errors: 403 ROLE_FORBIDDEN or MEMBERSHIP_REQUIRED; 404 RESOURCE_NOT_FOUND for missing/foreign-company incident; 409 STATE_CONFLICT or ASSIGNMENT_CONFLICT. No assignee-selection 422 error applies.

### ASN-03 — Current user's assignments

**Compatible with historical backend** for GET /api/v1/assignments, array of AssignmentResource and principal-based responsible-user predicate. Common target checks, completionDate lifecycle and serialization still require backend work.

Example request, no body:

```http
GET /api/v1/assignments
Authorization: Bearer SYNTHETIC_EMPLOYER_TOKEN_NOT_VALID
```

200 response for user 103:

```json
[
  {
    "id": 801,
    "incidentId": 501,
    "userId": 103,
    "incidentTitle": "Synthetic walkway obstruction",
    "status": "ASSIGNED",
    "assignedAt": "2026-10-08T12:10:00Z",
    "priority": "MEDIUM",
    "completionDate": null
  }
]
```

Supported WORKER/EMPLOYER identities may query legitimate assignments belonging to their current identity; also require the assignment incident to remain in its current authorized company. No request userId/recipient parameter is accepted. Another user's records never appear; [] means no own assignments. This route is not an employer's global assignment directory. Specific errors: 403 MEMBERSHIP_REQUIRED; common 401/400/500 profile.

### INC-04 — Start work

**Requires backend correction; responsible/company checks required.** Retain POST /api/v1/incidents/{incidentId}/start, no body, IncidentResource response. Keep ASSIGNED -> IN_PROGRESS and add company/responsible authorization plus atomic state changes.

Example request as responsible EMPLOYER 103:

```http
POST /api/v1/incidents/501/start
Authorization: Bearer SYNTHETIC_EMPLOYER_TOKEN_NOT_VALID
```

200 response:

```json
{
  "id": 501,
  "userId": 101,
  "companyId": 701,
  "title": "Synthetic walkway obstruction",
  "description": "Fictional material blocks a marked passage.",
  "location": "Synthetic area A",
  "status": "IN_PROGRESS",
  "documentUrl": null,
  "reporterName": "Synthetic Reporter",
  "assigneeName": "Synthetic Employer"
}
```

Required actor: the active current EMPLOYER who took the incident and remains its persisted responsible user in the same company. EMPLOYER status alone does not permit another person's start. Check incident visibility, matching assignment and current eligible responsible account before state. Do not trust actor IDs in body/query. Missing/corrupt assignment cannot be treated as permission to start.

Specific errors: 403 ROLE_FORBIDDEN/NOT_RESPONSIBLE; 404 RESOURCE_NOT_FOUND for absent/foreign-company incident; 409 STATE_CONFLICT for OPEN, IN_PROGRESS, CLOSED or missing/inconsistent assignment. A second/concurrent start cannot emit a second successful transition event.

### INC-05 — Close work

**Requires backend correction; responsible/company checks required.** Retain POST /api/v1/incidents/{incidentId}/close, no body, IncidentResource response. Keep IN_PROGRESS -> CLOSED and add the same company/responsible checks as INC-04.

Example request as responsible EMPLOYER 103:

```http
POST /api/v1/incidents/501/close
Authorization: Bearer SYNTHETIC_EMPLOYER_TOKEN_NOT_VALID
```

200 response:

```json
{
  "id": 501,
  "userId": 101,
  "companyId": 701,
  "title": "Synthetic walkway obstruction",
  "description": "Fictional material blocks a marked passage.",
  "location": "Synthetic area A",
  "status": "CLOSED",
  "documentUrl": null,
  "reporterName": "Synthetic Reporter",
  "assigneeName": "Synthetic Employer"
}
```

Required actor rules are identical to start; no implicit manager/admin override. Persist CLOSED and assignment completionDate in the same successful transition transaction. Afterward ASN-03's assignment status is CLOSED and completionDate is a UTC string, e.g. 2026-10-08T12:30:00Z. Closure recipients follow D6.

Specific errors: 403 ROLE_FORBIDDEN/NOT_RESPONSIBLE; 404 RESOURCE_NOT_FOUND; 409 STATE_CONFLICT for OPEN, ASSIGNED, CLOSED or inconsistent assignment. Do not close twice or duplicate completion/events.

## Notification Management operation

### NOT-01 — Authenticated recipient's notifications

**Compatible with historical backend** for GET /api/v1/notifications/my-notifications, NotificationResponse and authenticated-recipient newest-first query. Current-company-safe contents/time-zone serialization require backend work; only the historical recipient predicate is verified.

Example request as EMPLOYER 103, no body:

```http
GET /api/v1/notifications/my-notifications
Authorization: Bearer SYNTHETIC_EMPLOYER_TOKEN_NOT_VALID
```

200 response for that authenticated recipient:

```json
[
  {
    "id": "00000000-0000-4000-8000-000000000001",
    "subject": "Synthetic incident status update",
    "body": "Synthetic incident 501 is in progress.",
    "createdAt": "2026-10-08T12:20:00Z",
    "isRead": false
  }
]
```

Bearer required; supported WORKER/EMPLOYER identities query only their own legitimate notifications. Recipient comes from current authenticated identity; userId/recipientId/companyId query/body must not override it. Contents and incident links must remain visible in the authorized company after membership changes; this is a target check, not established by the historical recipient-only lookup.

**Read-only increment:** no mark-read/delete/push operation. The historical mapper hard-codes isRead=false; retain it as a documented unimplemented compatibility flag, not truthful read tracking. Do not claim reliable unread counts or successful read updates. A later read-state feature needs separately specified persistence and a protected mutation.

Historical creation events address reporter; assignment events address taking EMPLOYER; start/close events address actor. With responsible-user authorization, valid start/close actor is the taking EMPLOYER. Preserve those recipient semantics here; additionally notifying the reporter on status change is a later business change.

Specific errors: 403 ROLE_FORBIDDEN/MEMBERSHIP_REQUIRED and common 401/400/500. No notifications returns []. Example forbidden override ?recipientId=201 returns 400 VALIDATION_ERROR and never broadens the predicate.

## State and concurrency agreement

| Command | Required state | Required actor | Result |
| --- | --- | --- | --- |
| Report | New | Active WORKER/EMPLOYER, own company | OPEN, no assignment |
| Take | OPEN, no existing assignment | Current active EMPLOYER, own company, self only | ASSIGNED, one assignment responsible=principal |
| Start | ASSIGNED, valid own assignment | That current EMPLOYER responsible, same company | IN_PROGRESS |
| Close | IN_PROGRESS, valid own assignment | That current EMPLOYER responsible, same company | CLOSED and completionDate |

Target chain: OPEN -> ASSIGNED -> IN_PROGRESS -> CLOSED. Historical start/close state prerequisites exist; actor/company checks do not. Historical assignment only rejects CLOSED and can replace assignment/reset progress; add OPEN-only guards rather than presenting them as existing protections.

Authorization precedes mutation/state disclosure. Conditional updates/locking plus uniqueness must produce one winner, no partial changes or duplicate successful-transition events, and 409 for competing/invalid writes. Changing membership/EMPLOYER during a write must not retain stale authority. Locking/version/idempotency mechanics are backend choices; no DTO version field is assumed.

Android may correlate principal and GET /assignments incidentId/userId for UX without optional incident assignment IDs. The server always checks persisted responsible identity. No selection of another user, manager override, reassignment, reopening or state reset is included.

## Decision register

These directions are implemented where noted. Follow-up work is separate from verified server behavior and deployment availability.

| ID | Selected direction | Current implementation / follow-up |
| --- | --- | --- |
| D1 | Server-validated company-bound invitation; no arbitrary affiliation | Implemented 24-hour company/email-bound single-use proofs. Own-company EMPLOYER or ADMIN issuer; private delivery is manual. |
| D2 | Public registration WORKER; administrative EMPLOYER grant; no Android ADMIN functionality | Implemented ADMIN-only audited WORKER/EMPLOYER changes and one-shot non-web first-operator bootstrap. No public ADMIN grant or Android ADMIN. |
| D3 | EMPLOYER takes incident using only incidentId and is responsible | Implemented OPEN-only self-assignment with company/role checks, row lock and one raced winner. No responsible selector. |
| D4 | Responsible-only start/close within company; exact state chain | Implemented responsible/current-company checks, state chain, atomic events and persisted completionDate. No manager override. |
| D5 | No automatic renewal; invalid/expired token -> login | Approved persisted jti sessions; selective logout; all-session administrative revocation; seven-day configurable lifetime; no refresh. Retention/key rotation follow up. |
| D6 | Authenticated recipient polling; read mutation deferred | Implemented recipient/current-company scope and UTC instants; false read flag disclosed. Reading/push deferred. |
| D7 | Zone-aware ISO-8601, uniform errors and explicit limits above | Implemented limits, code-point/UTF-8 byte checks, zoned dates and errors. Pagination/filter/phone clearing deferred. |
| D8 | location string from manual or GPS-composed text | Implemented bounded text. Android GPS permission/manual UX is later; no coordinate DTO. |

Optional assignmentId/assigneeUserId remain **proposals pending server implementation/specification**, excluded from required responses. Current-course source and generated OpenAPI have local passing evidence and a published foundation PR; no deployed environment is available. Authorized local integration uses the pinned validated revision while review is pending.

Mandatory outcomes: no public privilege grant or arbitrary company affiliation; company authorization on list/detail/commands; responsible-only start/close; no recipient leaks; no token logs or usable signing-key code fallback.

## Acceptance scenarios

Use generated fixtures only: reporter 101 (WORKER), taker/responsible 103 (EMPLOYER) and other employer 104 belong to company 701; worker 201 and employer 203 belong to 702. Incident 501 is reported by 101; assignment 801 is created when 103 takes it. Provisioned ADMIN fixture tests absence of mobile scope bypass. No production data/tokens are used.

| ID | Scenario | Expected evidence |
| --- | --- | --- |
| T01 | Sign-up sends roles ADMIN/EMPLOYER or client companyId, with/without proof | 400; no privilege/affiliation. Valid invitation grants WORKER only; administrative grant/revoke has separate authorized/audited tests |
| T02 | Missing/expired/replayed/wrong-recipient invitation, tampered company binding or only known company ID/code | 422 invalid proof / 400 override; no affiliation. Valid server-bound proof creates membership once |
| T03 | Unknown email/wrong password; absent/malformed/expired/invalid-signature/issuer/audience token | Generic login or protected-call 401; no disclosure; Android returns to login with no refresh request |
| T04 | Deactivate/delete user, remove/change company or revoke EMPLOYER after token issuance | Both prior sessions401; unrelated users remain valid. Changes revoke all target sessions transactionally. |
| T05 | Company 701 list and detail of company 702/nonexistent incident; ADMIN attempts mobile global read | No foreign list record; absent/foreign detail equivalent 404; no ADMIN tenant bypass |
| T06 | Report valid manual/GPS-composed location text; then over-limit or reporter/company/status/responsible overrides | Same location string persists; bad input 400; current principal/company and OPEN/no-assignment derived server-side |
| T07 | WORKER takes; EMPLOYER takes foreign incident; caller sends selected userId/assigneeUserId | Respectively 403, 404, 400; no assignment/event. No selector endpoint or other-responsible input |
| T08 | EMPLOYER 103 takes OPEN 501 with {incidentId:501} only | 201; assignment 801.userId=103; incident ASSIGNED; actor=responsible=principal, reporter remains 101. Response uses historical fields, not optional IDs |
| T09 | Employer 104 repeats/races take; take of ASSIGNED/IN_PROGRESS/CLOSED | One winner maximum, others 409; no replacement of responsible, state regression or duplicate event |
| T10 | Other same-company EMPLOYER 104/reporter 101 starts/closes; foreign employer 203 does so | 104 ->403 NOT_RESPONSIBLE; 101 ->403 ROLE_FORBIDDEN; 203 ->404. No state/completion/event change |
| T11 | Responsible 103 attempts invalid state then valid ASSIGNED start/IN_PROGRESS close | Invalid 409; valid 200; exact chain and one persisted zone-aware completionDate |
| T12 | Concurrent/repeated start/close or membership/role revoked before write commits | One accepted transition, others 409/auth rejection; no stale authorization or duplicate event |
| T13 | Query own assignments as 103, 104, 101; attempt other userId selector | 103 sees assignment.userId=103; others own records or []; override400; current-company predicate retained |
| T14 | Own notifications, other recipient override, former-company incident content | Only authenticated recipient/authorized-company contents; newest first; override400. isRead=false documented placeholder, no mark-read/read-state claim |
| T15 | Capture logs for success/failure; start without valid signing key | No token/password/invitation proof in logs; key absence/invalidity fails safely without code fallback |
| T16 | Compare OpenAPI, limit boundaries, multibyte/password byte limits and response fixtures | Historical ten-field IncidentResource; optional IDs not required; invitationToken only if published; zone-aware dates/nulls/envelope/status/limits match |
| T17 | Restore/expire/revoke/sign out/switch accounts during in-flight request | 401->login, no automatic refresh or cross-user cached results; local clearing is not called server revocation; test actual backend mechanism |

Backend verification identifies executed MySQL/HTTP scenarios and actual restart evidence. This entire table is not blanket-marked passed: Android in-flight account switching, deployment/key-rotation and deferred behavior remain unexecuted. Server authorization is required.

## Shared contracts and team boundaries

The following allocation is user-confirmed. Central preparation is coordinated by Carlos with Codex; each contributor incorporates, reviews, validates and commits their own actual changes with their own identity. This plan is not evidence that future work has been performed, and it must not fabricate commit authors or contributions.

| Contributor | Agreed change responsibility | Contracts needed / shared dependencies |
| --- | --- | --- |
| Carlos | IAM: login, registration, profile and session; navigation and shared configuration coordination | IAM-01..05, UserResource, AuthenticatedUserResource, session invalidation/identity-company-role contract, error conventions and approved environment configuration |
| Daniel | Incident Management: list, detail, reporting, including later GPS-composed text/manual location entry | INC-01..03, shared historical IncidentResource/IDs/statuses, IncidentRepository semantics, IAM session, target validation and text-location convention |
| Francisco | Incident Management: assignment/start/close; Notification Management | ASN-02/ASN-03, INC-04..05, NOT-01, the same incident model/repository/IDs/statuses as Daniel, principal-derived responsible identity from AssignmentResource and IAM session |

**Incident Management is one bounded context shared by Daniel and Francisco. Assignment belongs to it.** Do not split it into screen/branch contexts or duplicate its domain model.

### Contracts to agree once and reuse

| Shared contract | Consumers | Required boundary |
| --- | --- | --- |
| CurrentSession identity snapshot | All three; IAM supplies it | Current UserId, CompanyId, approved roles and authenticated/unauthenticated/expired state. Consumers do not own duplicate logins or parse a token to authorize server operations. Raw tokens stay in IAM Infrastructure. |
| UserId / CompanyId / IncidentId / AssignmentId / NotificationId | All three | Same positive Long/UUID wire mapping and domain semantics; reporter and responsible IDs remain distinct. No String/Int/Long divergence between packages. |
| Incident aggregate and IncidentStatus | Daniel and Francisco | One model with reporter/company, assignment/responsible identity, OPEN/ASSIGNED/IN_PROGRESS/CLOSED and agreed invariants. DTO mapping is outside Domain. |
| IncidentRepository | Daniel and Francisco | One agreed domain contract covering visible incident list/detail, reporting, self-assignment, own assignments and start/close; method names and interface split are agreed together before either contributor implements them. Authoritative target state comes from responses, not copied UI state. |
| Assignment projection / policy | Daniel and Francisco | Assignment.userId is the authenticated taking EMPLOYER; correlate incidentId with IncidentResource.id. Incident assignmentId/assigneeUserId remain optional proposals, not required/implemented fields. No selected-responsible input. |
| ApiFailure to domain/application failure mapping | All three | Common stable code categories for validation/session/forbidden/not-found/conflict; app Infrastructure translates wire errors before Presentation; no Retrofit/HTTP types in business. |
| NotificationRepository and recipient projection | Francisco with Carlos's session | Fetch current user's notifications, own UUID/date mapping and documented read-flag placeholder; no arbitrary user selector and no duplicate session storage. Event links resolve through authorized incident detail. |
| Location reporting boundary | Daniel with shared incident model | Manual or GPS-composed text in the same location string; Android capability/permission conversion stays in Infrastructure. Android Location/permission types stay out of Domain/Application. |
| Navigation / environment / authenticated transport | Carlos coordinates; all consume | Agreed entry arguments/IDs and environment configuration. Presentation may use identity for UX; backend still authorizes. One later transport/session adapter, no historical fallback. |
| Fixture/schema revision | All three | Review the same approved DTOs, state/error fixtures and OpenAPI revision before parallel package changes; any schema change is coordinated across both incident contributors and IAM/notifications as applicable. |

Apply the existing [architecture decision](architecture.md): context packages directly below com.nexorape.safework; business holds pure Kotlin Domain/Application, app holds Presentation/Infrastructure. IAM is implemented as recorded separately; incident/notification interfaces and packages follow the agreed integration boundaries rather than fabricated placeholder classes.

## Document verification and remaining checks

The current-course generated OpenAPI, backend API contract and verification report distinguish retained DTOs, approved implemented session behavior and actual results. The historical audit and original draft are preserved. Synthetic examples remain deliberately invalid placeholders.

The preserved contract moved from feature/mobile-api-contract into feature/iam after updated test. Mobile business/HTTP/ViewModel tests, real local IAM checks, build and lint are recorded in [IAM verification](iam-implementation.md). Physical IAM flows/Keystore checks remain pending because no device is connected. Backend and mobile have separate PRs; neither is automatically merged or deployed.

## Integration dependencies and backend-owner follow-up

1. Publish/review the foundation PR and share its source/OpenAPI revision. Main initialization is not the API; authorized local integration may use the validated current-course commit while review is pending.
2. Configure an explicit test API URL. Published environments require HTTPS, private signing keys and reviewed schema migrations. No automatic deployment or historical fallback.
3. Provision the initial operator privately; issue validated invitations and grant EMPLOYER through audited ADMIN routes. No public role/company selection or Android ADMIN functionality.
4. Keep approved jti sessions and sign-out; retain regression coverage for independent sessions, selective logout, administrative revocation, restart persistence, altered/expired/unknown tokens and unrelated users.
5. Resolve outstanding responsibilities after role removal/transfer/disablement operationally. Transfers do not move business records or enable reassignment/manager override.
6. Plan expired-session retention/cleanup, key rotation and coordinated deployment rate limits. Local revocation/logout is already implemented.
7. Carlos's IAM implementation/verifications use the pinned local backend, with physical checks pending. Daniel/Francisco integrate the separate isolated packages against the [shared contracts](team-integration-contracts.md).
8. Mark-read/push, pagination/filter, phone clearing and optional assignmentId/assigneeUserId remain deferred, unimplemented proposals rather than integration blockers for this increment.
