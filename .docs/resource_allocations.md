# Resource Allocations

Resource allocations help managers plan how much of each employee's capacity is assigned to projects throughout a calendar year.

The feature has two sections:

- **Data entry** — plan and update allocations for one project;
- **Analytics** — review allocations across employees and projects without editing them.

## Enter allocations

1. Open **Managers → Resource allocations → Data entry**.
2. Select a year, one of the writable projects, and optionally one of its workstreams. The current year and the first project alphabetically are selected by default.
3. Find an employee in the grid or add one through the last row.
4. Enter allocation percentages for the required months.
5. Review the changes and click **Save**.

All 12 months are shown on one screen. The table keeps current project employees and every employee allocated to any workstream of that project, even while another workstream is selected. The selected year, project, and optional workstream are kept in the page URL, so the same view can be reopened or shared.

Project-level allocations and allocations for several workstreams of that project are independent and may coexist for the same employee and month.

The project selector excludes projects that started after the selected year or ended before it, unless the project already has allocations in that year. Projects with an actual end date inside the year remain selectable and show `Closed` with that date below the name.

The employee list contains:

- employees currently assigned to the selected project;
- employees who already have allocations on the project during the selected year;
- employees added to the current unsaved draft.

The add-employee selector shows the current project, project role, and dismissal date. Search matches the employee name, current project, and role, but not the dismissal date. Employee rows also show the project role.

### Read a cell

The large value is the employee's allocation on the selected project. Outside edit mode it is displayed with `%`; the editor accepts a number.

A small `+ N%` in the lower-right corner shows the employee's combined allocation on all other project/workstream dimensions for that month. It is hidden when that allocation is zero, keeping the selected value visually primary.

For example, `50%` with `+ 30%` means that the employee is allocated 50% to the selected project and another 30% to other projects.

Monthly cells in data entry and terminal analytics rows support comment threads. The comment action stays visible when a thread exists and appears on hover or keyboard focus for an empty thread. Anyone who can read the analytics cell can read all of its comments and add their own; authors alone can edit or delete their comments. Comments are plain text up to 4000 characters and remain attached to the cell when its allocation is cleared. Closed allocation periods do not lock comments.

### Editing rules

- Values must be whole numbers from `0` through `1000`.
- An empty value sends `null` and removes the allocation. `0` is an explicit allocation stored and displayed as `0%`.
- A month can be edited only while it is open and overlaps the employee's employment period.
- The employee's dismissal month remains editable. Existing values outside employment can be cleared, but no numeric value (including zero) can be entered there.
- Closed months have a lock in the column header and cannot be changed.
- Employees dismissed by the current date are marked in the employee column. If an active employee belongs to another current project, that project is shown next to the name.

The grid is read-only while loading or saving. Copy, paste, range selection, autofill, Tab, and Enter work directly in the grid. Changing the year, project, or page with unsaved changes requires confirmation.

### Concurrent changes

If another user changes the same cell first, the page loads the current server value and marks that cell in red. Changes in other cells remain in the draft. Review the marked cells, edit them again if necessary, and save.

## Analyze allocations

Open the **Analytics** tab to review recorded allocations, including explicit zeros, for the selected year. Analytics is read-only.

Two views are available:

- **Projects** — business account → project → workstream → employees;
- **Employees** — employee → project → workstream.

Projects remain the parent level. A project without workstream allocations skips the intermediate `Without workstream` row in both hierarchy modes. Project rows show a single workstream name inline; when several direction rows are available, they show their grammatically declined count instead. Projects can be expanded even when only one workstream exists. Workstream levels are collapsed by default. Project-level values are shown as `Without workstream` only when they coexist with workstream allocations.

Users with allocation admin permission open the period-lock dialog from the analytics toolbar, select the closed months for the current year, and save the whole selection at once. The current month is highlighted. Canceling the dialog does not change period states.

Group rows show monthly totals. A separate control hides group totals without hiding terminal values. Values can be displayed as percentages or person-months, and the year-total column follows the employee/group column. Data can be filtered by business account and project or searched by employee name, project role, project name, or workstream.

The information action next to an employee opens the existing employee profile card in a dialog without changing the URL, filters, expanded groups, or an unsaved allocation draft.

Expandable group rows use a pale gray background and highlighted totals; terminal data rows use normal font weight. Both follow the same hierarchy-depth indentation. Horizontal separators and the vertical dividers beside the employee and annual-total columns remain visible with group totals enabled or hidden. On desktop, employee details and empty comment actions appear on hover or keyboard focus; existing-comment actions and touch-device actions remain visible.

## Access and restrictions

The backend checks all permissions and project scopes. Hiding controls in the UI is not considered an access restriction by itself.

| Permission | User capability |
| --- | --- |
| `resource_allocation_read` | View employee-scoped allocation analytics and read/add comments on visible monthly cells. |
| `resource_allocation_write` | Edit allocations for projects available through the acting user's effective hierarchy access. |
| `resource_allocation_admin` | Close and reopen months. |

Authentication merges manager responsibilities with explicit project, department, and business-account access. Allocation authorization uses the same shared `ProjectHierarchyAccessor` as assessments and neighboring workflows. A writable project may receive allocations for any employee. Analytics includes employees whose current project is accessible OR who have at least one recorded allocation (including zero) on an accessible project in the selected year. All annual allocations of those employees are visible, including other projects and business accounts. Employees without recorded allocations in the selected year are omitted. Project-transfer history alone does not qualify an employee. Excel and the external API use this same scope. Projects outside the write scope are not available in the data-entry selector.

Closed months remain protected by the backend even if a save request is sent manually.

### Current limitations

