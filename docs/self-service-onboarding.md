# Self-service mobile onboarding

This branch starts from origin/test at da1068e, retaining the existing Android
Compose design system, incident handling, GPS capability and notification screens.
It uses the same app/business modules and the IAM, Incident Management and
Notification Management contexts. No Flutter rewrite or new framework is introduced.

## Behavior

Register with full name, email and password, then sign in. Signup sends exactly
fullName, emailAddress and password. It does not send roles, company IDs or codes.

An unassigned account sees Profile and Company. It can edit its profile, search
companies and send one pending request, or create its own company. Discovery shows
only company ID/name. Company selection and creation explain the permanent
one-company rule before submission. Creating a company requires a new login to
obtain employer access; the app does not silently retain the revoked session.

The employer sees company requests and a paginated member directory, approves or
rejects requests, and can revoke a worker with confirmation. It cannot revoke
itself or an employer. Approval requires the worker to sign in again. Rejected
workers may apply again. Revoked workers can sign in for their profile and request
reentry only to the original company. Incident and notification tabs require ACTIVE
membership, not merely a WORKER/EMPLOYER role. Their application use cases also
check active membership before making business requests.

401 responses clear the encrypted session and return to login. Company ViewModels
discard responses arriving after session invalidation and cannot issue more requests
using an old actor. Pending requests never grant access. Server authorization remains
authoritative. Stored tokens remain encrypted and bound to their API origin.

## Backend contract

Use the self-service backend branch feature/self-service-companies. User profiles
must contain nullable companyId and membershipStatus (UNASSIGNED, ACTIVE, REVOKED).
The company flow uses /api/v1/companies, /users/me/membership-requests,
/companies/{companyId}/membership-requests and their accept/reject routes.

The employer member directory requires the additional paginated
GET /api/v1/companies/{companyId}/members?page=0&size=20 route. The previous
GET /users returns only the caller and cannot populate a team directory. This
backend increment adds no migration or datasource/environment change; publish and
redeploy it before using the complete employer screen. DELETE
/companies/{companyId}/members/{userId} retains its existing revocation behavior.

## Run

The supplied Render service is configured as the public URL in gradle.properties.
Android Studio Run uses it without additional flags. To override it explicitly:

```powershell
.\gradlew.bat :app:assembleDebug "-Psafework.apiUrl=https://safework-backend-q6id.onrender.com/"
```

The APK is app/build/outputs/apk/debug/app-debug.apk. An HTTPS build does not need
the laptop backend or adb reverse. For a local backend, explicitly override
safework.localApiUrl and use adb reverse on a USB device. The default local port is
18086. Gradle properties contain the host only, never login credentials or database
settings. The Android app never connects directly to Aiven.

## Validation

Build and lint with JDK17, Android SDK35 and Build Tools35.0.0. Run app unit tests
and business tests. The three live integration tests require SAFEWORK_LIVE_API_URL
to name an isolated loopback server on port18086 or18090. They provision synthetic
accounts and companies without any ADMIN fixture and exercise real approval,
revocation, incident transitions and recipient isolation. They reject public hosts.
Without that explicit local fixture, those live tests are skipped and must not be
reported as passing integrations.

No production account needs to be created to run the automated local suite. A
public /health response alone does not prove deployed endpoint/schema compatibility.

## Results on 2026-11-14

- Debug APK built successfully with the Render HTTPS URL configured.
- Android app: 36 tests; business module: 7 tests. Zero failures, errors or skips.
- All three real local integration tests passed against the updated packaged backend
  on loopback18090 with isolated MySQL. No existing administrator was needed.
- Final PR verification: debug build and all 43 tests passed. Android lint reported
  zero errors and five warnings.
- Backend Maven clean verify: 41 tests, zero failures/errors/skips.
- Backend member directory published as e4f447e on feature/self-service-companies;
  its route was subsequently confirmed in the deployed OpenAPI document.
- Installed the Render-configured APK on a Huawei P20 Pro (CLT-L29) over USB and
  verified that SafeWork launched in the foreground. The user confirmed successful
  registration and sign-in against Render and subsequently reported that the app
  worked correctly. This is user-reported acceptance, not an automated production
  test of every employer/worker flow.
- Aiven initially required non-null users.fk_company_id and user_sessions.company_id,
  incompatible with registration before company membership. The user changed both
  columns to nullable and supplied verification results. Other newer structures
  already existed without flyway_schema_history: a complete migration-history and
  legacy-data reconciliation remains separate backend work. Do not blindly rerun
  the legacy migration or interpret these two corrections as full migration proof.
- An initial phone connection error disappeared on retry after the health endpoint
  responded. The exact cause was not established. The app has a 30-second total
  request timeout and does not automatically retry registration requests.
- Local integrations use only synthetic accounts on isolated MySQL; they do not
  modify Render/Aiven. Production registration above was performed by the user.
- Logs, generated APK and local verification metadata are ignored build artifacts.
  See .tools/pr-review-build.log and .tools/frontend-verification.json locally.
