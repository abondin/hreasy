CREATE schema if not exists sal;

CREATE SEQUENCE IF NOT EXISTS sal.salary_request_id_seq;
CREATE TABLE IF NOT EXISTS sal.salary_request (
---
--- Common fields
---
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('sal.salary_request_id_seq'),
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    -- 1 - salary increase
    -- 2 - bonus
    type integer NOT NULL default 1,
    budget_business_account integer NOT NULL REFERENCES ba.business_account (id),
    budget_expected_funding_until date NULL,
    assessment_id integer NULL REFERENCES assmnt.assessment (id),
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone  NULL,
    deleted_by integer NULL REFERENCES empl.employee (id),
---
--- Requested
---
    req_increase_amount numeric(10, 2) NOT NULL,
    req_planned_salary_amount numeric(10, 2) NULL,
    req_increase_start_period integer not null,
    req_reason varchar(1024) NOT NULL,
    req_comment text NULL,

---
--- Information from other tables. Populated when request creates
---
    info_empl_project integer null,
    info_empl_project_role varchar(1024) null,
    info_date_of_employment date null,
    info_empl_ba integer null,
    info_empl_position integer null,
    info_current_salary_amount numeric(10, 2) NULL,
    info_previous_salary_increase_text text null,
---
--- Implemented
---
    implemented_at timestamp with time zone NULL,
    implemented_by integer NULL REFERENCES empl.employee (id),
    -- 1 - Implemented
    -- 2 - Rejected
    impl_state integer NULL,
    impl_increase_amount numeric(10, 2) NULL,
    impl_salary_amount numeric(10, 2) NULL,
    impl_increase_start_period integer null,
    impl_increase_text text null,
    impl_new_position integer NULL REFERENCES dict.position (id),
    impl_reject_reason varchar(1024) NULL,
    impl_comment text NULL
);

COMMENT ON TABLE sal.salary_request IS 'Employee salary request';
---
--- Common fields
---
COMMENT ON COLUMN sal.salary_request.id IS 'Primary key';
COMMENT ON COLUMN sal.salary_request.employee_id IS 'Key attribute - link to employee';
COMMENT ON COLUMN sal.salary_request.budget_business_account IS 'Key attribute - link to business account';
COMMENT ON COLUMN sal.salary_request.budget_expected_funding_until IS 'Expected budgeting end date';
COMMENT ON COLUMN sal.salary_request.type IS '
1 - salary increase
2 - bonus
';
COMMENT ON COLUMN sal.salary_request.assessment_id IS 'Optional link to employee assessment';
COMMENT ON COLUMN sal.salary_request.created_at IS 'When request is reported';
COMMENT ON COLUMN sal.salary_request.created_by IS 'Created by';
COMMENT ON COLUMN sal.salary_request.deleted_at IS 'Deleted at';
COMMENT ON COLUMN sal.salary_request.deleted_by IS 'Deleted by';
---
--- Requested
---
COMMENT ON COLUMN sal.salary_request.req_increase_amount IS 'Requested increase sum';
COMMENT ON COLUMN sal.salary_request.req_planned_salary_amount IS 'New planned salary after increase';
COMMENT ON COLUMN sal.salary_request.req_increase_start_period IS 'Requested YYYYMM period. Month starts with 0. 202308 - September of 2023';
COMMENT ON COLUMN sal.salary_request.req_reason IS 'Reason to raise salary';
COMMENT ON COLUMN sal.salary_request.req_comment IS 'Optional request additional comment';

---
--- Info
---
COMMENT ON COLUMN sal.salary_request.info_empl_project IS 'Employee project';
COMMENT ON COLUMN sal.salary_request.info_empl_project_role IS 'Employee project role';
COMMENT ON COLUMN sal.salary_request.info_empl_ba IS 'Employee project business account';
COMMENT ON COLUMN sal.salary_request.info_empl_position IS 'Employee position in official documents';
COMMENT ON COLUMN sal.salary_request.info_current_salary_amount IS 'Current salary amount';
COMMENT ON COLUMN sal.salary_request.info_previous_salary_increase_text IS 'Official text from ERP system';

---
--- Implemented
---
COMMENT ON COLUMN sal.salary_request.implemented_at IS 'When the request was marked as implemented';
COMMENT ON COLUMN sal.salary_request.implemented_by IS 'Implemented by';
COMMENT ON COLUMN sal.salary_request.impl_increase_amount IS 'Actual implemented sum';
COMMENT ON COLUMN sal.salary_request.impl_increase_start_period IS 'Actual period to increase salary or implement bonus';
COMMENT ON COLUMN sal.salary_request.impl_increase_text IS 'Official text from ERP system';
COMMENT ON COLUMN sal.salary_request.impl_reject_reason IS 'Reject reason';
COMMENT ON COLUMN sal.salary_request.impl_comment IS 'Optional implementation additional comment';
COMMENT ON COLUMN sal.salary_request.impl_state IS '
1 - Implemented
2 - Rejected
';

