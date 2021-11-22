--- Position in business account (like Senior Java Developer)
CREATE SEQUENCE IF NOT EXISTS ba.BA_POSITION_ID_SEQ;
CREATE TABLE IF NOT EXISTS ba.ba_position (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ba.BA_POSITION_ID_SEQ'),
	business_account integer NOT NULL REFERENCES ba.business_account (id),
	"name" varchar(255) NULL,
	rate numeric(18, 2) NULL,
	description text NULL,
	archived boolean NULL default false,
	created_at timestamp with time zone NULL,
    created_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE ba.ba_position IS 'Position in business account (like Senior Java Developer)';
COMMENT ON COLUMN ba.ba_position.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ba.ba_position.business_account IS 'Link to BA';
COMMENT ON COLUMN ba.ba_position.name IS 'Name of the position';
COMMENT ON COLUMN ba.ba_position.rate IS 'Rate (in local currency)';
COMMENT ON COLUMN ba.ba_position.description IS 'Description in free format';
COMMENT ON COLUMN ba.ba_position.archived IS 'If position is archived';
COMMENT ON COLUMN ba.ba_position.created_at IS 'Created at';
COMMENT ON COLUMN ba.ba_position.created_by IS 'Created by (link to employee)';


CREATE SEQUENCE IF NOT EXISTS ba.BA_POSITION_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS ba.ba_position_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ba.BA_POSITION_HISTORY_ID_SEQ'),
	ba_position_id integer  NULL,
	business_account integer NOT NULL,
	"name" varchar(255) NULL,
	rate numeric(18, 2) NULL,
	description text NULL,
	archived boolean NULL default false,
	created_at timestamp with time zone NULL,
    created_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE ba.ba_position_history IS 'Position in business account (like Senior Java Developer)';
COMMENT ON COLUMN ba.ba_position_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ba.ba_position_history.ba_position_id IS 'Link to position';
COMMENT ON COLUMN ba.ba_position_history.business_account IS 'Link to BA';
COMMENT ON COLUMN ba.ba_position_history.name IS 'Name of the position';
COMMENT ON COLUMN ba.ba_position_history.rate IS 'Rate (in local currency)';
COMMENT ON COLUMN ba.ba_position_history.description IS 'Description in free format';
COMMENT ON COLUMN ba.ba_position_history.archived IS 'If position is archived';
COMMENT ON COLUMN ba.ba_position_history.created_at IS 'Created at';
COMMENT ON COLUMN ba.ba_position_history.created_by IS 'Created by (link to employee)';

--- Assignment to business account
CREATE SEQUENCE IF NOT EXISTS ba.BA_ASSIGNMENT_ID_SEQ;
CREATE TABLE IF NOT EXISTS ba.ba_assignment (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ba.BA_ASSIGNMENT_ID_SEQ'),
	business_account integer NOT NULL REFERENCES ba.business_account (id),
	employee integer NULL REFERENCES empl.employee (id),
	project integer NULL REFERENCES proj.project (id),
	parent_assignment integer NULL REFERENCES ba.ba_assignment (id),
	ba_position integer NULL REFERENCES ba.ba_position (id),
	employment_rate NUMERIC(18,2) NULL,
	ba_position_rate NUMERIC(18,2) NULL,
	"comment" text NULL,
	start_date date NULL,
	end_date date NULL,
	planned_start_date date NULL,
	planned_end_date date NULL,
	archived boolean NULL default false,
	closed_at timestamp with time zone NULL,
	closed_by integer NULL REFERENCES empl.employee (id),
	closed_reason text NULL,
	closed_comment text NULL,
	created_at timestamp with time zone NULL,
	created_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE ba.ba_assignment IS 'BA assignment. Links project position and employee';
COMMENT ON COLUMN ba.ba_assignment.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ba.ba_assignment.business_account IS 'Link to BA';
COMMENT ON COLUMN ba.ba_assignment.employee IS 'Link to employee';
COMMENT ON COLUMN ba.ba_assignment.project IS 'Link to project';
COMMENT ON COLUMN ba.ba_assignment.parent_assignment IS 'Link to previous assignment record which initiated this one';
COMMENT ON COLUMN ba.ba_assignment.ba_position IS 'Position ';
COMMENT ON COLUMN ba.ba_assignment.comment IS 'Comment in free format';
COMMENT ON COLUMN ba.ba_assignment.start_date IS 'Actual assignment start';
COMMENT ON COLUMN ba.ba_assignment.end_date IS 'Actual assignment end';
COMMENT ON COLUMN ba.ba_assignment.planned_start_date IS 'Planned assignment start';
COMMENT ON COLUMN ba.ba_assignment.planned_end_date IS 'Planned assignment end';
COMMENT ON COLUMN ba.ba_assignment.archived IS 'Move to archive';
COMMENT ON COLUMN ba.ba_assignment.created_at IS 'Created at';
COMMENT ON COLUMN ba.ba_assignment.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ba.ba_assignment.closed_at IS 'Created at';
COMMENT ON COLUMN ba.ba_assignment.closed_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ba.ba_assignment.closed_reason IS 'Reason of assignment close';
COMMENT ON COLUMN ba.ba_assignment.closed_comment IS 'Comment on close';



--- Assignment history to business account
CREATE SEQUENCE IF NOT EXISTS ba.BA_ASSIGNMENT_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS ba.ba_assignment_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ba.BA_ASSIGNMENT_HISTORY_ID_SEQ'),
	ba_assignment_id integer NULL,
	business_account integer NULL,
	employee integer NULL,
	project integer NULL,
	parent_assignment integer NULL,
	ba_position integer NULL,
	employment_rate NUMERIC(18,2) NULL,
	ba_position_rate NUMERIC(18,2) NULL,
	"comment" text NULL,
	start_date date NULL,
	end_date date NULL,
	planned_start_date date NULL,
	planned_end_date date NULL,
	archived boolean NULL default false,
	closed_at timestamp with time zone NULL,
	closed_by integer NULL,
	closed_reason text NULL,
	closed_comment text NULL,
	created_at timestamp with time zone NULL,
	created_by integer NULL
);
COMMENT ON TABLE ba.ba_assignment_history IS 'BA assignment. Links project position and employee';
COMMENT ON COLUMN ba.ba_assignment_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ba.ba_assignment_history.ba_assignment_id IS 'Link to assignment';
COMMENT ON COLUMN ba.ba_assignment_history.business_account IS 'Link to BA';
COMMENT ON COLUMN ba.ba_assignment_history.employee IS 'Link to employee';
COMMENT ON COLUMN ba.ba_assignment_history.project IS 'Link to project';
COMMENT ON COLUMN ba.ba_assignment_history.parent_assignment IS 'Link to previous assignment record which initiated this one';
COMMENT ON COLUMN ba.ba_assignment_history.ba_position IS 'Position ';
COMMENT ON COLUMN ba.ba_assignment_history.comment IS 'Comment in free format';
COMMENT ON COLUMN ba.ba_assignment_history.start_date IS 'Actual assignment start';
COMMENT ON COLUMN ba.ba_assignment_history.end_date IS 'Actual assignment end';
COMMENT ON COLUMN ba.ba_assignment_history.planned_start_date IS 'Planned assignment start';
COMMENT ON COLUMN ba.ba_assignment_history.planned_end_date IS 'Planned assignment end';
COMMENT ON COLUMN ba.ba_assignment_history.archived IS 'Move to archive';
COMMENT ON COLUMN ba.ba_assignment_history.created_at IS 'Created at';
COMMENT ON COLUMN ba.ba_assignment_history.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ba.ba_assignment_history.closed_at IS 'Created at';
COMMENT ON COLUMN ba.ba_assignment_history.closed_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ba.ba_assignment_history.closed_reason IS 'Reason of assignment close';
COMMENT ON COLUMN ba.ba_assignment_history.closed_comment IS 'Comment on close';