- A workstream removed from project editing is no longer available for allocation input. Existing cells remain visible in analytics, but cannot currently be cleared through the input page.
- Clearing the last annual cell of an otherwise ineligible project can leave that project selected while the refreshed input endpoint rejects it; the UI does not yet recover by selecting another eligible project.

### Project and workstream lifecycle

Project administrators edit the optional project `externalId` and its workstreams on the project form. A workstream contains `id`, optional `externalId`, `displayName`, and optional `description`; removing it from the form sets `deleted_at`/`deleted_by` instead of deleting the row. New overtime and allocation entries accept active workstreams belonging to the selected project only. The project dictionary and external project API return active workstreams, while allocation analytics and detailed overtime reports resolve referenced soft-deleted workstreams for historical display.

## Implementation details

The frontend uses two child routes:

- `/management/resource-allocations/input?year={year}&projectId={projectId}&workstreamId={workstreamId}`;
- `/management/resource-allocations/analytics`.

### API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/resource-allocations/input/{year}?projectId={id}&workstreamId={id}` | Load annual input data for an optional workstream, searchable employee details including email, closed periods, and employee/month totals on all other allocation dimensions. |
| `PUT` | `/api/v1/resource-allocations/input/{year}/{projectId}?workstreamId={id}` | Save changed employee/month cells for an optional workstream as one revision. |
| `GET` | `/api/v1/resource-allocations/analytics/{year}` | Load annual read-only analytics with searchable employee details including email. |
| `GET` | `/api/v1/resource-allocations/closed-periods/{year}` | Load closed months for the analytics toolbar. |
| `PUT` | `/api/v1/resource-allocations/closed-periods/{year}` | Replace the closed-month selection for the year. |
| `GET` | `/api/v1/resource-allocations/comments/summary/{year}` | Load visible annual comment counts and metadata for comment-only rows. |
| `GET` | `/api/v1/resource-allocations/comments?period={period}&employeeId={id}&projectId={id}&workstreamId={id}` | Load one visible cell thread. |
| `POST` | `/api/v1/resource-allocations/comments` | Add a comment to a visible monthly cell. |
| `PUT` | `/api/v1/resource-allocations/comments/{commentId}` | Edit the acting user's comment. |
| `DELETE` | `/api/v1/resource-allocations/comments/{commentId}` | Delete the acting user's comment. |

Periods use the repository's zero-based `YYYYMM` convention: `202600` is January 2026 and `202611` is December 2026.
The annual period update body is `{ "closedPeriods": [202600, 202601] }`; omitted months are reopened.

The annual input response separates the selected project/workstream dimension from combined allocations on all other dimensions. `sameProject` marks employees who must remain visible because another dimension of the selected project contains data:

```json
{
  "allocations": [
    { "period": 202600, "employeeId": 42, "percent": 50, "revisionId": 17 }
  ],
  "otherAllocations": [
    { "period": 202600, "employeeId": 42, "percent": 30, "sameProject": true }
  ]
}
```

The save request contains only changed cells. `expectedRevisionId` is `null` for a new cell, while `percent: null` removes an existing allocation; `percent: 0` stores an explicit zero:

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

- `resource_allocation` stores current monthly values, including explicit zeros, for a project and optional workstream;
- `resource_allocation_revision` stores one Save operation for a project and optional workstream;
- `resource_allocation_change` stores immutable before/after cell history;
- `resource_allocation_closed_period` stores the currently closed months and who closed each one;
- `resource_allocation_period_history` stores immutable close and reopen events with their actor and timestamp.
- `resource_allocation_comment` stores monthly cell comments independently of current allocation values.

One Save creates one project/year revision. Clearing a cell (`percent: null`) physically removes it from the current-state table. The deletion remains auditable as an immutable change with the previous percentage and `new_percent = null`; unlike ordinary CRUD entities, a second soft-deleted copy would duplicate the existing revision history.

Period selection saves compare the requested and current sets. Existing closed months are left untouched, so resubmitting a checked month neither rewrites its original closure metadata nor creates a duplicate history event. Only actual open-to-closed and closed-to-open transitions are appended to history.

Saving and closing periods are transactional. PostgreSQL transaction advisory locks serialize writes for affected months, and per-cell revision checks reject stale changes.

Comment identity consists of period, employee, project, and optional workstream. Comment-only rows remain visible to users with the corresponding allocation scope. Comments are not included in Excel exports or the external API.

### Analytics Excel export

The analytics toolbar exports a flat XLSX using the selected year and display unit only. Search, business-account/project filters, grouping, collapsed rows and group totals do not restrict the export. All recorded annual allocations available to the acting user are included.

`GET /api/v1/resource-allocations/analytics/{year}/export?unit=personMonths` requires the same `resource_allocation_read` permission as analytics. Supported units are `personMonths` (default) and `percent`.

Columns: employee, email, business account, project, workstream, current project role, annual total and January through December. Each employee/project/workstream combination has a separate row. Project-level allocations are separate from workstream allocations; there are no subtotal rows. Organizational metadata describes the current configuration.

Values are numeric fractions: 100 stored percent exports as 1 person-month or 100% with Excel percentage formatting. Annual totals are sums of monthly values (12 or 1200% for a full year). Missing months remain blank; explicitly recorded zero remains numeric zero. The workbook records the export timestamp and acting username and includes an expanding Excel table with frozen headers, ready for filtering and user-defined pivots.

The JXLS template is `backend/platform/src/main/resources/jxls/resource_allocations_template.xlsx`, following existing overtime and salary exports. The exporter prepares data and renders the template directly, without POI post-processing. Numeric formats and frozen headers are defined in the template; conditional formatting selects the percentage display from the unit label. The export timestamp is passed as a local date-time, following the other Excel exporters. Analytics links preserve year and unit, for example `/management/resource-allocations/analytics?year=2026&unit=personMonths`.
