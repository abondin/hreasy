CREATE SCHEMA IF NOT EXISTS ovt;

---------- Overtime report
CREATE SEQUENCE IF NOT EXISTS ovt.OVERTIME_REPORT_ID_SEQ;
CREATE TABLE IF NOT EXISTS ovt.overtime_report (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ovt.OVERTIME_REPORT_ID_SEQ'),
	employee integer NOT NULL REFERENCES empl.employee (id),
	"period" integer NOT NULL
);
COMMENT ON TABLE ovt.overtime_report IS 'Employee Overtime monthly report';
COMMENT ON COLUMN ovt.overtime_report.employee IS 'Link to employee to assessment';
COMMENT ON COLUMN ovt.overtime_report.period IS 'Overtime period in YYYYMM format (202111). MM - starts with 0';

CREATE SEQUENCE IF NOT EXISTS ovt.OVERTIME_ITEM_ID_SEQ;
CREATE TABLE IF NOT EXISTS ovt.overtime_item (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('ovt.OVERTIME_ITEM_ID_SEQ'),
	report_id integer NOT NULL REFERENCES ovt.overtime_report (id),
	"date" date NOT NULL,
	project_id integer NOT NULL REFERENCES proj.project (id),
	hours smallint NULL,
	notes varchar(1024) NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NOT NULL REFERENCES empl.employee (id),
	deleted_at timestamp with time zone NULL,
	deleted_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE ovt.overtime_item IS 'One item in monthly overtime report';
COMMENT ON COLUMN ovt.overtime_item.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ovt.overtime_item.report_id IS 'Link to report';
COMMENT ON COLUMN ovt.overtime_item.date IS 'When overtime task happened';
COMMENT ON COLUMN ovt.overtime_item.project_id IS 'Overtime task`s project';
COMMENT ON COLUMN ovt.overtime_item.hours IS 'Overtime task`s spent hours';
COMMENT ON COLUMN ovt.overtime_item.created_at IS 'Created at';
COMMENT ON COLUMN ovt.overtime_item.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ovt.overtime_item.deleted_at IS 'Deleted at';
COMMENT ON COLUMN ovt.overtime_item.deleted_by IS 'Deleted by (link to employee)';


CREATE TABLE IF NOT EXISTS ovt.overtime_closed_period (
    period integer PRIMARY KEY NOT NULL,
	closed_at timestamp with time zone NOT NULL,
	closed_by integer NOT NULL REFERENCES empl.employee (id),
	"comment" text NULL
);
COMMENT ON TABLE ovt.overtime_closed_period IS 'Overtime periods closed for editing. No overtime items can be added/deleted or approved';
COMMENT ON COLUMN ovt.overtime_closed_period.period IS 'Overtime period (PK)';
COMMENT ON COLUMN ovt.overtime_closed_period.comment IS 'Comment';
COMMENT ON COLUMN ovt.overtime_closed_period.closed_at IS 'Closed at';
COMMENT ON COLUMN ovt.overtime_closed_period.closed_by IS 'Closed by (link to employee)';

CREATE SEQUENCE IF NOT EXISTS ovt.OVERTIME_PERIOD_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS ovt.overtime_period_history (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('ovt.OVERTIME_PERIOD_HISTORY_ID_SEQ'),
	"period" integer NOT NULL,
	"action" smallint NOT NULL,
	updated_at timestamp with time zone NOT NULL,
	updated_by integer NOT NULL REFERENCES empl.employee (id),
	"comment" text NULL
);

COMMENT ON TABLE ovt.overtime_period_history IS 'History of periods closing';
COMMENT ON COLUMN ovt.overtime_period_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ovt.overtime_period_history.period IS 'Overtime period (link to overtime_period_history)';
COMMENT ON COLUMN ovt.overtime_period_history.action IS 'ACTION_CLOSE=1; ACTION_REOPEN=2';
COMMENT ON COLUMN ovt.overtime_period_history.comment IS 'Comment';
COMMENT ON COLUMN ovt.overtime_period_history.updated_at IS 'Updated at';
COMMENT ON COLUMN ovt.overtime_period_history.updated_by IS 'updated by (link to employee)';


CREATE SEQUENCE IF NOT EXISTS ovt.OVERTIME_APPROVAL_DECISION_ID_SEQ;
CREATE TABLE IF NOT EXISTS ovt.overtime_approval_decision (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('ovt.OVERTIME_PERIOD_HISTORY_ID_SEQ'),
	report_id integer NOT NULL REFERENCES ovt.overtime_report (id),
	approver integer NOT NULL REFERENCES empl.employee (id),
	decision varchar(255) NULL,
	decision_time timestamp with time zone NOT NULL,
	cancel_decision_time timestamp with time zone NULL,
	"comment" text NULL
);

COMMENT ON TABLE ovt.overtime_approval_decision IS 'History of periods closing';
COMMENT ON COLUMN ovt.overtime_approval_decision.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ovt.overtime_approval_decision.report_id IS 'Link to overtime report';
COMMENT ON COLUMN ovt.overtime_approval_decision.approver IS 'Who make the decision';
COMMENT ON COLUMN ovt.overtime_approval_decision.decision IS 'Decision: [APPROVED, DECLINED]';
COMMENT ON COLUMN ovt.overtime_approval_decision.decision_time IS 'Approve/Decline time';
COMMENT ON COLUMN ovt.overtime_approval_decision.cancel_decision_time IS 'Time of decision cancel';
COMMENT ON COLUMN ovt.overtime_approval_decision.comment IS 'Comment';
