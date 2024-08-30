ALTER TABLE dict.office_location ADD COLUMN IF NOT EXISTS map_svg text null;
ALTER TABLE dict.office_location_log ADD COLUMN IF NOT EXISTS map_svg text null;

COMMENT ON COLUMN dict.office_location.map_svg IS 'SVG of the office location map';
COMMENT ON COLUMN dict.office_location_log.map_svg IS 'SVG of the office location map';
