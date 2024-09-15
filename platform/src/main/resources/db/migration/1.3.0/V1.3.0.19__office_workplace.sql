CREATE SEQUENCE IF NOT EXISTS dict.OFFICE_WORKPLACE_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.office_workplace(
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.OFFICE_WORKPLACE_ID_SEQ'),
	office_location_id int NOT NULL REFERENCES dict.office_location(id),
    "name" varchar(255) NOT NULL,
    description varchar(1024) NULL,
    archived boolean NOT NULL DEFAULT false,
    map_x int NULL,
    map_y int NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE dict.office_workplace IS 'Workplace in office location';
COMMENT ON COLUMN dict.office_workplace.created_by IS 'Employee who created this entry';
COMMENT ON COLUMN dict.office_workplace.office_location_id IS 'Office location where this workplace is';
COMMENT ON COLUMN dict.office_workplace.name IS 'Name of workplace';
COMMENT ON COLUMN dict.office_workplace.description IS 'Description of workplace';
COMMENT ON COLUMN dict.office_workplace.archived IS 'Is this entry archived';
COMMENT ON COLUMN dict.office_workplace.map_x IS 'X coordinate on office location map';
COMMENT ON COLUMN dict.office_workplace.map_y IS 'Y coordinate on office location map';

COMMENT ON COLUMN history.history.entity_type IS '
  [empl_manager] - Entity type,
  [working_days] - Working Days Calendar,
  [timesheet_record] - Timesheet Record
  [support_request_group] - Support Request Group
  [support_request] - Support Request
  [junior_registry] - Juniors registry
  [junior_registry_report] - Juniors registry
  [office] - Office
  [office_location] - Office Location
  [office_workplace] - Office Location Seat
';

