---
name: db-migration-style
description: Use this skill when creating or editing backend/platform Flyway SQL migrations in this repository.
---

# DB migration style

Use this skill for `backend/platform/src/main/resources/db/migration/**.sql`.

## Migration files

- Follow the existing version folder and file naming pattern. Pick the next available Flyway version in the target
  version folder.
- Match the surrounding SQL style.
- Use existing schemas when the domain already fits. For employee current-project workflow, prefer `empl`.
- Create sequences explicitly before integer primary-key tables.
- Use `integer PRIMARY KEY NOT NULL DEFAULT nextval('schema.sequence_name')` for generated ids.
- Define foreign keys inline in the column when practical.

```sql
CREATE SEQUENCE IF NOT EXISTS schema.entity_id_seq;
```

## Status/state columns

- Store business states as `integer`, not PostgreSQL enum or text, unless an existing table in the same domain already uses another pattern.
- Prefer the column name `state` for request/workflow decisions.
- Document state values in `COMMENT ON COLUMN`.
- For project transfer requests use:
  - `1 - Pending`
  - `2 - Approved`
  - `3 - Rejected`
  - `4 - Canceled`
- In code constants or API labels, use `CANCELED` spelling when an English enum/string value is needed.

## Audit columns

- New business tables normally include:
  - `created_at timestamp with time zone NOT NULL`
  - `created_by integer NOT NULL REFERENCES empl.employee (id)`
- Add `updated_at` and `updated_by` when a row changes state after creation.
- Add `deleted_at` and `deleted_by` only when the business flow includes soft deletion. Do not add them only out of habit.
- For immutable audit-like request tables, prefer retaining rows via `state` instead of soft deleting them.

## Comments

- Add `COMMENT ON TABLE` for every new table.
- Add `COMMENT ON COLUMN` for every column.
- Use comments to document integer state meanings and non-obvious business rules.
- Use concise English comments consistent with nearby migrations.

## Indexes and constraints

- Add partial unique indexes for active-only business uniqueness.

```sql
CREATE UNIQUE INDEX entity_pending_unique
ON schema.entity (business_key_1, business_key_2)
WHERE state = 1;
```

- Add indexes for expected list endpoints, especially inbox/outbox patterns.

```sql
CREATE INDEX entity_inbox_idx
ON schema.entity (approver_employee_id, state, created_at DESC);

CREATE INDEX entity_created_by_idx
ON schema.entity (created_by, state, created_at DESC);
```

- Use `CHECK` constraints sparingly. Existing migrations mostly rely on comments and service validation for state dictionaries.

## history.history entity types

- When adding a business entity that will be written to `history.history`, update the comment on `history.history.entity_type`.
- Preserve existing entries and append the new one.
- For project transfer request, add:

```sql
  [project_transfer_request] - Project transfer request
```

## Views

When a migration changes a table, column, column type, column semantics, or drops/renames anything, check affected views.

```shell
rg -n "CREATE( OR REPLACE)? VIEW|create view|CREATE VIEW|DROP VIEW|drop view" backend/platform/src/main/resources/db/migration -g '*.sql'
```

```shell
rg -n "view_name|changed_table|changed_column" backend/platform/src/main/resources/db/migration -g '*.sql'
rg -n "FROM .*changed_table|JOIN .*changed_table|changed_column" backend/platform/src/main/resources/db/migration -g '*.sql'
```

- Identify views whose SQL references the changed table or column.
- Find the newest definition for each affected view by migration order.
- Recreate the view in the new migration when a referenced column is dropped, renamed, type-changed, or semantically changed.
- Check `table_alias.*` views after adding or removing columns because repository-facing result shapes may change.
- If recreation is needed, add `DROP VIEW`/`DROP VIEW IF EXISTS` and the full updated `CREATE VIEW` or `CREATE OR REPLACE VIEW`.

## Request/workflow tables

- integer `state` with state comments
- `created_at`, `created_by`
- `updated_at`, `updated_by` if state can change
- partial unique indexes for active pending duplicates when duplicates are not allowed
- indexes for expected inbox/outbox list queries
- Prefer one stateful request table before adding action/event tables, views, or snapshot columns.
