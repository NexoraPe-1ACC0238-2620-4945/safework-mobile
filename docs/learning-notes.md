# Learning notes: Android completion and the Flutter transition

We discuss code in English and explain each working increment. Flutter is the UI framework; Dart is the language used to write the Flutter version. The current Android increment remains Kotlin/Compose.

## One operation through the layers

Saving a document reference follows this path:

1. **Presentation:** `IncidentScreen` displays the URL form and forwards the button action. `IncidentViewModel` keeps the draft, loading/error state and selected incident.
2. **Application:** `IncidentUseCases.updateDocument` validates the operation's input and checks that the returned incident belongs to the authenticated company and matches the requested ID and URL.
3. **Domain:** `IncidentDocumentUrl` represents a valid HTTPS reference. Its factory rejects invalid schemes, embedded credentials, malformed hosts and overlong values before sending an HTTP request. `IncidentRepository` defines what the operation needs without depending on Android or HTTP.
4. **Infrastructure:** `HttpIncidentRepository` implements that contract using the existing authenticated `ApiClient`. It sends only `documentUrl` to the server's existing PATCH route and maps the returned JSON to a domain `Incident`.
5. **Back to Presentation:** the ViewModel replaces the incident in its list and detail state. Compose displays the changed state. A late response after session invalidation is discarded.

The backend is the final authority for reporter/responsible-user permissions. Showing an editor to an EMPLOYER does not grant that user permission to update someone else's incident.

## Why separate these responsibilities?

- A button should not construct an authenticated HTTP request.
- A domain rule can be tested without an Android device.
- The repository interface lets tests exercise loading, permission errors and late responses without changing the real database.
- The HTTP implementation can change while the use case keeps the same responsibility.

These responsibilities match the professor's feature-oriented examples. His Android projects use Kotlin interfaces/use cases/ViewModels; his Flutter project uses Dart abstract repositories/use cases and `ChangeNotifier` ViewModels.

## Syntax bridge

| Kotlin concept in this app | Dart concept in the professor's Flutter example | Meaning |
| --- | --- | --- |
| `val repository: IncidentRepository` | `final BookRepository repository;` | The field cannot be reassigned after initialization. This does not make the referenced object deeply immutable. |
| `suspend fun list(): List<Incident>` | `Future<List<Book>> getBooks()` | Asynchronous work returns a collection. Dart `async`/`await` expresses how the method waits for that result. |
| `interface IncidentRepository` | `abstract class BookRepository` | A contract names available operations; an implementation supplies their behavior. |
| `class HttpIncidentRepository : IncidentRepository` | `class BookRepositoryImpl implements BookRepository` | The infrastructure class implements the domain contract. |
| `state.value.copy(...)` and observable state | ViewModel fields plus `notifyListeners()` | Tell the UI that screen state changed. The mechanics and lifecycle differ between Kotlin and Flutter. |
| Compose functions | Widgets and their `build` method | Describe the UI from current state. |

In the professor's `GetBooksUseCase`, `Future<List<Book>>` means "a result that will eventually contain a list of books". In the eventual SafeWork Flutter version, `Future<List<Incident>>` will mean the same thing for incidents. `await repository.getIncidents()` waits without blocking the UI thread while the asynchronous operation is pending.

## How we will learn a Flutter feature

For each feature, explain its purpose and files, implement a small complete flow, explain the Dart syntax used, run checks, and ask the user to describe the flow in their own words. Begin with the project entry point and a real sign-in flow, then profile, incident list/detail/report and the remaining agreed features.

The Flutter directory proposal will be reviewed before creating the project. It will follow the professor's `lib/core` and `lib/features/<feature>/{domain,application,infrastructure,presentation}` approach where those layers have actual responsibilities. The current Kotlin app and working backend remain usable throughout the transition.

## Reviewed teaching sources

- [Flutter use case](https://github.com/upc-pre-202620-1acc0238-4945/easybook-mobile-flutter/blob/e7047168a059824970f731a3b0872de153474e5d/lib/features/home/application/get_books_use_case.dart)
- [Flutter repository contract](https://github.com/upc-pre-202620-1acc0238-4945/easybook-mobile-flutter/blob/e7047168a059824970f731a3b0872de153474e5d/lib/features/home/domain/book_repository.dart)
- [Flutter ViewModel](https://github.com/upc-pre-202620-1acc0238-4945/easybook-mobile-flutter/blob/e7047168a059824970f731a3b0872de153474e5d/lib/features/home/presentation/home_view_model.dart)
- [Flutter dependency registration](https://github.com/upc-pre-202620-1acc0238-4945/easybook-mobile-flutter/blob/e7047168a059824970f731a3b0872de153474e5d/lib/core/service_locator.dart)

These examples establish the course conventions. SafeWork must still implement real authentication, error handling and account-specific lifecycle behavior where a teaching example is simplified.
