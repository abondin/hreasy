# Resource Allocations

Resource allocations help managers plan how much of each employee's capacity is assigned to projects throughout a calendar year.

The feature has two sections:

- **Data entry** — plan and update allocations for one project;
- **Analytics** — review allocations across employees and projects without editing them.

## Enter allocations

1. Open **Managers → Resource allocations → Data entry**.
2. Select a year, a project, and optionally one of its workstreams. The current year and the first writable project are selected by default.
3. Find an employee in the grid or add one through the last row.
4. Enter allocation percentages for the required months.
5. Review the changes and click **Save**.

All 12 months are shown on one screen. The selected year, project, and optional workstream are kept in the page URL, so the same view can be reopened or shared.

Project-level allocations and allocations for several workstreams of that project are independent and may coexist for the same employee and month.

The employee list contains:

- employees currently assigned to the selected project;
- employees who already have allocations on the project during the selected year;
- employees added to the current unsaved draft.

### Read a cell

The large value is the employee's allocation on the selected project. Outside edit mode it is displayed with `%`; the editor accepts a number.

A small `+ N%` in the lower-right corner shows the employee's combined allocation on all other project/workstream dimensions for that month. It is hidden when that allocation is zero, keeping the selected value visually primary.

For example, `50%` with `+ 30%` means that the employee is allocated 50% to the selected project and another 30% to other projects.

### Editing rules

- Values must be whole numbers from `0` through `1000`.
- An empty value or `0` removes the allocation.
- A month can be edited only while it is open and overlaps the employee's employment period.
- The employee's dismissal month remains editable; later months do not.
- Closed months have a lock in the column header and cannot be changed.
- Employees dismissed by the current date are marked in the employee column. If an active employee belongs to another current project, that project is shown next to the name.

Copy, paste, range selection, autofill, Tab, and Enter work directly in the grid. Changing the year, project, or page with unsaved changes requires confirmation.

### Concurrent changes

If another user changes the same cell first, the page loads the current server value and marks that cell in red. Changes in other cells remain in the draft. Review the marked cells, edit them again if necessary, and save.

## Analyze allocations

Open the **Analytics** tab to review non-zero allocations for the selected year. Analytics is read-only.

Two views are available:

- **Projects** — business account → project → workstream → employees;
- **Employees** — employee → project → workstream.

Projects remain the parent level. Project rows show a single workstream name or a workstream count inline and can be expanded even when only one workstream exists. Workstream levels are collapsed by default. Project-level values are shown as `Without workstream`.

Users with allocation admin permission can close or reopen a selected month from the analytics toolbar.

Group rows show monthly totals. Data can be filtered by business account and project or searched by employee, project, and workstream name.

## Access and restrictions

The backend checks all permissions and project scopes. Hiding controls in the UI is not considered an access restriction by itself.

| Permission | User capability |
| --- | --- |
| `resource_allocation_read` | View all allocations and analytics. |
| `resource_allocation_write` | Edit allocations for projects available through the acting user's effective hierarchy access. |
| `resource_allocation_admin` | Close and reopen months. |

Authentication merges manager responsibilities with explicit project, department, and business-account access. Allocation authorization uses the same shared `ProjectHierarchyAccessor` as assessments and neighboring workflows. A writable project may receive allocations for any employee. Projects outside the write scope remain visible but read-only.

Closed months remain protected by the backend even if a save request is sent manually.

### Current limitations

- Cell comment threads are not implemented.
- A workstream removed from project editing is no longer available for allocation input. Existing cells remain visible in analytics, but cannot currently be cleared through the input page.

### Project and workstream lifecycle

Project administrators edit the optional project `externalId` and its workstreams on the project form. A workstream contains `id`, optional `externalId`, `displayName`, and optional `description`; removing it from the form sets `deleted_at`/`deleted_by` instead of deleting the row. New overtime and allocation entries accept active workstreams belonging to the selected project only. The project dictionary and external project API return active workstreams, while allocation analytics and detailed overtime reports resolve referenced soft-deleted workstreams for historical display.

## Implementation details

The frontend uses two child routes:

- `/management/resource-allocations/input?year={year}&projectId={projectId}&workstreamId={workstreamId}`;
- `/management/resource-allocations/analytics`.

### API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/resource-allocations/input/{year}?projectId={id}&workstreamId={id}` | Load annual input data for an optional workstream, closed periods, and employee/month totals on all other allocation dimensions. |
| `PUT` | `/api/v1/resource-allocations/input/{year}/{projectId}?workstreamId={id}` | Save changed employee/month cells for an optional workstream as one revision. |
| `GET` | `/api/v1/resource-allocations/analytics/{year}` | Load annual read-only analytics. |
| `GET` | `/api/v1/resource-allocations/closed-periods/{year}` | Load closed months for the analytics toolbar. |
| `PUT` | `/api/v1/resource-allocations/closed-periods/{period}` | Close a month. |
| `DELETE` | `/api/v1/resource-allocations/closed-periods/{period}` | Reopen a month. |

Periods use the repository's zero-based `YYYYMM` convention: `202600` is January 2026 and `202611` is December 2026.

The annual input response separates allocations on the selected project from combined allocations on other projects:

```json
{
  "allocations": [
    { "period": 202600, "employeeId": 42, "percent": 50, "revisionId": 17 }
  ],
  "otherAllocations": [
    { "period": 202600, "employeeId": 42, "percent": 30 }
  ]
}
```

The save request contains only changed cells. `expectedRevisionId` is `null` for a new cell, while `percent: 0` removes an existing allocation:

```json
{
  "changes": [
    {
      "period": 202600,
      "employeeId": 42,
      "percent": 50,
      "expectedRevisionId": 17
    }
  ]
}
```

### Persistence and concurrency

The Platform service owns the `alloc` schema:

- `resource_allocation` stores current non-zero monthly values for a project and optional workstream;
- `resource_allocation_revision` stores one Save operation for a project and optional workstream;
- `resource_allocation_change` stores immutable before/after cell history;
- `resource_allocation_closed_period` stores globally closed months.

One Save creates one project/year revision. Setting a cell to zero physically removes it from the current-state table because that table stores only non-zero values. The deletion remains auditable as an immutable change with the previous percentage and `new_percent = 0`; unlike ordinary CRUD entities, a second soft-deleted copy would duplicate the existing revision history.

Saving and closing periods are transactional. PostgreSQL transaction advisory locks serialize writes for affected months, and per-cell revision checks reject stale changes.
