# Backend Guidelines

## Structure and Stack

- `pom.xml` is the Maven reactor for `parent`, `common`, `platform`, `notify-ms`, and legacy `telegram` modules.
- Java 25 and Spring Boot 4.x are used with WebFlux, R2DBC, PostgreSQL, and Flyway.
- Follow existing packages under `ru.abondin.hreasy`.
- Use the root `.agents/skills/java-coding-style` skill for Java changes, `junit-tests-rules` for JUnit work, and `db-migration-style` for Platform Flyway migrations.

## Build and Test

- Run backend commands from the repository root so reactor dependencies are available.
- Use `mvn -q -f backend/pom.xml test` only for backend-wide validation.
- For a Platform test, use `mvn -q -f backend/pom.xml -pl platform -am -Dtest=SomeTest "-Dsurefire.failIfNoSpecifiedTests=false" test`.
- For a Notify MS test, use `mvn -q -f backend/pom.xml -pl notify-ms -am -Dtest=SomeTest "-Dsurefire.failIfNoSpecifiedTests=false" test`.
- Start with the affected test or module; `-am` builds only its required reactor dependencies.
- If dependency resolution fails, check the configured Nexus before changing dependency versions.

## Data and Service Boundaries

- Use R2DBC repositories for runtime database access unless nearby code establishes another requirement.
- Add Flyway migrations under `src/main/resources/db/migration`; never edit an applied migration.
- Prefer schema-qualified SQL in migrations.
- Do not add cross-service foreign keys.
- Keep service-owned schemas isolated: Platform owns `notify`; Notify MS owns `notify_ms`.
- For R2DBC mappings, do not put `schema.table` in `@Table` when it would be quoted as one identifier; configure the schema or search path instead.
- In Platform, use `BusinessError` for localized client-facing failures and existing `BusinessErrorFactory` methods for common reactive errors.
- Treat backend roles and permissions as the source of truth for protected data, actions, and notification recipients.
