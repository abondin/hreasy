ALTER TABLE proj.project ADD COLUMN info text;
ALTER TABLE proj.project_history ADD COLUMN info text;

COMMENT ON COLUMN proj.project.info IS 'Project information in markdown format';
COMMENT ON COLUMN proj.project_history.info IS 'Project information in markdown format';
