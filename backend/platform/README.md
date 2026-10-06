# HR Easy Platform

Platform is the main backend for employee records, projects, allocations, vacations, overtime, salary workflows, and the web notification inbox.

## Stack

- Java 25 and Spring Boot 4 (versions are pinned in `../parent/pom.xml`).
- Spring WebFlux and Spring Security.
- PostgreSQL with R2DBC for runtime persistence and Flyway for migrations.
- JXLS and Apache POI for Excel import/export.

## Build

Run Maven commands from the monorepository root:

```shell
mvn -q -f backend/pom.xml -pl platform -am -DskipTests package
```

## Run locally

Start PostgreSQL using the repository's development Compose file:

```shell
docker compose -f .hreasy-localdev/docker-compose.yml up -d hreasypg
```

Build the application, then run its executable JAR:

```shell
java -jar backend/platform/target/platform-1.4.0-SNAPSHOT-exec.jar --spring.profiles.active=dev --hreasy.notifications.delivery-service.enabled=false
```

The `dev` profile configures PostgreSQL at `localhost:5432`, database `hr`, and the local-development credentials in `.hreasy-localdev/docker-compose.yml`. Override `hreasy.db.host`, `port`, `database`, `username`, and `password` for a different database. The backend listens on port `8081`.

Flyway runs the commands in `hreasy.db.flyway-commands` at startup; the default is `migrate`. Database connection properties are shared by the R2DBC and Flyway configurations. `clean,migrate` deletes existing data and is intended only for disposable test databases.

Authentication supports LDAP and internal passwords. Configure `hreasy.ldap.*` for LDAP. The `dev` profile enables internal passwords and a development master password; use production authentication settings outside local development.

The [web dev server](../../web/README.md) proxies `/api` to this backend. For direct browser calls, configure `hreasy.web.sec.cors-allowed-origins` to match the frontend origin.

For external messenger delivery, enable and configure `hreasy.notifications.delivery-service.*` and run [Notify MS](../notify-ms/README.md). The web inbox does not require external delivery.

## Tests

Run the narrowest relevant test from the monorepository root:

```shell
mvn -q -f backend/pom.xml -pl platform -am -Dtest=AdminEmployeesExportedTest -Dsurefire.failIfNoSpecifiedTests=false test
```

PostgreSQL integration tests require Docker. Testcontainers starts `postgres:15` and applies migrations to a disposable database. To use an existing test database, set `-Dhreasy.test.existing-database-docker=true` and configure `hreasy.db.*` using the test profile.

## Employee Excel export

The employee admin export produces two worksheets:

- **Employees** (`Сотрудники`): the selected employee records.
- **Children** (`Дети`): employee name, employee email, child name, birthday, and age for children of the exported employees.

The include-dismissed filter applies to both sheets. A child row belongs to one employee record; children entered separately for two parents produce separate rows. The export does not deduplicate family records. Birthdays are Excel dates; missing birthdays and ages remain blank. Skype is not included.

The template is [`src/main/resources/jxls/admin_employees_template.xlsx`](src/main/resources/jxls/admin_employees_template.xlsx). Preserve its JXLS comments when editing the workbook.

## Further documentation

- [External API](../../.docs/external_api_hld.md)
- [Resource allocations](../../.docs/resource_allocations.md)
- [Notification catalog](../../.docs/notification_catalog.md)
- [Release history](../../changelogs/CHANGELOG.md)
