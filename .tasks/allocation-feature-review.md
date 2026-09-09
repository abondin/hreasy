# Allocation feature and external API review

## Scope and status

Static review of the allocation feature relative to master, including the current zero/null and loading fixes. Reviewed annual input, analytics, project/workstream lifecycle, employment dates, permissions, revisions, period locks, SQL persistence, external DTOs, token authentication, and nginx routing. No further execution was performed after the user reserved builds and runs for themselves.

## Implemented in the current iteration

- Explicit zero is persisted as a current allocation and rendered as zero in input, analytics leaves, and totals.
- Empty input sends null, deletes a current cell, and records null in revision history.
- Clearing is allowed outside employment; numeric values including zero remain disallowed there. Closed periods, project permissions, and optimistic revisions still apply.
- Annual employee queries retain employees referenced by allocations even when employment dates no longer overlap the year.
- Input and the add-employee control are disabled during load/save; single-cell and range pre-edit validation preserve the same rules.
- Existing migration V1.3.0.22 was edited at the user's request. The database will be recreated; no new migration remains.

## Remaining findings

### P2: Removing a workstream strands its current allocations

`backend/platform/src/main/java/ru/abondin/hreasy/platform/service/allocation/ResourceAllocationService.java`, `getProjectInput` and `validateWorkstream`.

Create future allocations for a direction, then remove that direction in project administration. Its rows remain in current allocations and analytics, but input lists only active directions and every save rejects the deleted direction, including null clears. This is an existing documented limitation, still relevant to daily planning and external totals. Either prevent deletion while current allocations exist or allow a deleted direction to be opened for clearing only. The current iteration changes employment cleanup, not workstream lifecycle policy.

### P2: Clearing an inactive project's last cell leaves input in an error state

`web/src/views/allocations/ResourceAllocationInputView.vue`, `save`/`loadView`, and `ResourceAllocationService.getProjectInput`.

A project outside the selected year remains selectable only because it has allocations. Clear its last annual cell and save. The write succeeds; the subsequent GET still requests that project, which has now disappeared from the eligible set. The service returns `invalid_reference`. The page has already cleared its loaded sheet, so the project list is empty, while the submitted draft remains until a successful load. A normal project selection is no longer available on that page. Resolve the selection again after this lifecycle transition, without weakening write authorization.

## External API usability

The present API supports complete read-only exports with a small number of requests. It is not an external allocation editor or incremental synchronization API; writes were explicitly excluded from v1.

| Consumer task | Current behavior | Assessment |
| --- | --- | --- |
| Export a year's allocation cells | One annual analytics GET, with employee/project/workstream lookup arrays | Usable; no per-cell requests needed |
| Distinguish zero from absence | Zero is a returned row; null/absence is represented by no row | Correct after this iteration; preserve sparse semantics |
| Resolve project external IDs | Additional `/projects` GET and ID join | Usable, but not self-contained; adding externalId to the analytics project DTO would remove this extra join if needed |
| Resolve workstream external IDs | Included analytics workstreams carry externalId, including referenced deleted workstreams | Use this array for historical rows, not the active dictionary alone |
| Resolve employees to ERP | HR Easy ID and email are available; extErpId is absent | Consumer needs an existing mapping; this was already an open contract decision |
| Interpret months | Zero-based YYYYMM in allocations; ISO YYYY-MM in overtime URL | Easy to misread; exact conversion is now documented |
| Detect removed cells | No deletion/change feed or revision in external analytics | Replace a successfully fetched annual snapshot; an upsert-only importer is incorrect |
| Identify finalized months | No closed-period state in external responses | Insufficient if the consumer must separate draft from closed monthly data |
| Change allocations | External security chain permits GET only | Intentionally unsupported; granting write authority does not enable external PUT |

External project IDs are optional. Workstream external IDs are unique only among active directions within a project. Use the full dimension key `(period, employeeId, projectId, workstreamId)` for HR Easy cells; do not collapse project-level and directed cells or assume global workstream external-ID uniqueness.

## Access and persistence observations

- `resource_allocation_read` deliberately exposes all allocation data, including through the external endpoint. It is not restricted to managed projects. Write operations still require the shared project hierarchy check.
- Saving and period closure take month locks in sorted order, and both are transactional. Per-cell revisions protect positive, zero, and delete operations.
- Read responses now preserve explicit zeros and employees with recorded cells. Sum calculations keep zero distinct from an absent group value.
- External tokens are opaque, configured as SHA-256 hashes, and resolved to the acting user's current authorities. The separate external chain is stateless and rejects non-GET methods.
- IP filtering is nginx-owned. Its effectiveness depends on deployment keeping the backend behind nginx; runtime topology was not validated.

## Validation boundary

Before the user's no-run instruction, the focused `ResourceAllocationServiceTest,ExternalApiControllerTest` Maven command and all 30 frontend unit tests passed. Type-check passed before the final small UI/test changes. This is not a browser, SQL integration, nginx, or live API verification. Final follow-up edits were checked by reading only; remaining execution belongs to the user.
