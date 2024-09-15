CREATE SEQUENCE IF NOT EXISTS dict.OFFICE_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.office (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.OFFICE_ID_SEQ'),
    "name" varchar(255) NOT NULL,
    address varchar(1024) NULL,
    description varchar(1024) NULL,
    archived boolean NOT NULL DEFAULT false,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    map_svg text NULL
);
COMMENT ON TABLE dict.office IS 'Company office';
COMMENT ON COLUMN dict.office.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.office."name" IS 'Office name';
COMMENT ON COLUMN dict.office.address IS 'Office address';
COMMENT ON COLUMN dict.office.description IS 'Office description';
COMMENT ON COLUMN dict.office.map_svg IS 'Office map SVG';

ALTER TABLE dict.office_location ADD COLUMN IF NOT EXISTS office_id int null REFERENCES dict.office(id);
COMMENT ON COLUMN dict.office_location.office IS '!!! DEPRECATED COLUMN !!! Use office_id instead';

ALTER TABLE dict.office_location ADD COLUMN IF NOT EXISTS map_svg text NULL;
COMMENT ON COLUMN dict.office_location.map_svg IS 'Office location map SVG';

DROP TABLE IF EXISTS dict.office_location_log;

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
  ';
