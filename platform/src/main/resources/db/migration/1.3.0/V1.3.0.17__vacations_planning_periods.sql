--- Period when employee can send his vacations requests
CREATE TABLE IF NOT EXISTS vac.vac_planning_period (
    year integer PRIMARY KEY NOT NULL,
	opened_at timestamp with time zone NOT NULL,
	opened_by integer NOT NULL REFERENCES empl.employee (id),
	closed_at timestamp with time zone NULL,
	closed_by integer NULL REFERENCES empl.employee (id),
	"comment" text NULL
);

COMMENT ON TABLE vac.vac_planning_period IS 'Period when employee can send his vacations requests';
COMMENT ON COLUMN vac.vac_planning_period.year IS 'Salary request period (PK)';
COMMENT ON COLUMN vac.vac_planning_period.comment IS 'Comment';
COMMENT ON COLUMN vac.vac_planning_period.opened_at IS 'Closed at';
COMMENT ON COLUMN vac.vac_planning_period.opened_by IS 'Closed by (link to employee)';