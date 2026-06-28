CREATE SEQUENCE IF NOT EXISTS empl.project_transfer_request_id_seq;
CREATE TABLE IF NOT EXISTS empl.project_transfer_request (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.project_transfer_request_id_seq'),
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    from_project_id integer NOT NULL REFERENCES proj.project (id),
    to_project_id integer NOT NULL REFERENCES proj.project (id),
    requested_project_role varchar(1024) NULL,
    approver_employee_id integer NOT NULL REFERENCES empl.employee (id),
    state integer NOT NULL,
    decision_comment text NULL,
    applied_employee_history_id integer NULL REFERENCES empl.employee_history (id),
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    updated_at timestamp with time zone NULL,
    updated_by integer NULL REFERENCES empl.employee (id)
);

CREATE UNIQUE INDEX project_transfer_request_pending_employee_unique
ON empl.project_transfer_request (employee_id)
WHERE state = 1;

CREATE INDEX project_transfer_request_approver_idx
ON empl.project_transfer_request (approver_employee_id, state, created_at DESC);

CREATE INDEX project_transfer_request_created_by_idx
ON empl.project_transfer_request (created_by, state, created_at DESC);

CREATE INDEX project_transfer_request_employee_idx
ON empl.project_transfer_request (employee_id, created_at DESC);

CREATE INDEX project_transfer_request_pending_created_at_idx
ON empl.project_transfer_request (state, created_at);

COMMENT ON TABLE empl.project_transfer_request IS 'Request to transfer employee from current project to another project';
COMMENT ON COLUMN empl.project_transfer_request.id IS 'Primary key';
COMMENT ON COLUMN empl.project_transfer_request.employee_id IS 'Employee to transfer';
COMMENT ON COLUMN empl.project_transfer_request.from_project_id IS 'Project employee is transferred from';
COMMENT ON COLUMN empl.project_transfer_request.to_project_id IS 'Project employee is transferred to';
COMMENT ON COLUMN empl.project_transfer_request.requested_project_role IS 'Requested employee role on target project';
COMMENT ON COLUMN empl.project_transfer_request.approver_employee_id IS 'Employee selected to approve project transfer';
COMMENT ON COLUMN empl.project_transfer_request.state IS 'Project transfer request state:
  1 - Pending
  2 - Approved
  3 - Rejected
  4 - Canceled
  5 - Expired
';
COMMENT ON COLUMN empl.project_transfer_request.decision_comment IS 'Approver decision comment';
COMMENT ON COLUMN empl.project_transfer_request.applied_employee_history_id IS 'Employee history row created when approved transfer is applied';
COMMENT ON COLUMN empl.project_transfer_request.created_at IS 'Created at';
COMMENT ON COLUMN empl.project_transfer_request.created_by IS 'Created by (employee)';
COMMENT ON COLUMN empl.project_transfer_request.updated_at IS 'Updated at';
COMMENT ON COLUMN empl.project_transfer_request.updated_by IS 'Updated by (employee)';

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
  ';
