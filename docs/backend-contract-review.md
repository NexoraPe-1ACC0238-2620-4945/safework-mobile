# SafeWork backend contract review

Reviewed on 2026-10-07 (America/Lima). Scope: read-only inspection of repository metadata and historical source. No backend was cloned, copied into this project, published, executed or modified. No Android HTTP client or feature was implemented.

## 1. Current-course backend: no available contracts

The [current-course backend](https://github.com/NexoraPe-1ACC0238-2620-4945/safework-backend) is accessible and empty at review time.

| Check | Observed result |
| --- | --- |
| Repository metadata | Public; repository ID 1375343321; size 0; declared default branch main |
| [Branches API](https://api.github.com/repos/NexoraPe-1ACC0238-2620-4945/safework-backend/branches) | Returned an empty array; no source commit or existing branch tip to pin |
| Contents inspection | GitHub reported that the repository is empty |
| Controllers, DTOs, services, security and OpenAPI | No current implementation or specification available |
| Deployment/base URL | Not supplied or verified |

The metadata default branch does not prove that a populated main exists. Authentication, registration, current user, incidents, assignments, start/close and notifications all remain **unverified for the current course**. The historical inventory below is not an API specification for that repository and does not authorize integration.

## 2. Historical reference and evidence limits

The user supplied [the previous SafeWork backend](https://github.com/NexoraPe-1ASI0732/backend-safework) as a historical reference. Its default branch is main. This review pins commit **778fe1ec8e999b27e8d0340eb26fef50d1a49683**. All source links below point to that snapshot, rather than a moving branch.

Controllers, resource records, assemblers, application services, entities, repositories, JWT/security configuration and OpenAPI configuration were inspected remotely. Route annotations and executable service paths take precedence over comments and Swagger response annotations. The findings establish what the source implements or omits; they are not results of requests to a deployed server. No backend build, database test or exploit was performed. Persistence behavior, actual environment variables, runtime serialization and deployment availability remain unverified. Secret values are intentionally not reproduced.

## 3. Implemented historical DTOs

Sign-in uses the field email, sign-up uses emailAddress, and user responses use email. The following types describe the historical Java records; they do not freeze the current course's future JSON schema. Long denotes a numeric identifier, and lists denote JSON arrays.

| DTO | Fields |
| --- | --- |
| [SignInResource] | email: String; password: String |
| [SignUpResource] | companyId: Long; fullName: String; emailAddress: String; password: String; roles: List&lt;String&gt; |
| [AuthenticatedUserResource] | id: Long; username: String; token: String; assembler uses email as username |
| [UserResource] | id: Long; companyId: Long; fullName: String; email: String; phoneNumber: String; createdAt: Date; updatedAt: Date; roles: List&lt;String&gt; |
| [UpdateProfileResource] | fullName: String; phoneNumber: String; non-null fields are updated |
| [CreateCompanyResource] | name: String; constructor rejects null/blank |
| [CompanyResource] | id: Long; name: String; registrationCode: String |
| [CreateIncidentResource] | title: String; description: String; location: String |
| [IncidentResource] | id: Long; userId: Long; companyId: Long; title: String; description: String; location: String; status: String; documentUrl: String; reporterName: String; assigneeName: String |
| [CreateAssignmentResource] | incidentId: Long; no selectable userId |
| [AssignmentResource] | id: Long; incidentId: Long; userId: Long; incidentTitle: String; status: String; assignedAt: Date; priority: String; completionDate: Date |
| [UpdateAssignmentPriorityResource] | priority: String; enum names LOW, MEDIUM, HIGH, case-sensitive |
| [UpdateIncidentDocumentResource] | documentUrl: String; URL metadata update, not a file-upload endpoint |
| [IncidentAnalyticsResponse] | analytics: Map&lt;String, IncidentStatusAnalytics&gt;; each value has count: Long and percentage: Double |
| [NotificationResponse][notification-dto] | id: UUID; subject: String; body: String; createdAt: LocalDateTime; isRead: boolean |

In incident responses, userId identifies the reporter; assigneeName is null when there is no assignment. Assignment userId identifies the responsible user; status comes from the incident, and assignedAt comes from the assignment's creation audit field. Analytics keys are open, assigned, in_progress and closed, including zero-count entries, with percentages within the company total. Wire date formats/time zones and null-field serialization were not verified against running OpenAPI or responses. See [incident mapping][incident-map], [assignment mapping][assignment-map] and [analytics calculation][incident-query].

## 4. Implemented historical routes

All paths below include the historical /api/v1 prefix. Except sign-in and sign-up, these routes require a JWT under the global security rule. That establishes authentication, not permission to access any supplied resource ID. Missing/invalid authentication reaches the configured 401 entry point. See [security configuration][security] and [entry point][entrypoint].

Status codes below are explicit controller branches. **400*** or **404*** means an empty-optional branch exists, but service exceptions may bypass it. Uncaught business exceptions have no application-wide exception-to-status mapping in the inspected source; they normally reach framework error handling rather than the advertised business code. Their exact runtime response was not tested. Do not treat Swagger annotations as proof of error handling or of 403 enforcement.

### IAM: authentication and users

Sources: [AuthenticationController][authentication], [UsersController][users], [user command service][user-command] and [user query service][user-query].

| Method and route | Request | Success response | Explicit other branches | Actual historical scope |
| --- | --- | --- | --- | --- |
| POST /api/v1/authentication/sign-in | SignInResource | 200 AuthenticatedUserResource | 404* | Public; email/password checked; invalid credentials throw instead of returning an empty optional |
| POST /api/v1/authentication/sign-up | SignUpResource | 201 UserResource | 400* | Public; supplied company must exist; supplied roles are accepted if valid/existing |
| GET /api/v1/users | None | 200 list of UserResource | None in controller | All users; no role, self or company predicate |
| GET /api/v1/users/{userId} | Path ID | 200 UserResource | 404 if absent | Global lookup by ID; no self/company check |
| GET /api/v1/users/email/{email} | Path email | 200 UserResource | 404 if absent | Global lookup by email; no self/company check |
| GET /api/v1/users/me | None | 200 UserResource | 404 if absent | Uses authenticated principal's user ID |
| PATCH /api/v1/users/me | UpdateProfileResource | 200 UserResource | 404* | Updates authenticated user's non-null name/phone only; no role/company update here |

Passwords are BCrypt-hashed and checked through the [hashing service][hashing]. Duplicate sign-up email, nonexistent company/role and bad sign-in credentials throw runtime exceptions; the authentication controller does not catch them. No role-management REST controller was found, although a RoleResource record exists.

### IAM: companies

Sources: [CompaniesController][companies], [company command service][company-command] and [company query service][company-query].

| Method and route | Request | Success response | Explicit other branches | Actual historical scope |
| --- | --- | --- | --- | --- |
| POST /api/v1/companies | CreateCompanyResource | 201 CompanyResource | 400* | Any authenticated principal; no privileged-role restriction |
| GET /api/v1/companies | None | 200 list of CompanyResource | None in controller | Global company list, including registration codes |
| GET /api/v1/companies/{companyId} | Path ID | 200 CompanyResource | 404 if absent | Global lookup; no own-company check |
| GET /api/v1/companies/registration_code/{registrationCode} | Path code | 200 CompanyResource | 404 if absent | Global lookup; no membership/invitation authorization |

Company creation rejects duplicate names by throwing, without a controller mapping for that business error. Registration does not consume or validate a registration code: it trusts a supplied existing company ID.

### Incident Management: incidents

Sources: [IncidentsController][incidents], [incident command service][incident-command], [incident query service][incident-query] and [repository predicates][incident-repository].

| Method and route | Request | Success response | Explicit other branches | Actual historical scope |
| --- | --- | --- | --- | --- |
| POST /api/v1/incidents | CreateIncidentResource | 201 IncidentResource | 400* | Reporter and company come from JWT principal; referenced user/company existence checked |
| GET /api/v1/incidents | None | 200 list of IncidentResource | None in controller | JWT role ADMIN: all companies; other roles: principal's company |
| GET /api/v1/incidents/{incidentId} | Path ID | 200 IncidentResource | 404 if absent | Global ID lookup; no company/reporter/responsible check |
| GET /api/v1/incidents/analytics | None | 200 IncidentAnalyticsResponse | None in controller | Company ID from JWT; query filters that company, including for ADMIN |
| POST /api/v1/incidents/{incidentId}/start | Path ID, no body | 200 IncidentResource | 400 invalid state; 404 caught runtime failure | State check only; no role/company/responsible authorization |
| POST /api/v1/incidents/{incidentId}/close | Path ID, no body | 200 IncidentResource | 400 invalid state; 404 caught runtime failure | State check only; no role/company/responsible authorization |
| PATCH /api/v1/incidents/{incidentId}/document | UpdateIncidentDocumentResource | 200 IncidentResource | 400 caught illegal argument; 404 caught runtime failure | Global ID lookup; no role/company/reporter/responsible authorization |

Creation does not let the body choose reporter/company. However, the service only checks that the separately supplied principal user and company exist, rather than revalidating their current membership. Start/close put the acting user ID into a notification event; this is not an authorization comparison with the assignment.

### Incident Management: assignments

Sources: [AssignmentsController][assignments], [incident command service][incident-command], [assignment query service][assignment-query] and [assignment repository][assignment-repository].

| Method and route | Request | Success response | Explicit other branches | Actual historical scope |
| --- | --- | --- | --- | --- |
| POST /api/v1/assignments | CreateAssignmentResource | 201 AssignmentResource | 400*; thrown role/company/state failures are not caught here | Self-assignment: responsible ID is the authenticated user's ID; persisted roles must include EMPLOYER and incident/company must match that user |
| GET /api/v1/assignments | None | 200 list of AssignmentResource | None in controller | Query uses authenticated user ID and repository predicate a.user.id; only own assignments |
| GET /api/v1/assignments/{assignmentId} | Path ID | 200 AssignmentResource | 404 if absent | Global ID lookup; no responsible/company check |
| PATCH /api/v1/assignments/{assignmentId}/priority | UpdateAssignmentPriorityResource | 200 AssignmentResource | 400 invalid enum/illegal argument; 404 caught runtime failure | Global ID lookup; no role/company/responsible check |

The historical API does **not** implement a manager selecting another responsible user. ADMIN alone also does not satisfy the assignment service's EMPLOYER requirement. Multiple persisted roles can include EMPLOYER even when the JWT contains another single role.

### Notification Management

Source: [NotificationController][notifications] and [notification assembler][notification-map].

| Method and route | Request | Success response | Actual historical scope |
| --- | --- | --- | --- |
| GET /api/v1/notifications/my-notifications | None | 200 list of NotificationResponse | Recipient ID comes from the authenticated principal; newest first |

No client-supplied recipient ID is used. No mark-read or notification-write REST route was found. isRead is hard-coded to false in the response mapper. Creation events address the reporter, assignment events address the assignee, and start/close events address the **acting user** carried by the command. Whether the future product should also notify the reporter/responsible/company is a pending business decision. These records do not establish device push support.

## 5. Historical JWT, roles and real protections

- [Security configuration][security] disables CSRF, uses stateless sessions, permits authentication and Swagger/OpenAPI routes publicly, and requires authentication elsewhere. Method security is enabled, but no method role-authorization annotations/checks were found on the inspected controllers or domain service interfaces. The assignment application service's explicit EMPLOYER/company checks remain real protections.
- [Role names][roles] are exactly WORKER, EMPLOYER and ADMIN. [Role conversion/defaulting][role-domain] is case-sensitive; omitted/empty roles default to WORKER, while non-empty requested roles are retained. [Startup][startup] invokes [seeding][role-seed] for all enum roles.
- [Sign-in token issuance][jwt] uses the user's email as sub, with companyId, userId, a single role, iat and exp. The accepted header form is Authorization: Bearer followed by the token. Signing derives an HMAC key from the configured secret's UTF-8 bytes using JJWT; the code does not explicitly hard-code an algorithm name.
- Expiry is configured in days, with a seven-day environment fallback in [application properties][properties]. Signature verification and expiry rejection are implemented. There is no explicit issuer/audience or required custom-claim policy, refresh/revocation flow or token denylist in the inspected code. An alternative username-only generator exists, but the inspected sign-in path uses the User overload with the custom claims.
- The [bearer filter][filter] builds the principal directly from claims and a single authority with the enum name, without reloading the current user, company or roles. [UserDetailsImpl][principal] reports enabled/nonexpired/nonlocked as true. Changing/deleting an account or role is therefore not itself a demonstrated token revocation mechanism.
- The [User aggregate][user-domain] holds roles in a set; token issuance takes its first role. No role precedence is defined for multiple roles. List authorization uses this single JWT authority; assignment authorization instead checks all persisted roles. The two paths can apply inconsistent permissions.
- Actual scope restrictions are: /users/me uses the principal ID; incident creation derives reporter/company from the principal; non-ADMIN incident lists and analytics filter by company; assignment creation requires same-company persisted EMPLOYER self-assignment; assignment lists and notifications filter by authenticated recipient/responsible ID. These protections do not extend automatically to the ID-based endpoints.

## 6. Historical state and responsible-user rules

The [Incident aggregate][incident-domain] initializes OPEN and implements these transitions:

| Operation | Accepted previous state | Result | Additional historical rule |
| --- | --- | --- | --- |
| Report | New incident | OPEN | Reporter/company from principal |
| Assign | Any state except CLOSED | ASSIGNED | Service requires authenticated EMPLOYER from same company; creates a new assignment |
| Start | ASSIGNED only | IN_PROGRESS | No actor-to-assignee/company/role check |
| Close | IN_PROGRESS only | CLOSED | No actor-to-assignee/company/role check |

Assignment of an ASSIGNED or IN_PROGRESS incident is not rejected by the aggregate; it replaces its assignment reference and resets the status to ASSIGNED. The [Assignment entity][assignment-domain] also declares a unique incident foreign key. Repeated assignment may interact with persistence constraints; successful reassignment, concurrency behavior and database failure codes were not tested and must not be promised.

Assignment priority defaults to MEDIUM. Closing changes the incident status but does not populate assignment completionDate in the inspected path; that field is declared non-updatable. A response field is not evidence that its lifecycle is implemented.

## 7. Problems evidenced in historical code

These findings concern the pinned historical implementation only. Impact statements are static inferences from the referenced execution paths, conditional on deployment and valid resources; no live exploitation was attempted.

| Finding | Source evidence | Consequence / limitation |
| --- | --- | --- |
| Public registration accepts privileged roles | Public [security rule][security]; [sign-up role mapping][signup-map]; [service role lookup/save][user-command] lines 76-88; [role defaults][role-domain] lines 75-79; [startup seeding][role-seed] | No server restriction prevents a registrant selecting ADMIN or EMPLOYER when seeded. Default WORKER does not protect requests supplying roles. |
| Public registration trusts company selection | Sign-up companyId; [user command service][user-command] lines 82-86 | Only company existence is checked, with no invitation, membership or approval proof. A registrant can choose another existing company ID. |
| Cross-company read policy is inconsistent | [Incident query][incident-query] lines 26-42 and [repository][incident-repository] | Non-ADMIN list filtering is real, but detail-by-ID is global. ADMIN explicitly reads all companies; public ADMIN registration undermines the list boundary. |
| User/company data exposed broadly to authenticated callers | [User query][user-query] lines 37-60; [company query][company-query] lines 34-45; [company controller][companies] | No role/tenant predicate for lists and lookups; user responses include contact, company and roles, and company responses include registration codes. Intended directory/admin policies must be decided separately. |
| Start/close authorize state, not actor | [Incident command service][incident-command] lines 106-128; [aggregate][incident-domain] lines 102-114 | An authenticated caller can target another responsible user's or company's incident by ID when the state permits. Event actor ID supplies no permission check. |
| Assignment detail/priority and document update omit ownership/tenant checks | [Assignment query][assignment-query] lines 23-24; [incident command service][incident-command] lines 132-150 | ID-based access/mutations lack responsible/company/role validation. Own-assignment lists and guarded assignment creation do not fix these paths. |
| Raw bearer token is logged | [Bearer filter][filter] line 45 | Tokens can reach application logs, including before validation; exposure depends on logging and access. No token value is reproduced here. |
| JWT configuration contains a non-empty secret fallback | [Application properties][properties] line 23 | If JWT_SECRET is absent, signing uses the published fallback instead of requiring a deployment secret. The actual deployed key is unknown. The fallback value is not copied into this project. |
| Long-lived claim authority and multiple-role mismatch | [JWT generation/validation][jwt], [bearer filter][filter], [role set][user-domain], [assignment checks][incident-command] | No DB reload/revocation; single-role selection lacks precedence while assignment checks the persisted role set. Role/company changes need an explicit token policy. |
| Reassignment and lifecycle fields are incomplete | [Incident transitions][incident-domain]; [Assignment entity][assignment-domain]; [notification mapping][notification-map] | IN_PROGRESS can reset to ASSIGNED in memory; persistence result untested. completionDate is not set on close and isRead is always false. |
| Documented errors differ from controller/service behavior | [Authentication][authentication], [companies][companies], [incidents][incidents], [assignments][assignments], [user commands][user-command] | Some GET annotations advertise 201 but controllers return 200; optional error branches do not catch thrown failures. Broad runtime catches map unrelated failures to 404. No uniform error envelope or authorization 403 mapping is established. |

The [development database configuration][dev-properties] also has environment fallbacks, including a non-empty password fallback; values are not reproduced. [Production configuration][prod-properties] refers to required environment variables. These are configuration observations, not proof of actual deployment credentials.

## 8. Historical OpenAPI availability

[OpenAPI configuration][openapi] registers a global HTTP bearer/JWT security scheme. Security configuration permits /v3/api-docs/** and Swagger UI routes. Documentation is generated by Springdoc when the application runs; no checked-in OpenAPI JSON/YAML or repository README was found in this snapshot, and no deployed OpenAPI URL was supplied or called.

Global documentation security also covers authentication operations unless overridden, whereas actual sign-in/sign-up are public. Some response annotations and comments are stale: authentication route comments differ from /api/v1/authentication, and several GET annotations specify 201 despite returning 200. An unrelated external-documentation placeholder is not a SafeWork contract. This report uses actual mapping annotations and return/catch branches, not those comments.

## 9. Decisions pending for the current-course backend

None of these policies is implemented or approved by this historical review. Proposed directions below are discussion inputs; the current backend must define and test its own contracts before Android integration.

| Pending decision | Required current-course specification / validation |
| --- | --- |
| Registration and company membership | Decide whether public registration always assigns WORKER; define invitation/company admission and controlled provisioning of EMPLOYER/ADMIN. Reject unauthorized privileged role selection server-side. |
| Role meanings and tenant boundaries | Define whether ADMIN is global or company-local; define who can list users/companies and view registration codes. Apply the approved scope equally to list, detail, analytics and mutations. |
| Assignment policy | Choose historical EMPLOYER self-assignment or explicit authorized manager-to-responsible assignment. Define eligible assignee roles, same-company checks, uniqueness, allowed reassignment states and concurrency behavior. |
| Start/close/document/priority permissions | Define which roles and responsible users can act, any manager override, and mandatory company/resource checks. Match state transitions with authorization and persist audit/completion timestamps. |
| JWT and account lifecycle | Specify mandatory claims, role representation/precedence, issuer/audience, expiry, refresh/revocation, account/company changes and required secrets without usable defaults. Keep bearer values out of logs. |
| Responses and validation | Publish field names, nullable fields, dates/time zones, pagination, validation limits and uniform errors. Decide 400/401/403/404/409 semantics, including forbidden-versus-missing resource policy. |
| Notifications | Define recipients of each event, read-state operations and delivery model. Scope all reads/updates to authorized recipients; historical polling DTOs do not decide push requirements. |
| OpenAPI and integration gate | Publish current source and versioned OpenAPI, record its commit and authorized test URL, reconcile docs with behavior, then plan clients in later feature branches. |
| Negative server tests | Test anonymous access, privileged-role registration, foreign-company membership/IDs, another user's assignments/notifications, forbidden state transitions and stale/deleted-account tokens. |

Filtering results or hiding Android buttons cannot replace server authorization. Do not label the current backend protected, vulnerable or compatible based on the older repository. The Android foundation remains independent of these historical HTTP/JWT contracts.

## 10. Local foundation scope and checks

Only this review and .gitignore are adjusted for this task. Existing project files and local changes are preserved on feature/android-foundation; no commit, push, PR or merge is performed.

Ignore coverage includes .tools/, local.properties, Gradle/Kotlin caches, every module's build/, signing stores, environment files and conventional credential/secret filenames. Ignore checks use representative paths; filename patterns do not detect secrets embedded in arbitrary source and do not untrack already tracked files. No matching ignored paths were already tracked at verification.

This documentation/ignore update does not change Android sources or Gradle configuration. Android compilation and lint are not rerun for this update; their earlier foundation results remain recorded in [verification.md](verification.md). Historical backend execution and current-backend contract/security tests remain pending for the reasons stated above.

## Historical source references

[authentication]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/AuthenticationController.java
[users]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/UsersController.java
[companies]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/CompaniesController.java
[incidents]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/IncidentsController.java
[assignments]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/AssignmentsController.java
[notifications]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/notificationmanagement/interfaces/rest/NotificationController.java
[user-command]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/application/internal/commandservices/UserCommandServiceImpl.java
[user-query]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/application/internal/queryservices/UserQueryServiceImpl.java
[company-command]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/application/internal/commandservices/CompanyCommandServiceImpl.java
[company-query]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/application/internal/queryservices/CompanyQueryServiceImpl.java
[incident-command]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/application/internal/commandservices/IncidentCommandServiceImpl.java
[incident-query]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/application/internal/queryservices/IncidentQueryServiceImpl.java
[assignment-query]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/application/internal/queryservices/AssignmentQueryServiceImpl.java
[incident-domain]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/domain/model/aggregates/Incident.java
[assignment-domain]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/domain/model/entities/Assignment.java
[roles]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/domain/model/valueobjects/Roles.java
[role-domain]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/domain/model/entities/Role.java
[role-seed]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/application/internal/commandservices/RoleCommandServiceImpl.java
[startup]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/application/internal/eventhandlers/ApplicationReadyEventHandler.java
[signup-map]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/transform/user/SignUpCommandFromResourceAssembler.java
[jwt]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/infrastructure/tokens/jwt/services/TokenServiceImpl.java
[security]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/infrastructure/authorization/sfs/configuration/WebSecurityConfiguration.java
[filter]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/infrastructure/authorization/sfs/pipeline/BearerAuthorizationRequestFilter.java
[principal]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/infrastructure/authorization/sfs/model/UserDetailsImpl.java
[entrypoint]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/infrastructure/authorization/sfs/pipeline/UnauthorizedRequestHandlerEntryPoint.java
[user-domain]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/domain/model/aggregates/User.java
[assignment-map]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/transform/assignment/AssignmentResourceFromEntityAssembler.java
[incident-map]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/transform/incidents/IncidentResourceFromEntityAssembler.java
[openapi]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/shared/infrastructure/documentation/openapi/configuration/OpenApiConfiguration.java
[notification-map]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/notificationmanagement/application/internal/transform/NotificationAssembler.java
[incident-repository]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/infrastructure/persistence/jpa/repositories/IncidentRepository.java
[assignment-repository]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/infrastructure/persistence/jpa/repositories/AssignmentRepository.java
[hashing]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/infrastructure/hashing/bcrypt/services/HashingServiceImpl.java
[notification-dto]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/notificationmanagement/application/internal/queryservices/NotificationResponse.java
[SignInResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/resources/user/SignInResource.java
[SignUpResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/resources/user/SignUpResource.java
[AuthenticatedUserResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/resources/user/AuthenticatedUserResource.java
[UserResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/resources/user/UserResource.java
[UpdateProfileResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/resources/user/UpdateProfileResource.java
[CreateCompanyResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/resources/company/CreateCompanyResource.java
[CompanyResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/iam/interfaces/rest/resources/company/CompanyResource.java
[CreateIncidentResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/resources/incident/CreateIncidentResource.java
[IncidentResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/resources/incident/IncidentResource.java
[CreateAssignmentResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/resources/assignment/CreateAssignmentResource.java
[AssignmentResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/resources/assignment/AssignmentResource.java
[UpdateAssignmentPriorityResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/resources/assignment/UpdateAssignmentPriorityResource.java
[UpdateIncidentDocumentResource]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/resources/incident/UpdateIncidentDocumentResource.java
[IncidentAnalyticsResponse]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/java/com/nexorape/safework/service/incidentmanagement/interfaces/rest/resources/incident/IncidentAnalyticsResponse.java
[properties]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/resources/application.properties
[dev-properties]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/resources/application-dev.properties
[prod-properties]: https://github.com/NexoraPe-1ASI0732/backend-safework/blob/778fe1ec8e999b27e8d0340eb26fef50d1a49683/src/main/resources/application-prod.properties
