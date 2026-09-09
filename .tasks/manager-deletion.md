# Manager deletion

## Goal

- Show backend deletion errors inside the manager deletion dialog.
- Allow `admin_managers` users and the employee who created a manager link to delete it.

## Decisions

- Reuse the existing `created_by` column; no migration is required.
- Compare boxed employee IDs by value with `Objects.equals`; the authorization test uses an ID outside the JVM integer cache.
- Keep physical deletion plus the existing immutable manager history record.
- Project editors may still create and update project-manager links under the existing project access rule.

## Progress

- [x] Traced backend and compact/full manager UI flows.
- [x] Added creator-or-admin deletion authorization.
- [x] Added deletion error feedback to the confirmation dialog.
- [x] Added focused authorization tests and validated backend, type-check, and targeted ESLint.
- [x] Audited backend getter comparisons: fixed the manager boxed-ID comparison and the one other confirmed boxed/boxed comparison in salary-request link validation; primitive comparisons were left unchanged.
