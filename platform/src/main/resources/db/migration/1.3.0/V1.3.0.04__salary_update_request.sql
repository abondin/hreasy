CREATE schema if not exists sal;

CREATE SEQUENCE IF NOT EXISTS sal.salary_request_id_seq;
CREATE TABLE IF NOT EXISTS sal.salary_request (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('sal.salary_request_id_seq'),
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    -- 1 - salary increase
    -- 2 - bonus
    type integer NOT NULL default 1,
    budget_business_account integer NOT NULL REFERENCES ba.business_account (id),
    budget_expected_funding_until date NULL,
     -- 0 - created: when PM creates a request
     -- 1 - in progress: when Salary Manager move request to in progress
     -- 2 - implemented: when Salary Manager or Finance mark after Finance implemented in real
    stat smallint NOT NULL,
    salary_increase numeric(10, 2) NOT NULL,
    increase_start_period integer not null,
    assessment_id integer NULL REFERENCES assmnt.assessment (id),
    reason varchar(1024) NOT NULL,
    comment text NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone  NULL,
    deleted_by integer NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE sal.salary_request IS 'Employee salary request';
COMMENT ON COLUMN sal.salary_request.id IS 'Primary key';
COMMENT ON COLUMN sal.salary_request.employee_id IS 'Key attribute - link to employee';
COMMENT ON COLUMN sal.salary_request.budget_business_account IS 'Key attribute - link to business account';
COMMENT ON COLUMN sal.salary_request.budget_expected_funding_until IS 'Expected budgeting end date';
COMMENT ON COLUMN sal.salary_request.stat IS 'Status of the request:
  0 - created: when PM creates a request
  1 - in progress: when Salary Manager move request to in progress
  2 - implemented: when Salary Manager or Finance mark after Finance implemented in real
';
COMMENT ON COLUMN sal.salary_request.salary_increase IS 'Increase sum';
COMMENT ON COLUMN sal.salary_request.increase_start_period IS 'YYYYMM period. Month starts with 0. 202308 - September of 2023';
COMMENT ON COLUMN sal.salary_request.assessment_id IS 'Optional link to employee assessment';
COMMENT ON COLUMN sal.salary_request.reason IS 'Reason';
COMMENT ON COLUMN sal.salary_request.comment IS 'Optional additional comment';
COMMENT ON COLUMN sal.salary_request.created_at IS 'Created at';
COMMENT ON COLUMN sal.salary_request.created_by IS 'Created by';
COMMENT ON COLUMN sal.salary_request.deleted_at IS 'Deleted at';
COMMENT ON COLUMN sal.salary_request.deleted_by IS 'Deleted by';

---------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS sal.salary_request_approval_id_seq;
CREATE TABLE IF NOT EXISTS sal.salary_request_approval (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('sal.salary_request_approval_id_seq'),
    request_id integer NOT NULL REFERENCES sal.salary_request (id),
    -- 0 - Comment: If no decision made. Just basic comment
    -- 1 - Approved
    -- 2 - Declined
    stat integer NOT NULL,
    comment text NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone  NULL,
    deleted_by integer NULL REFERENCES empl.employee (id)
 );

COMMENT ON TABLE sal.salary_request_approval IS 'Approval decision for salary request';
COMMENT ON COLUMN sal.salary_request_approval.id IS 'Primary key';
COMMENT ON COLUMN sal.salary_request_approval.request_id IS 'Key attribute - link to request';
COMMENT ON COLUMN sal.salary_request_approval.stat IS 'Status of the request:
  0 - Comment: If no decision made. Just basic comment
  1 - Approved
  2 - Declined
';
COMMENT ON COLUMN sal.salary_request_approval.created_at IS 'Created at';
COMMENT ON COLUMN sal.salary_request_approval.created_by IS 'Created by';
COMMENT ON COLUMN sal.salary_request_approval.deleted_at IS 'Deleted at';
COMMENT ON COLUMN sal.salary_request_approval.deleted_by IS 'Deleted by';



INSERT INTO sec."role" ("role", description) VALUES
('salary_manager', 'View and update salary information in the system');

INSERT INTO sec.perm (permission,description) VALUES
    ('report_salary_request','Report salary increase or bonus request for the employee (PM)');
INSERT INTO sec.perm (permission,description) VALUES
        ('update_salary_request','Update salary increase or bonus request status (Salary manager, Finance)');
INSERT INTO sec.perm (permission,description) VALUES
        ('approve_salary_request_globally','View and approve salary increase or bonus request of any employee (Global admin, Finance)');
INSERT INTO sec.perm (permission,description) VALUES
        ('approve_salary_request','View and approve salary increase or bonus request for employee from user`s department or budgeting from user`s business account');

INSERT INTO sec.role_perm ("role","permission") VALUES
    ('global_admin','report_salary_request'),
    ('global_admin','update_salary_request'),
    ('global_admin','approve_salary_request_globally'),
    ('finance','report_salary_request'),
    ('finance','update_salary_request'),
    ('finance','approve_salary_request_globally'),
    ('salary_manager','update_salary_request'),
    ('salary_manager','approve_salary_request_globally'),
    ('pm','report_salary_request'),
    ('pm','approve_salary_request');

COMMENT ON COLUMN history.history.entity_type IS '
  [empl_manager] - Employee Manager,
  [working_days] - Working Days Calendar,
  [timesheet_record] - Timesheet Record
  [salary_request] - Salary Request,
  [salary_request_approval] - Salary Request
';
