CREATE SCHEMA IF NOT EXISTS vac;

CREATE SEQUENCE IF NOT EXISTS vac.VACATION_ID_SEQ;
CREATE TABLE IF NOT EXISTS vac.vacation (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('vac.VACATION_ID_SEQ'),
	employee integer NOT NULL REFERENCES empl.employee (id),
	"year" integer NOT NULL,
	start_date date NULL,
	end_date date NULL,
	notes varchar(1024) NULL,
	planned_start_date date NULL,
	planned_end_date date NULL,
	days_number smallint NULL,
	stat smallint NOT NULL,
	documents varchar(255) NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NOT NULL REFERENCES empl.employee (id),
	updated_at timestamp with time zone NOT NULL,
	updated_by integer NOT NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE vac.vacation IS 'One vacation for one employee';
COMMENT ON COLUMN vac.vacation.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN vac.vacation.employee IS 'Link to employee';
COMMENT ON COLUMN vac.vacation.year IS 'Vacation report year';
COMMENT ON COLUMN vac.vacation.start_date IS 'Start date';
COMMENT ON COLUMN vac.vacation.end_date IS 'End date';
COMMENT ON COLUMN vac.vacation.notes IS 'Notes in free format';
COMMENT ON COLUMN vac.vacation.planned_start_date IS 'Initial planned start date';
COMMENT ON COLUMN vac.vacation.planned_end_date IS 'Initial planned end date';
COMMENT ON COLUMN vac.vacation.days_number IS 'Number of vacation days';
COMMENT ON COLUMN vac.vacation.stat IS 'Status of vacation: 0 - planned, 1 - taken, 2 - compensation, 3 - canceled (canceled by employee), 4 - rejected (created by mistake)';
COMMENT ON COLUMN vac.vacation.documents IS 'Document status in free form';
COMMENT ON COLUMN vac.vacation.created_at IS 'Created at';
COMMENT ON COLUMN vac.vacation.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN vac.vacation.updated_at IS 'Updated at';
COMMENT ON COLUMN vac.vacation.updated_by IS 'Updated by (link to employee)';


CREATE SEQUENCE IF NOT EXISTS vac.VACATION_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS vac.vacation_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('vac.VACATION_HISTORY_ID_SEQ'),
	vacation_id integer NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NOT NULL,
	employee integer NULL,
	"year" integer NOT NULL,
	start_date date NULL,
	end_date date NULL,
	planned_start_date date NULL,
	planned_end_date date NULL,
	days_number smallint NULL,
	stat smallint NULL,
	documents varchar(255) NULL,
	notes varchar(1024) NULL
);

COMMENT ON TABLE vac.vacation_history IS 'One vacation for one employee';
COMMENT ON COLUMN vac.vacation_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN vac.vacation_history.vacation_id IS 'Link to vacation table';
COMMENT ON COLUMN vac.vacation_history.employee IS 'Link to employee';
COMMENT ON COLUMN vac.vacation_history.year IS 'Vacation report year';
COMMENT ON COLUMN vac.vacation_history.start_date IS 'Start date';
COMMENT ON COLUMN vac.vacation_history.end_date IS 'End date';
COMMENT ON COLUMN vac.vacation_history.notes IS 'Notes in free format';
COMMENT ON COLUMN vac.vacation_history.planned_start_date IS 'Initial planned start date';
COMMENT ON COLUMN vac.vacation_history.planned_end_date IS 'Initial planned end date';
COMMENT ON COLUMN vac.vacation_history.days_number IS 'Number of vacation days';
COMMENT ON COLUMN vac.vacation_history.stat IS 'Status of vacation: 0 - planned, 1 - taken, 2 - compensation, 3 - canceled (canceled by employee), 4 - rejected (created by mistake)';
COMMENT ON COLUMN vac.vacation_history.documents IS 'Document status in free form';
COMMENT ON COLUMN vac.vacation_history.created_at IS 'Created at';
COMMENT ON COLUMN vac.vacation_history.created_by IS 'Created by (link to employee)';
