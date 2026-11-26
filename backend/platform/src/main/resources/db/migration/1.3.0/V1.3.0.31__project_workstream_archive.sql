ALTER TABLE proj.project_workstream
    ADD COLUMN archived_at timestamp with time zone NULL,
    ADD COLUMN archived_by integer NULL REFERENCES empl.employee (id);

COMMENT ON COLUMN proj.project_workstream.archived_at IS 'Archive time; null for an unarchived workstream';
COMMENT ON COLUMN proj.project_workstream.archived_by IS 'Employee who archived the workstream';

COMMENT ON COLUMN history.history.entity_type IS '
  [empl_manager] - Entity type,
  [working_days] - Working Days Calendar,
  [timesheet_record] - Timesheet Record
  [support_request_group] - Support Request Group
  [support_request] - Support Request
  [junior_registry] - Juniors registry
  [junior_registry_report] - Juniors registry
  [office] - Office
  [office_location] - Office Location
  [project_transfer_request] - Project transfer request
  [project_workstream] - Project workstream
  ';
