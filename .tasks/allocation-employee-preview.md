# Employee preview from allocation tables

Scope: open the existing employee card from both allocation tabs without disrupting editing, group expansion, filters, drafts, or navigation. Keep builds and test execution with the user.

Implemented:
- Separate small information button before employee names in input rows, analytics employee groups, and project-mode employee leaves (including synthetic terminal groups).
- Profile buttons stop mouse, pointer, touch, double-click and keyboard propagation. Expansion retains its separate button; no nested buttons.
- A shared modal in ResourceAllocationsView reuses useEmployeeProfile and EmployeeDetailsPanel, including existing permissions. Opening/closing does not change the URL or reload allocation data. Keyed employee components isolate pending profile requests; route changes/deactivation close the modal.
- Loading and error states are displayed. The modal title is Employee card; employee name appears once inside the reused card, following user screenshot feedback.
- Initial first-column width scales to 36% of viewport, bounded at 800px and at the existing minimum (600 input / 520 analytics). Manual grid resizing remains enabled.
- Information icons keep their layout space but appear only on row hover or keyboard focus for fine-pointer devices; touch devices keep the action visible.
- Analytics has a separate Group totals checkbox (enabled by default), followed by a visible Percentages / Person-months toggle. Person-months is the default; the dropdown was removed following user feedback. Data entry and API snapshots always remain percentages; 100 percent in one month displays as 1 person-month. Display rows used for clipboard copying and group summaries use the selected unit, while hierarchy configuration remains based on the unchanged percentage rows.
- Person-month cells display numbers only; percentage cells retain the percent sign. Closed months show the same lock icon in analytics headers as in data entry.
- A Year total column follows the first column, summing monthly allocations in the selected unit. Example: 100 percent in each of 12 months is 1200 percent or 12 person-months. Annual totals are computed before display rounding. Hiding group totals hides annual totals too, except terminal rows and explicit zeros.
- Updated unit regression coverage for 14 columns, annual sums, unit conversion, blank versus zero, restoring percentage mode, no additional API loads and stable grouping configuration. These tests were not run.
- Regression assertions cover hiding/restoring totals in both modes, retaining terminal zero values, unchanged source/grouping references and no extra API load.
- Existing allocation unit tests extended to cover profile opening with a pending draft, unchanged route/save calls, analytics employee actions, and separation from group expansion.

Validation: git diff --check passed. Builds, type-check and tests were not run. User has opened the modal and supplied screenshots; no automated browser verification was run for this feature. Awaiting user confirmation.
