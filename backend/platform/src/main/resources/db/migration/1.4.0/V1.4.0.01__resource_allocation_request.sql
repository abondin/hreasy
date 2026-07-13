CREATE SEQUENCE IF NOT EXISTS empl.resource_allocation_request_id_seq;
CREATE TABLE IF NOT EXISTS empl.resource_allocation_request (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.resource_allocation_request_id_seq'),
    request_type integer NOT NULL,
    req_project_id integer NOT NULL REFERENCES proj.project (id),
    req_employee_id integer NULL REFERENCES empl.employee (id),
    req_project_role varchar(1024) NULL,
    req_employment_rate numeric(3, 2) NOT NULL,
    req_start_date date NOT NULL,
    req_end_date date NULL,
    state integer NOT NULL,
    merged_into_request_id integer NULL REFERENCES empl.resource_allocation_request (id),
    impl_employee_id integer NULL REFERENCES empl.employee (id),
    impl_from_project_id integer NULL REFERENCES proj.project (id),
    impl_to_project_id integer NULL REFERENCES proj.project (id),
    impl_project_role varchar(1024) NULL,
    impl_employment_rate numeric(3, 2) NULL,
    impl_start_date date NULL,
    impl_end_date date NULL,
    impl_release_confirmed_by integer NULL REFERENCES empl.employee (id),
    impl_acceptance_confirmed_by integer NULL REFERENCES empl.employee (id),
    implemented_at timestamp with time zone NULL,
    implemented_by integer NULL REFERENCES empl.employee (id),
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    updated_at timestamp with time zone NULL,
    updated_by integer NULL REFERENCES empl.employee (id),
    CONSTRAINT resource_allocation_request_req_employment_rate_check
        CHECK (req_employment_rate BETWEEN 0 AND 1),
    CONSTRAINT resource_allocation_request_impl_employment_rate_check
        CHECK (impl_employment_rate IS NULL OR impl_employment_rate BETWEEN 0 AND 1),
    CONSTRAINT resource_allocation_request_req_dates_check
        CHECK (req_end_date IS NULL OR req_end_date >= req_start_date),
    CONSTRAINT resource_allocation_request_impl_dates_check
        CHECK (impl_end_date IS NULL OR impl_start_date IS NOT NULL AND impl_end_date >= impl_start_date),
    CONSTRAINT resource_allocation_request_not_merged_into_self_check
        CHECK (merged_into_request_id IS NULL OR merged_into_request_id <> id)
);

CREATE UNIQUE INDEX resource_allocation_request_merged_into_unique
ON empl.resource_allocation_request (merged_into_request_id)
WHERE merged_into_request_id IS NOT NULL;

CREATE INDEX resource_allocation_request_state_idx
ON empl.resource_allocation_request (state, req_start_date);

CREATE INDEX resource_allocation_request_project_idx
ON empl.resource_allocation_request (req_project_id, state, req_start_date);

CREATE INDEX resource_allocation_request_employee_idx
ON empl.resource_allocation_request (req_employee_id, state, req_start_date)
WHERE req_employee_id IS NOT NULL;

COMMENT ON TABLE empl.resource_allocation_request IS 'Project resource allocation planning request';
COMMENT ON COLUMN empl.resource_allocation_request.id IS 'Primary key';
COMMENT ON COLUMN empl.resource_allocation_request.request_type IS 'Resource allocation request type:
  1 - Demand
  2 - Availability
';
COMMENT ON COLUMN empl.resource_allocation_request.req_project_id IS 'Project reporting demand or employee availability';
COMMENT ON COLUMN empl.resource_allocation_request.req_employee_id IS 'Employee offered by an availability request';
COMMENT ON COLUMN empl.resource_allocation_request.req_project_role IS 'Requested or offered project role';
COMMENT ON COLUMN empl.resource_allocation_request.req_employment_rate IS 'Requested or offered employment rate from 0 to 1; 1 means full time';
COMMENT ON COLUMN empl.resource_allocation_request.req_start_date IS 'Requested allocation start date or employee availability date';
COMMENT ON COLUMN empl.resource_allocation_request.req_end_date IS 'Requested allocation end date or employee availability end date';
COMMENT ON COLUMN empl.resource_allocation_request.state IS 'Resource allocation request state:
  1 - New
  2 - Paused
  3 - Implemented
  4 - Withdrawn
  5 - Discarded
  6 - Merged
