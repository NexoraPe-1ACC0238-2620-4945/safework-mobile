# Business module

This is a Kotlin/JVM module targeting JDK 17. IAM now contains identity value objects, the user profile, repository interface, safe failure categories and Application use cases, with rule tests. It imports no Android, HTTP/DTO or persistence framework.

Domain and Application code belongs directly under the `com.nexorape.safework` package, using `iam`, `incidentmanagement` and `notificationmanagement` as bounded contexts. The Gradle module name `business` is not an intermediate package. Incident/Notification implementation is delivered separately for teammate integration.

Use constructor parameters for dependencies. Domain defines repository interfaces, entities and validated value objects; Application orchestrates use cases through those interfaces. Android, UI, Retrofit, Room, DTOs and framework DI wiring belong in `app`.

See [the architecture decision](../docs/architecture.md) for responsibilities, dependency direction and the planned package layout. Add source files when actual use cases are implemented; do not create placeholder entities, repositories or context classes.
