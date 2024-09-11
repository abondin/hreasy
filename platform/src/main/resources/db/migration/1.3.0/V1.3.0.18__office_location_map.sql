CREATE SEQUENCE IF NOT EXISTS dict.OFFICE_LOCATION_MAP_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.office_location_map (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.OFFICE_LOCATION_MAP_ID_SEQ'),
    map_svg text null,
    description text null,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL,
    deleted_at timestamp with time zone NULL,
    deleted_by integer NULL
);

COMMENT ON TABLE dict.office_location_map IS 'SVG map of office locations';
COMMENT ON COLUMN dict.office_location_map.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.office_location_map.map_svg IS 'SVG map of office location';
COMMENT ON COLUMN dict.office_location_map.description IS 'Description of the office location';
COMMENT ON COLUMN dict.office_location_map.created_at IS 'Created at';
COMMENT ON COLUMN dict.office_location_map.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN dict.office_location_map.deleted_at IS 'Deleted at';
COMMENT ON COLUMN dict.office_location_map.deleted_by IS 'Deleted by (link to employee)';