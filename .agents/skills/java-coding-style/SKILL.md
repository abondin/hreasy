---
name: java-coding-style
description: Apply HREasy Java 25, Spring Boot 4, WebFlux, R2DBC, error-handling, and security conventions when creating or editing backend Java code.
---

# HREasy Java Conventions

Follow `backend/AGENTS.md` and the style of the affected module.

## Code and Structure

- Use Java 25 without preview features.
- Keep packages under the existing `ru.abondin.hreasy` module hierarchy.
- Use 4-space indentation, braces for control flow, standard IntelliJ import grouping, and UTF-8 text.
- Do not reformat, rename, or refactor unrelated code.
- Reuse existing helpers and domain types before creating a new class or abstraction.
- Keep business logic out of controllers, entities, configuration, and repository classes.

## Spring and Reactive Code

- Runtime persistence uses Spring Data R2DBC repositories, commonly `ReactiveCrudRepository` and `R2dbcRepository`.
- Follow existing Reactor patterns and avoid introducing blocking calls into reactive runtime flows.
- Prefer constructor injection; `@RequiredArgsConstructor` is the dominant production pattern.
- Prefer `@ConfigurationProperties` for grouped configuration.
- Follow nearby Lombok usage; `@Data`, `@RequiredArgsConstructor`, and `@Slf4j` are established in this repository.
- Do not add a service interface for a single implementation.
- Do not introduce a nullability annotation dependency; none is currently used.

## Errors, Logging, and Security

- In Platform, use `BusinessError` for localized client-facing business failures.
- Reuse `BusinessErrorFactory` for its existing common reactive errors such as entity-not-found results.
- Use standard Java exceptions for invalid configuration, programmer errors, and module-internal failures when nearby code does so.
- Preserve useful error context without logging secrets or protected employee data.
- Treat backend role and permission checks as authoritative; never rely on frontend checks for security.
- For notification code, derive recipients from the same role and permission model that controls the linked business object.

## Completion

- Make the smallest change that solves the task.
- Run the narrowest relevant compile or test command from `backend/AGENTS.md`.
- Report the command run, or state why validation was not run.
