ALTER TABLE proj.project ADD COLUMN IF NOT EXISTS plan_start_date date null;
ALTER TABLE proj.project ADD COLUMN IF NOT EXISTS plan_end_date date null;

UPDATE proj.project SET plan_start_date=start_date where plan_start_date is null;
UPDATE proj.project SET plan_end_date=end_date where plan_end_date is null;

COMMENT ON COLUMN proj.project.plan_start_date IS 'Planning date of the project start';
COMMENT ON COLUMN proj.project.plan_end_date IS 'Planning date of the project end';

