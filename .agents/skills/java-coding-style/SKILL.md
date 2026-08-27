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
- Add concise Javadoc to main classes and public methods. Document contracts, security scope, domain conventions, and non-obvious behavior; do not restate signatures or add Javadoc to obvious accessors and private helpers.
- Do not reformat, rename, or refactor unrelated code.
- Reuse existing helpers and domain types before creating a new class or abstraction.
- Keep business logic out of controllers, entities, configuration, and repository classes.

## Spring and Reactive Code

- Runtime persistence uses Spring Data R2DBC repositories, commonly `ReactiveCrudRepository` and `R2dbcRepository`.
- Name persistence models `*Entry`, query-only SQL projections `*View`, API responses `*Dto`, and request payloads `*Body`. Do not expose repository models through API contracts.
- Prefer Spring Data repositories with `@Query` for ordinary CRUD and read queries. Use one focused `R2dbcEntityTemplate` repository when custom SQL such as UPSERT, `INSERT ... RETURNING`, or transactional revision/change writes is materially simpler than splitting the flow across several CRUD repositories.
- Follow existing Reactor patterns and avoid introducing blocking calls into reactive runtime flows.
- Prefer constructor injection; `@RequiredArgsConstructor` is the dominant production pattern.
- Prefer `@ConfigurationProperties` for grouped configuration.
- Follow nearby Lombok usage; `@Data`, `@RequiredArgsConstructor`, and `@Slf4j` are established in this repository.
- Use Lombok when it removes boilerplate from mutable models and Spring components. Prefer Java records for small immutable values when framework binding and mapping requirements allow them.
- Use MapStruct for reused or structurally non-trivial mappings. Keep one-off constructor mappings explicit when a mapper would add more structure than it removes, especially when fields depend on runtime access or business context.
- Do not add a service interface for a single implementation.
- Do not introduce a nullability annotation dependency; none is currently used.

## Errors, Logging, and Security

- In Platform, use `BusinessError` for localized client-facing business failures.
- Reuse `BusinessErrorFactory` for its existing common reactive errors such as entity-not-found results.
- Use standard Java exceptions for invalid configuration, programmer errors, and module-internal failures when nearby code does so.
- Preserve useful error context without logging secrets or protected employee data.
- For business mutations, log a concise operation summary when nearby services do so: actor, stable identifiers, and item count are usually enough. Do not log full request payloads by default.
- Treat application logs and persisted business history as separate concerns. Do not duplicate a feature's revision/history data in another history store unless the requirements call for both.
- Treat backend role and permission checks as authoritative; never rely on frontend checks for security.
- Put feature-specific permission and object-scope rules in a `*SecurityValidator`; keep business services focused on orchestration. The validator must cover both coarse permissions and scoped access used by mutations.
- For notification code, derive recipients from the same role and permission model that controls the linked business object.

## Completion

- Make the smallest change that solves the task.
- Run the narrowest relevant compile or test command from `backend/AGENTS.md`.
- Report the command run, or state why validation was not run.
