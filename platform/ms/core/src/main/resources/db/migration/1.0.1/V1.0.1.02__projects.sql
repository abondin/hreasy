CREATE SCHEMA IF NOT EXISTS ba;
CREATE SCHEMA IF NOT EXISTS proj;

-------------- BA
CREATE SEQUENCE IF NOT EXISTS ba.BA_ID_SEQ;
CREATE TABLE IF NOT EXISTS ba.business_account (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ba.BA_ID_SEQ'),
	"name" varchar(255) NULL,
	responsible_employee integer NULL,
	description text NULL,
	archived boolean NOT NULL default false,
	created_at timestamp with time zone NULL,
	created_by integer NULL
);
COMMENT ON TABLE ba.business_account IS 'Business Account. Aggregate entity for project budgeting';
COMMENT ON COLUMN ba.business_account.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ba.business_account.name IS 'Business account name';
COMMENT ON COLUMN ba.business_account.responsible_employee IS 'BA owner';
COMMENT ON COLUMN ba.business_account.description IS 'BA description';
COMMENT ON COLUMN ba.business_account.archived IS 'Shows if BA is not more available';
COMMENT ON COLUMN ba.business_account.created_at IS 'DB record created at';
COMMENT ON COLUMN ba.business_account.created_by IS 'DB record created by';

CREATE SEQUENCE IF NOT EXISTS ba.BA_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS ba.business_account_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ba.BA_HISTORY_ID_SEQ'),
	ba_id integer NOT NULL,
	"name" varchar(255) NULL,
	responsible_employee integer NULL,
	description text NULL,
	archived boolean NULL,
	updated_at timestamp with time zone NULL,
	updated_by integer NULL
);
COMMENT ON TABLE ba.business_account_history IS 'Business Account. Aggregate entity for project budgeting';
COMMENT ON COLUMN ba.business_account_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ba.business_account_history.ba_id IS 'Link to the business account';
COMMENT ON COLUMN ba.business_account_history.name IS 'Business account name';
COMMENT ON COLUMN ba.business_account_history.responsible_employee IS 'BA owner';
COMMENT ON COLUMN ba.business_account_history.description IS 'BA description';
COMMENT ON COLUMN ba.business_account_history.archived IS 'Shows if BA is not more available';
COMMENT ON COLUMN ba.business_account_history.updated_at IS 'DB record created at';
COMMENT ON COLUMN ba.business_account_history.updated_by IS 'DB record created by';



-- Project
CREATE SEQUENCE IF NOT EXISTS proj.PROJECT_ID_SEQ;
CREATE TABLE IF NOT EXISTS proj.project (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('proj.PROJECT_ID_SEQ'),
	ba_id integer NULL REFERENCES ba.business_account (id),
	"name" varchar(255) NULL,
	start_date date NULL,
	end_date date NULL,
	customer varchar(255) NULL,
	person_of_contact varchar(255) NULL,
	department_id integer NULL REFERENCES dict.department (id),
	created_at timestamp with time zone NULL,
	created_by integer NULL
);
COMMENT ON TABLE proj.project IS 'Project';
COMMENT ON COLUMN proj.project.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN proj.project.ba_id IS 'Project must be assigned to business account';
COMMENT ON COLUMN proj.project.name IS 'Project name';
COMMENT ON COLUMN proj.project.start_date IS 'Project start date';
COMMENT ON COLUMN proj.project.end_date IS 'Project end date';
COMMENT ON COLUMN proj.project.customer IS 'Customer';
COMMENT ON COLUMN proj.project.person_of_contact IS 'Customer person of contact';
COMMENT ON COLUMN proj.project.department_id IS 'Link to department';
COMMENT ON COLUMN proj.project.created_at IS 'DB record created at';
COMMENT ON COLUMN proj.project.created_by IS 'DB record created by';


CREATE SEQUENCE IF NOT EXISTS proj.PROJECT_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS proj.project_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('proj.PROJECT_HISTORY_ID_SEQ'),
	project_id integer,
	ba_id integer NULL,
	"name" varchar(255) NULL,
	start_date date NULL,
	end_date date NULL,
	customer varchar(255) NULL,
	person_of_contact varchar(255) NULL,
	department_id integer NULL,
	updated_at timestamp with time zone NULL,
	updated_by integer NULL
);
COMMENT ON TABLE proj.project_history IS 'Project History';
COMMENT ON COLUMN proj.project_history.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN proj.project_history.project_id IS 'Link to parent entity';
COMMENT ON COLUMN proj.project_history.ba_id IS 'Project must be assigned to business account';
COMMENT ON COLUMN proj.project_history.name IS 'Project name';
COMMENT ON COLUMN proj.project_history.start_date IS 'Project start date';
COMMENT ON COLUMN proj.project_history.end_date IS 'Project end date';
COMMENT ON COLUMN proj.project_history.customer IS 'Customer';
COMMENT ON COLUMN proj.project_history.person_of_contact IS 'Customer person of contact';
COMMENT ON COLUMN proj.project_history.department_id IS 'Link to department';
COMMENT ON COLUMN proj.project_history.updated_at IS 'DB record created at';
COMMENT ON COLUMN proj.project_history.updated_by IS 'DB record created by';
