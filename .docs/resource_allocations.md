# Resource Allocations

Resource allocations help managers plan how much of each employee's capacity is assigned to projects throughout a calendar year.

The feature has two sections:

- **Data entry** — plan and update allocations for one project;
- **Analytics** — review allocations across employees and projects without editing them.

## Enter allocations

1. Open **Managers → Resource allocations → Data entry**.
2. Select a year and a project. The current year and the first managed project are selected by default.
3. Find an employee in the grid or add one through the last row.
4. Enter allocation percentages for the required months.
5. Review the changes and click **Save**.

All 12 months are shown on one screen. The selected year and project are kept in the page URL, so the same view can be reopened or shared.

The employee list contains:

- employees currently assigned to the selected project;
- employees who already have allocations on the project during the selected year;
- employees added to the current unsaved draft.

### Read a cell

The large value is the employee's allocation on the selected project. Outside edit mode it is displayed with `%`; the editor accepts a number.

A small `+ N%` in the lower-right corner shows the employee's combined allocation on all other projects for that month. It is hidden when the other-project allocation is zero, keeping the selected-project value visually primary.

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

- **Projects** — business account → project → employees;
- **Employees** — employee → projects.

Group rows show monthly totals. Data can be filtered by business account and project or searched by employee and project name.

## Access and restrictions

The backend checks all permissions and project scopes. Hiding controls in the UI is not considered an access restriction by itself.

| Permission | User capability |
| --- | --- |
| `resource_allocation_edit` | Open resource allocations and work with projects from the user's manager scope. |
| `resource_allocation_edit_globally` | Work with every project. |
| `resource_allocation_period_manage` | Close and reopen months for every project. |

Regular managers can edit only projects in their manager hierarchy. Explicit project access does not make a project managed. Projects outside this scope require global edit permission.

Closed months remain protected by the backend even if a save request is sent manually.

### Current limitations

- The backend supports closing and reopening months, but the allocation page does not yet provide controls for it.
- Cell comment threads are not implemented.

## Implementation details

The frontend uses two child routes:

- `/management/resource-allocations/input?year={year}&projectId={projectId}`;
- `/management/resource-allocations/analytics`.

### API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/resource-allocations/input/{year}?projectId={id}` | Load annual input data, closed periods, and employee/month totals on other projects. |
| `PUT` | `/api/v1/resource-allocations/input/{year}/{projectId}` | Save changed employee/month cells as one revision. |
| `GET` | `/api/v1/resource-allocations/analytics/{year}` | Load annual read-only analytics. |
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

- `resource_allocation` stores current non-zero monthly values;
- `resource_allocation_revision` stores one Save operation;
- `resource_allocation_change` stores immutable before/after cell history;
- `resource_allocation_closed_period` stores globally closed months.

One Save creates one project/year revision. Saving and closing periods are transactional. PostgreSQL transaction advisory locks serialize writes for affected months, and per-cell revision checks reject stale changes.
