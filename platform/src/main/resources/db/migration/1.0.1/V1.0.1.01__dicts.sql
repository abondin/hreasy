CREATE SCHEMA IF NOT EXISTS dict;

----------------- department --------------------
CREATE SEQUENCE IF NOT EXISTS dict.DEP_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.department (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.DEP_ID_SEQ'),
	"name" varchar(255) NULL
);
COMMENT ON TABLE dict.department IS 'Department. Uses in security model. All projects assigned to departments';
COMMENT ON COLUMN dict.department.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.department.name IS 'Name';

----------------- level --------------------
CREATE SEQUENCE IF NOT EXISTS dict.LEVEL_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.level (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.LEVEL_ID_SEQ'),
	"name" varchar(255) NULL,
	weight integer NULL
);

COMMENT ON TABLE dict.level IS 'Employee Level (Senior, Middle, Junior)';
COMMENT ON COLUMN dict.level.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.level.name IS 'Name';
COMMENT ON COLUMN dict.level.weight IS 'Weight to sort levels (30 - for Senior, 10 - for Junior)';

----------------- position --------------------
CREATE SEQUENCE IF NOT EXISTS dict.POSITION_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.position (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.POSITION_ID_SEQ'),
	"name" varchar(255) NULL,
	category varchar(255) NULL
);

COMMENT ON TABLE dict.position IS 'Employee Position';
COMMENT ON COLUMN dict.position.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.position.name IS 'Official name of the position (for HR uses)';
COMMENT ON COLUMN dict.position.category IS 'Simplified position name for public access';


---------------- office location -------------------------------
CREATE SEQUENCE IF NOT EXISTS dict.OFFICE_LOCATION_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.office_location (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.POSITION_ID_SEQ'),
	"name" varchar(255) NULL,
	description varchar(1024) NULL,
	office varchar(255) NULL
);
COMMENT ON TABLE dict.office_location IS 'Employee office location';
COMMENT ON COLUMN dict.office_location.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.office_location.name IS 'Name of the location (307A)';
COMMENT ON COLUMN dict.office_location.description IS 'Location description';
COMMENT ON COLUMN dict.office_location.office IS 'Name of the office if many';

