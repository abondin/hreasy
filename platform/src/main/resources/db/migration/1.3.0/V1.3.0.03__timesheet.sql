CREATE schema if not exists ts;
CREATE SEQUENCE IF NOT EXISTS ts.timesheet_record_id_seq;
CREATE TABLE IF NOT EXISTS ts.timesheet_record (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('ts.timesheet_record_id_seq'),
    employee integer NOT NULL REFERENCES empl.employee (id),
    business_account integer NOT NULL REFERENCES ba.business_account (id),
    project integer NULL REFERENCES proj.project (id),
    date date not null,
    hours_planned smallint null,
    hours_spent smallint null,
    billable boolean not null default true,
    description varchar(1024) NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    constraint timesheet_record_hours_not_null check (hours_planned is not null or hours_spent is not null),
    constraint timesheet_record_uk UNIQUE NULLS NOT DISTINCT ("employee","business_account","project","date")
);

COMMENT ON TABLE ts.timesheet_record IS 'Daily spent hours';
COMMENT ON COLUMN ts.timesheet_record.id IS 'Primary key';
COMMENT ON COLUMN ts.timesheet_record.employee IS 'Key attribute - link to employee';
COMMENT ON COLUMN ts.timesheet_record.business_account IS 'Key attribute - link to business account';
COMMENT ON COLUMN ts.timesheet_record.project IS 'Link to specific project';
COMMENT ON COLUMN ts.timesheet_record.date IS 'Reporting date';
COMMENT ON COLUMN ts.timesheet_record.hours_planned IS 'Amount of planned working hours';
COMMENT ON COLUMN ts.timesheet_record.hours_spent IS 'Amount of actually spent working hours';
COMMENT ON COLUMN ts.timesheet_record.billable IS 'If spent hours billable';
COMMENT ON COLUMN ts.timesheet_record.description IS 'General description';
COMMENT ON COLUMN ts.timesheet_record.created_at IS 'Created at';
COMMENT ON COLUMN ts.timesheet_record.created_by IS 'Created by (link to employee)';

INSERT INTO sec.perm (permission,description) VALUES
    ('report_timesheet','Report daily timesheet');
INSERT INTO sec.perm (permission,description) VALUES
        ('view_timesheet','View daily timesheet for given employee');
INSERT INTO sec.perm (permission,description) VALUES
        ('view_timesheet_summary','View daily timesheet summary');

INSERT INTO sec.role_perm ("role","permission") VALUES
    ('global_admin','view_timesheet_summary'),
     ('pm','view_timesheet_summary'),
     ('hr','view_timesheet_summary'),
    ('global_admin','view_timesheet'),
     ('pm','view_timesheet'),
    ('global_admin','report_timesheet'),
     ('pm','report_timesheet');

COMMENT ON COLUMN history.history.entity_type IS '[empl_manager] - Entity type, [working_days] - Working Days Calendar, [timesheet_record] - Timesheet Record';
