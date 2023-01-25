CREATE schema if not exists ts;
CREATE SEQUENCE IF NOT EXISTS ts.timesheet_id_seq;
CREATE TABLE IF NOT EXISTS ts.timesheet (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('ts.timesheet_id_seq'),
    employee integer NOT NULL REFERENCES empl.employee (id),
    business_account integer NOT NULL REFERENCES ba.business_account (id),
    project integer NULL REFERENCES proj.project (id),
    date date not null,
    hours smallint not null,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone,
    deleted_by integer NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE ts.timesheet IS 'Daily spent hours';
COMMENT ON COLUMN ts.timesheet.id IS 'Primary key';
COMMENT ON COLUMN ts.timesheet.employee IS 'Key attribute - link to employee';
COMMENT ON COLUMN ts.timesheet.business_account IS 'Key attribute - link to business account';
COMMENT ON COLUMN ts.timesheet.project IS 'Link to specific project';
COMMENT ON COLUMN ts.timesheet.date IS 'Reporting date';
COMMENT ON COLUMN ts.timesheet.hours IS 'Amount of working hours';
COMMENT ON COLUMN ts.timesheet.created_at IS 'Created at';
COMMENT ON COLUMN ts.timesheet.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ts.timesheet.deleted_at IS 'Deleted/Canceled at';
COMMENT ON COLUMN ts.timesheet.deleted_by IS 'Deleted/Canceled by (link to employee)';

INSERT INTO sec.perm (permission,description) VALUES
    ('report_timesheet','Report daily timesheet');

INSERT INTO sec.role_perm ("role","permission") VALUES
    ('global_admin','report_timesheet'),
     ('pm','report_timesheet');