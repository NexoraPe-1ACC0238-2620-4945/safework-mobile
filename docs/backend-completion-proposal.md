# Backend completion proposal for discussion

This is a proposed contract, not implemented server behavior. The current Android increment uses only existing endpoints. The user requested discussion before backend changes or substantial restructuring.

## First backend increment: profile department and internal notes

These capabilities can be developed and tested locally without choosing a new external provider.

| Area | Proposed change | Authorization and validation |
| --- | --- | --- |
| Department | Add optional `department` to the persisted user profile and own-profile response. Accept it in `PATCH /api/v1/users/me`. | Own account only; no role/company override. Proposed limit: 120 Unicode code points. Omitted value keeps the current department; define explicit clearing consistently before implementing. |
| Create note | `POST /api/v1/incidents/{incidentId}/notes` with `{ "text": "..." }`; return 201 and the created note. | Proposed: responsible EMPLOYER only, same company. Nonblank text up to 4000 Unicode code points. Author and company come from the authenticated identity. |
| List notes | `GET /api/v1/incidents/{incidentId}/notes`; return notes in a stable order. | Proposed: same-company EMPLOYER readers. WORKER cannot read internal notes. A foreign company's incident is not disclosed. |

Proposed note response: `id`, `incidentId`, `authorId`, `authorName`, `text`, `createdAt`. Persist the incident/author relationship and creation timestamp. Keep internal notes separate from the worker-visible incident description and notification body.

Prepare additive schema changes and regression tests before deployment. Check compatibility with the installed Kotlin client, public OpenAPI and the Flutter client's future DTOs. Do not change deployment settings or production schema merely to prepare the code.

## Second backend increment: file attachments

Document links alone do not satisfy file-upload stories. A durable object-storage provider must be selected/configured before this stage can be considered usable.

- Proposed metadata: attachment ID, incident/company/author, original display filename, detected content type, byte size, storage object key and creation timestamp.
- Proposed operations: create an authorized upload for an existing incident, finalize verified upload metadata, list attachments visible to that incident reader, and request an authorized read URL.
- Define allowed image/document types and byte limits with the team. Verify content on the server, not only the client's filename extension. A raw client path is not an attachment.
- Android creates the incident once, then uploads selected/captured evidence. A failed upload must offer retry for that incident instead of creating a duplicate incident. Show the saved report and any failed attachments separately.
- Do not persist an expiring signed read URL as the permanent document identity. Store the object key and issue a short-lived read link through an authorized server operation.
- Decide deletion, retention and cleanup of unfinalized uploads before implementing the lifecycle.

No provider, paid plan, domain or credentials have been selected or configured by this proposal.

## Third backend increment: push notifications

The delivery provider and its server credentials must be configured. Existing database notification rows remain the in-app history.

- Proposed authenticated device-registration operation links a provider token to the correct user/device and supports rotation and deregistration.
- Reuse the existing incident-event recipients rather than expanding company visibility.
- Send push after the incident transaction succeeds; distinguish an internal notification record, provider acceptance and observed phone delivery.
- Keep permanent provider credentials on the server. Define token/session revocation behavior so logout or an account change cannot leave another user's messages on that device.
- Android needs provider initialization, the applicable notification permission, notification display and handling of a notification tap with an authorized fetch of current incident data.

## Scope decisions to confirm

The backlog's selection of another responsible user, public role selection and global ADMIN report access differ from the currently implemented security model. These are business decisions, not missing buttons that can safely be added in isolation. Preserve current permissions while the team confirms the intended scope.

The audit story also needs a defined event list and authorized reader. Existing audits of some administrative changes do not establish a complete platform activity history.
