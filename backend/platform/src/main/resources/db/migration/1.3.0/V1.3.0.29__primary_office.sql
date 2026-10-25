ALTER TABLE dict.office ADD COLUMN primary_office boolean NOT NULL DEFAULT false;
COMMENT ON COLUMN dict.office.primary_office IS 'Whether the office is considered a primary office of the organization';
