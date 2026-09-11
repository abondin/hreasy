---
name: task-lifecycle
description: Maintain tracked, up-to-date task context for HREasy work that spans analysis, implementation, review, or verification iterations.
---

# Task Lifecycle

Use one `.tasks/<task-name>.md` file per active task.

At the start, create the file or read and refresh the existing one. Keep only information that helps continue the task:

- goal and agreed scope;
- product and technical decisions;
- current implementation checklist;
- validation already run and its result;
- unresolved questions or blockers.

Update the file after each meaningful analysis, implementation, review, or verification iteration. Rewrite stale sections instead of keeping a chronological diary. Never overwrite another active task file.

Keep the task file until the user explicitly confirms the task is finished. Then update durable documentation affected by the change, update `changelogs/CHANGELOG.md`, and delete the task file. Passing tests alone is not user confirmation.
