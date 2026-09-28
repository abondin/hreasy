---
name: task-lifecycle
description: Maintain persistent context for large, multi-stage HREasy tasks such as cross-module features, architectural changes, or substantial migrations. Small fixes and local enhancements do not require task files.
---

# Task Lifecycle

Use one persistent `.tasks/<task-name>.md` file per large task that needs durable working context. Ordinary analysis, implementation, and verification steps alone do not make a task large.

For small fixes and local enhancements, skip the task file by default. Optional temporary notes must be deleted after implementation and validation, without waiting for separate user confirmation. Adding an employee table to an existing project page using existing components is a small task.

At the start, create the file or read and refresh the existing one. Keep only information that helps continue the task:

- goal and agreed scope;
- product and technical decisions;
- current implementation checklist;
- validation already run and its result;
- unresolved questions or blockers.

Update the file after each meaningful analysis, implementation, review, or verification iteration. Rewrite stale sections instead of keeping a chronological diary. Never overwrite another active task file.

For large tasks, keep the persistent task file until the user explicitly confirms completion. Then update durable documentation affected by the change, update `changelogs/CHANGELOG.md`, and delete the task file. Passing tests alone is not user confirmation for large tasks.
