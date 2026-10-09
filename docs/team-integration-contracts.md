# Shared mobile integration contracts

2026-10-08 (America/Lima). Central preparation with Carlos and Codex does not fabricate teammate authorship. Each recipient incorporates, reviews, validates and publishes their own changes with their existing Git identity.

| Owner | Package responsibility | Shared dependency |
| --- | --- | --- |
| Carlos | IAM, session, navigation/technical configuration | Publishes `feature/iam` to `test`; supplies `UserProfile` with validated user/company IDs and roles and protected API/session infrastructure |
| Daniel | Incident query/detail/report and optional device location capture | Consumes IAM identity; establishes IncidentManagement Domain model and query/report repository/use cases |
| Francisco | Incident self-assignment/start/close and NotificationManagement | Consumes the same Incident model established by Daniel and IAM session; adds handling/assignment ports within the same context |

IncidentManagement is one context under `com.nexorape.safework.incidentmanagement`. Assignment belongs there. NotificationManagement is separate under `...notificationmanagement`. Domain/Application remain pure Kotlin in `business`; Presentation/Infrastructure remain in `app`. No `features` layer or screen-named business context.

## Agreed model/transport boundaries

- IAM exposes `UserProfile.id`, `companyId`, `roles`, full name, normalized email and optional phone. Credentials/JWTs remain in Infrastructure. All protected transports use the shared API/session store, including clearing the matching credential on401; valid-session403 keeps it.
- Incident ID, reporter user ID and company ID are positive integer identities. State is exactly OPEN, ASSIGNED, IN_PROGRESS, CLOSED. Title/description/location are validated nonblank Unicode text with limits120/4000/500. `location` stays text; coordinates are composed into it only after consent/capture and remain editable manually.
- Map all ten historical IncidentResource fields: id, userId, companyId, title, description, location, status, documentUrl, reporterName, assigneeName. `userId` identifies the reporter, not the responsible. No assignmentId/assigneeUserId field is assumed.
- Assignment has its own ID, incidentId, userId (responsible), incidentTitle, status, assignedAt, priority and nullable completionDate. Responsibility is established by own assignments/returned assignment IDs, never by comparing display names. Taking sends incidentId only; the server derives the EMPLOYER caller. No selection of another responsible.
- Query/report and handling ports operate on the same Incident entity/value objects/state model. Separate interfaces reflect use cases, not separate bounded contexts or duplicated incident models. Server checks remain authoritative even when local use cases guide transitions.
- Notifications contain UUID id, subject, body, zoned createdAt and isRead. Query only `/notifications/my-notifications`; no user selector, mark-read or push. Use IAM's session; do not assume isRead=false proves tracking exists.
- Safe errors distinguish validation, invalid session401, permission403, invisible/missing404, conflict409, network and server failures. Do not display arbitrary server errors or log credentials. Lists currently have no pagination/filter contract.

## Integration order and ownership

1. Review/integrate IAM PR into mobile `test` through the agreed workflow; no automatic merge is performed.
2. Daniel applies his isolated package to a feature branch based on the supplied IAM revision and incorporates Incident query/report/location and shared model/navigation.
3. Francisco applies his package after Daniel's prerequisite tree; it extends Incident handling and adds notifications without replacing the shared model. Package delivery specifies exact base SHA and prerequisite patch digest to detect drift.
4. Recipients run provided business/HTTP/real-local checks, build/lint and physical-device checks, then make their own Conventional Commits/PRs to `test`. Validate shared `test` before PR to `main`.

Package preparation/testing and integration are distinct. Packages stay outside versioned files, contain no `.git`, secrets, logs or build outputs, and are not published as Carlos's completed incident/notification contributions. Physical-device checks absent from a report remain pending, not inferred from compilation.
