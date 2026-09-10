# Allocation cell comments

## Goal

Add comment threads to cells in resource-allocation analytics.

## Agreed access rules

- Anyone allowed to read an analytics cell can read all comments attached to that cell.
- The same readers can add comments.
- A user can edit and delete only comments they created.
- Backend permissions and allocation visibility are authoritative; the UI must not broaden access.

## Scope to design

- Persist comments with the allocation cell identity: year/period, employee, project, and optional workstream.
- Define API operations for listing, adding, editing, and deleting comments.
- Add an analytics-cell affordance and comment thread UI without enabling allocation edits.
- Preserve author and creation/update timestamps.
- Decide how comments behave when an allocation value is cleared or its workstream is soft-deleted.

## Status

Requirements captured; analysis and implementation have not started.
