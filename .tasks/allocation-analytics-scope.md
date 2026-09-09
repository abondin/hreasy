# Allocation analytics employee scope

Agreed: allocation read permission plus existing project hierarchy scope. Include people with annual allocations if their current project is accessible OR at least one annual allocation is on an accessible project (explicit zero counts). Return all annual allocations for those people, including other accounts. Employees without annual allocations are omitted per user clarification; no empty-row code. No project-transfer history lookup. Same backend service for UI, Excel and external API; editing unchanged.

Implemented shared project access check independent of write permission; filtered employees, cells, project and workstream dictionaries; updated API documentation. Added unrun parameterized regression for project/department/account access, no scope, current staff on other projects, historical allocations, explicit zero, unrelated employees and empty employees.

Also reduced input-cell secondary allocation text from 10px to 9px. Percent suffix retained per final user feedback; primary values and tooltips unchanged.

Validation: static review and git diff --check; builds, tests and application starts remain reserved for user. Awaiting acceptance.
