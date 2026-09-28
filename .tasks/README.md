# Active Tasks

This directory keeps concise, source-controlled context for large, multi-stage tasks such as cross-module features, architectural changes, or substantial migrations.

Each large task has one Markdown file, updated as requirements, decisions, implementation, and verification evolve. After the user confirms completion, applicable durable documentation and `changelogs/CHANGELOG.md` are updated and the file is removed.

Small fixes and local enhancements do not require task files. Skip them by default; if temporary notes are useful, remove them after implementation and validation without waiting for separate user confirmation. Having analysis, implementation, and verification steps does not by itself require a persistent file.
