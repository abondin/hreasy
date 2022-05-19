ALTER TABLE dict.department
ADD COLUMN updated_at timestamp with time zone,
ADD COLUMN updated_by integer,
ADD COLUMN archived boolean default false;
COMMENT ON COLUMN dict.department.updated_at is 'Updated at';
COMMENT ON COLUMN dict.department.updated_by is 'Updated by (link to employee)';
COMMENT ON COLUMN dict.department.archived is 'Do not show entry in UI';
update dict.department set archived = false;
ALTER TABLE dict.department ALTER COLUMN archived SET NOT NULL;
ALTER TABLE dict.department  ADD CONSTRAINT department_unique_name UNIQUE (name);

ALTER TABLE dict.level
ADD COLUMN updated_at timestamp with time zone,
ADD COLUMN updated_by integer,
ADD COLUMN archived boolean default false;
COMMENT ON COLUMN dict.level.updated_at is 'Updated at';
COMMENT ON COLUMN dict.level.updated_by is 'Updated by (link to employee)';
COMMENT ON COLUMN dict.level.archived is 'Do not show entry in UI';
update dict.level set archived = false;
ALTER TABLE dict.level ALTER COLUMN archived SET NOT NULL;
ALTER TABLE dict.level  ADD CONSTRAINT level_unique_name UNIQUE (name);

ALTER TABLE dict.position
ADD COLUMN updated_at timestamp with time zone,
ADD COLUMN updated_by integer,
ADD COLUMN archived boolean default false;
COMMENT ON COLUMN dict.position.updated_at is 'Updated at';
COMMENT ON COLUMN dict.position.updated_by is 'Updated by (link to employee)';
COMMENT ON COLUMN dict.position.archived is 'Do not show entry in UI';
update dict.position set archived = false;
ALTER TABLE dict.position ALTER COLUMN archived SET NOT NULL;
ALTER TABLE dict.position  ADD CONSTRAINT position_unique_name UNIQUE (name);

ALTER TABLE dict.office_location
ADD COLUMN updated_at timestamp with time zone,
ADD COLUMN updated_by integer,
ADD COLUMN archived boolean NOT NULL default false;
COMMENT ON COLUMN dict.office_location.updated_at is 'Updated at';
COMMENT ON COLUMN dict.office_location.updated_by is 'Updated by (link to employee)';
COMMENT ON COLUMN dict.office_location.archived is 'Do not show entry in UI';
update dict.office_location set archived = false;
ALTER TABLE dict.office_location ALTER COLUMN archived SET NOT NULL;
ALTER TABLE dict.office_location  ADD CONSTRAINT office_location_unique_name UNIQUE (name);

----------------- Logs tables -------------------
----------------- department --------------------
CREATE SEQUENCE IF NOT EXISTS dict.DEP_LOG_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.department_log (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.DEP_LOG_ID_SEQ'),
	department_id integer,
	"name" varchar(255) NULL,
    archived boolean,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL
);
COMMENT ON TABLE dict.department_log IS 'Department log';
COMMENT ON COLUMN dict.department_log.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.department_log.department_id IS 'Link to parent';
COMMENT ON COLUMN dict.department_log.name IS 'Name';
COMMENT ON COLUMN dict.department_log.archived is 'Do not show entry in UI';
COMMENT ON COLUMN dict.department_log.created_at is 'Created at';
COMMENT ON COLUMN dict.department_log.created_by is 'Created by (link to employee)';

----------------- level --------------------
CREATE SEQUENCE IF NOT EXISTS dict.LEVEL_LOG_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.level_log (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.LEVEL_LOG_ID_SEQ'),
	level_id integer,
	"name" varchar(255) NULL,
	weight integer NULL,
    archived boolean,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL
);

COMMENT ON TABLE dict.level_log IS 'Employee Level Log';
COMMENT ON COLUMN dict.level_log.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.level_log.level_id IS 'Link to parent';
COMMENT ON COLUMN dict.level_log.name IS 'Name';
COMMENT ON COLUMN dict.level_log.weight IS 'Weight to sort levels (30 - for Senior, 10 - for Junior)';
COMMENT ON COLUMN dict.level_log.archived is 'Do not show entry in UI';
COMMENT ON COLUMN dict.level_log.created_at is 'Created at';
COMMENT ON COLUMN dict.level_log.created_by is 'Created by (link to employee)';

----------------- position --------------------
CREATE SEQUENCE IF NOT EXISTS dict.POSITION_LOG_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.position_log (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.POSITION_LOG_ID_SEQ'),
	position_id integer,
    "name" varchar(255) NULL,
	category varchar(255) NULL,
    archived boolean,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL
);

COMMENT ON TABLE dict.position_log IS 'Employee Position';
COMMENT ON COLUMN dict.position_log.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.position_log.position_id IS 'Link to parent';
COMMENT ON COLUMN dict.position_log.name IS 'Official name of the position (for HR uses)';
COMMENT ON COLUMN dict.position_log.category IS 'Simplified position name for public access';
COMMENT ON COLUMN dict.position_log.archived is 'Do not show entry in UI';
COMMENT ON COLUMN dict.position_log.created_at is 'Created at';
COMMENT ON COLUMN dict.position_log.created_by is 'Created by (link to employee)';


---------------- office location -------------------------------
CREATE SEQUENCE IF NOT EXISTS dict.OFFICE_LOCATION_LOG_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.office_location_log (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.OFFICE_LOCATION_LOG_ID_SEQ'),
	office_location_id integer,
	"name" varchar(255) NULL,
	description varchar(1024) NULL,
	office varchar(255) NULL,
    archived boolean,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL
);
COMMENT ON TABLE dict.office_location_log IS 'Employee office location';
COMMENT ON COLUMN dict.office_location_log.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.office_location_log.office_location_id IS 'Link to parent';
COMMENT ON COLUMN dict.office_location_log.name IS 'Name of the location (307A)';
COMMENT ON COLUMN dict.office_location_log.description IS 'Location description';
COMMENT ON COLUMN dict.office_location_log.office IS 'Name of the office if many';
COMMENT ON COLUMN dict.office_location_log.archived is 'Do not show entry in UI';
COMMENT ON COLUMN dict.office_location_log.created_at is 'Created at';
COMMENT ON COLUMN dict.office_location_log.created_by is 'Created by (link to employee)';


-------------------- Security ----------------------------
-- Permission to admin department
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_department','Create/update/delete department') on conflict do nothing;
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','admin_department'),
	 ('hr','admin_department') on conflict do nothing;
-- Permission to admin level
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_level','Create/update/delete employee overall skills level') on conflict do nothing;
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','admin_level'),
	 ('hr','admin_level') on conflict do nothing;
-- Permission to admin position
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_position','Create/update/delete employee position') on conflict do nothing;
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','admin_position'),
	 ('hr','admin_position') on conflict do nothing;
-- Permission to admin office location
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_office_location','Create/update/delete office location') on conflict do nothing;
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','admin_office_location'),
	 ('hr','admin_office_location') on conflict do nothing;

