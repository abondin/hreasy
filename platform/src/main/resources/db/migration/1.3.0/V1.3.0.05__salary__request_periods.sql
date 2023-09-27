CREATE TABLE IF NOT EXISTS sal.salary_request_closed_period (
    period integer PRIMARY KEY NOT NULL,
	closed_at timestamp with time zone NOT NULL,
	closed_by integer NOT NULL REFERENCES empl.employee (id),
	"comment" text NULL
);
COMMENT ON TABLE sal.salary_request_closed_period IS 'Salary request periods closed for editing. No requests can be reported';
COMMENT ON COLUMN sal.salary_request_closed_period.period IS 'Salary request period (PK)';
COMMENT ON COLUMN sal.salary_request_closed_period.comment IS 'Comment';
COMMENT ON COLUMN sal.salary_request_closed_period.closed_at IS 'Closed at';
COMMENT ON COLUMN sal.salary_request_closed_period.closed_by IS 'Closed by (link to employee)';

INSERT INTO sec.perm (permission,description) VALUES
    ('close_salary_request_period','Close salary request period to deny new requests reporting') ON CONFLICT DO NOTHING;
INSERT INTO sec.role_perm ("role","permission") VALUES
    ('global_admin','close_salary_request_period'),
    ('finance','close_salary_request_period') ON CONFLICT DO NOTHING;