';
COMMENT ON COLUMN empl.resource_allocation_request.merged_into_request_id IS 'Implemented request containing the result for this merged request';
COMMENT ON COLUMN empl.resource_allocation_request.impl_employee_id IS 'Employee assigned as the implemented result';
COMMENT ON COLUMN empl.resource_allocation_request.impl_from_project_id IS 'Project releasing the employee in the implemented result';
COMMENT ON COLUMN empl.resource_allocation_request.impl_to_project_id IS 'Project accepting the employee in the implemented result';
COMMENT ON COLUMN empl.resource_allocation_request.impl_project_role IS 'Employee project role in the implemented result';
COMMENT ON COLUMN empl.resource_allocation_request.impl_employment_rate IS 'Implemented employment rate from 0 to 1; 1 means full time';
COMMENT ON COLUMN empl.resource_allocation_request.impl_start_date IS 'Implemented allocation start date';
COMMENT ON COLUMN empl.resource_allocation_request.impl_end_date IS 'Implemented allocation end date';
COMMENT ON COLUMN empl.resource_allocation_request.impl_release_confirmed_by IS 'Manager who confirmed releasing the employee';
COMMENT ON COLUMN empl.resource_allocation_request.impl_acceptance_confirmed_by IS 'Manager who confirmed accepting the employee';
COMMENT ON COLUMN empl.resource_allocation_request.implemented_at IS 'When the request implementation was recorded';
COMMENT ON COLUMN empl.resource_allocation_request.implemented_by IS 'Employee who recorded the request implementation';
COMMENT ON COLUMN empl.resource_allocation_request.created_at IS 'Created at';
COMMENT ON COLUMN empl.resource_allocation_request.created_by IS 'Created by (employee)';
COMMENT ON COLUMN empl.resource_allocation_request.updated_at IS 'Updated at';
COMMENT ON COLUMN empl.resource_allocation_request.updated_by IS 'Updated by (employee)';

CREATE SEQUENCE IF NOT EXISTS empl.resource_allocation_request_comment_id_seq;
CREATE TABLE IF NOT EXISTS empl.resource_allocation_request_comment (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.resource_allocation_request_comment_id_seq'),
    request_id integer NOT NULL REFERENCES empl.resource_allocation_request (id),
    comment text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone NULL,
    deleted_by integer NULL REFERENCES empl.employee (id)
);

CREATE INDEX resource_allocation_request_comment_request_idx
ON empl.resource_allocation_request_comment (request_id, created_at)
WHERE deleted_at IS NULL;

COMMENT ON TABLE empl.resource_allocation_request_comment IS 'Comment in a resource allocation request discussion';
COMMENT ON COLUMN empl.resource_allocation_request_comment.id IS 'Primary key';
COMMENT ON COLUMN empl.resource_allocation_request_comment.request_id IS 'Resource allocation request';
COMMENT ON COLUMN empl.resource_allocation_request_comment.comment IS 'Comment text';
COMMENT ON COLUMN empl.resource_allocation_request_comment.created_at IS 'Created at';
COMMENT ON COLUMN empl.resource_allocation_request_comment.created_by IS 'Created by (employee)';
COMMENT ON COLUMN empl.resource_allocation_request_comment.deleted_at IS 'Deleted at';
COMMENT ON COLUMN empl.resource_allocation_request_comment.deleted_by IS 'Deleted by (employee)';

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
  [resource_allocation_request] - Resource allocation request
  [resource_allocation_request_comment] - Resource allocation request comment
  ';
