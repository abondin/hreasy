ALTER TABLE proj.project ADD COLUMN workstream_required boolean NOT NULL DEFAULT false;
ALTER TABLE proj.project_history ADD COLUMN workstream_required boolean NOT NULL DEFAULT false;

COMMENT ON COLUMN proj.project.workstream_required IS 'Require a workstream when setting resource allocations';
COMMENT ON COLUMN proj.project_history.workstream_required IS 'Workstream requirement at the time of the project change';