---------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS sal.salary_request_approval_id_seq;
CREATE TABLE IF NOT EXISTS sal.salary_request_approval (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('sal.salary_request_approval_id_seq'),
    request_id integer NOT NULL REFERENCES sal.salary_request (id),
    -- 1 - Comment: If no decision made. Just basic comment
    -- 2 - Approved
    -- 3e - Declined
    state integer NOT NULL,
    comment text NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone  NULL,
    deleted_by integer NULL REFERENCES empl.employee (id)
 );

COMMENT ON TABLE sal.salary_request_approval IS 'Approval decision for salary request';
COMMENT ON COLUMN sal.salary_request_approval.id IS 'Primary key';
COMMENT ON COLUMN sal.salary_request_approval.request_id IS 'Key attribute - link to request';
COMMENT ON COLUMN sal.salary_request_approval.state IS 'Status of the request:
  1 - Comment: If no decision made. Just basic comment
  2 - Approved
  3 - Declined
';
COMMENT ON COLUMN sal.salary_request_approval.created_at IS 'Created at';
COMMENT ON COLUMN sal.salary_request_approval.created_by IS 'Created by';
COMMENT ON COLUMN sal.salary_request_approval.deleted_at IS 'Deleted at';
COMMENT ON COLUMN sal.salary_request_approval.deleted_by IS 'Deleted by';



INSERT INTO sec."role" ("role", description) VALUES
('salary_manager', 'View and update salary information in the system') ON CONFLICT DO NOTHING;
INSERT INTO sec."role" ("role", description) VALUES
('pm_finance', 'PM with ability to report salary requests') ON CONFLICT DO NOTHING;

INSERT INTO sec.perm (permission,description) VALUES
    ('report_salary_request','Report salary increase or bonus request for the employee (PM)') ON CONFLICT DO NOTHING;
INSERT INTO sec.perm (permission,description) VALUES
        ('approve_salary_request','Approve request (BA leads)') ON CONFLICT DO NOTHING;
INSERT INTO sec.perm (permission,description) VALUES
        ('admin_salary_request','View all requests in company. Change request states') ON CONFLICT DO NOTHING;

INSERT INTO sec.role_perm ("role","permission") VALUES
    ('global_admin','report_salary_request'),
    ('global_admin','approve_salary_request'),
    ('global_admin','admin_salary_request'),
    ('salary_manager','admin_salary_request'),
    ('salary_manager','approve_salary_request'),
    ('salary_manager','report_salary_request'),
    ('pm_finance','approve_salary_request'),
    ('pm_finance','report_salary_request')
     ON CONFLICT DO NOTHING;


--- Salary Request Period
CREATE TABLE IF NOT EXISTS sal.salary_request_closed_period (
    period integer PRIMARY KEY NOT NULL,
	closed_at timestamp with time zone NOT NULL,
	closed_by integer NOT NULL REFERENCES empl.employee (id),
	"comment" text NULL
);
COMMENT ON TABLE sal.salary_request_closed_period IS 'Salary request periods closed for editing. No requests can be reported';
COMMENT ON COLUMN sal.salary_request_closed_period.period IS 'Salary request period (PK)';
COMMENT ON COLUMN sal.salary_request_closed_period.comment IS 'Comment';
COMMENT ON COLUMN sal.salary_request_closed_period.closed_at IS 'Closed at';
COMMENT ON COLUMN sal.salary_request_closed_period.closed_by IS 'Closed by (link to employee)';


-- Salary Request Full Information View with displayName of createdBy and Merged approvals in column as a JSON array
create view sal.v_salary_request_full AS
select
                r.*,
                e.display_name as employee_display_name,
                p.name info_empl_project_name,
                pos.name as info_empl_position_name,
                ba.name as info_empl_ba_name,
                budget_ba.name as budget_business_account_name,
                asm.planned_date as assessment_planned_date,
                cr.display_name as created_by_display_name,
                im.display_name as implemented_by_display_name,
                newPos.name as impl_new_position_name,
                approvals.approval_data AS approvals
            from sal.salary_request r
                left join empl.employee e on r.employee_id = e.id
                left join proj.project p on e.current_project=p.id
                left join dict.position pos on e.position = pos.id
                left join ba.business_account ba on r.budget_business_account=ba.id
                left join ba.business_account budget_ba on r.budget_business_account=budget_ba.id
                left join assmnt.assessment asm on r.assessment_id=asm.id
                left join empl.employee cr on r.created_by = cr.id
                left join empl.employee im on r.implemented_by = im.id
                left join dict.position newPos on r.impl_new_position = newPos.id
                LEFT JOIN
    (
        SELECT
            request_id,
            json_agg(json_build_object(
                'id', id,
                'requestId', request_id,
                'state', state,
                'comment', comment,
                'createdAt', created_at,
                'createdBy', json_build_object(
                    'id', created_by,
                     'name', (SELECT display_name as name FROM empl.employee WHERE id = created_by)
                )
            )) AS approval_data
        FROM
            sal.salary_request_approval where deleted_at is null
        GROUP BY
            request_id
    ) approvals ON r.id = approvals.request_id;