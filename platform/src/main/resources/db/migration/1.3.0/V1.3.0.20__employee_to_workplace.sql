ALTER TABLE empl.employee ADD COLUMN IF NOT EXISTS office_workplace integer REFERENCES dict.office_workplace(id) null;

ALTER TABLE dict.office_workplace ADD COLUMN IF NOT EXISTS type integer default 1;
UPDATE dict.office_workplace SET type=1 where type is null;
alter table dict.office_workplace alter column type set not null;

COMMENT ON COLUMN empl.employee.office_workplace IS 'Link to office workplace';
COMMENT ON COLUMN dict.office_workplace.type IS 'Type of workplace:
1 - Regular
2 - Guest
';