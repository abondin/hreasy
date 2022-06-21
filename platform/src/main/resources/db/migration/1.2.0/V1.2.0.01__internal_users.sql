-- Login type in login history
ALTER TABLE sec.login_history ADD COLUMN IF NOT EXISTS
    login_type smallint NULL;
COMMENT ON COLUMN sec.login_history.login_type IS '1 - LDAP, 2 - INTERNAL, 3 - MASTER PASSWORD LDAP by default';

-- Internal user password
CREATE TABLE IF NOT EXISTS sec.passwd (
    employee integer PRIMARY KEY NOT NULL REFERENCES empl.employee (id),
    password_hash VARCHAR(1024) NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NOT NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE sec.passwd IS 'Internal (not ldap) password to login';
COMMENT ON COLUMN sec.passwd.employee IS 'Link to the employee';
COMMENT ON COLUMN sec.passwd.password_hash IS 'Hash of the password';
COMMENT ON COLUMN sec.passwd.created_at IS 'Created at';
COMMENT ON COLUMN sec.passwd.created_by IS 'Created by (link to employee)';

CREATE SEQUENCE IF NOT EXISTS sec.PASSWORD_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS sec.passwd_history (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('sec.PASSWORD_HISTORY_ID_SEQ'),
    employee integer,
    password_hash VARCHAR(1024),
	created_at timestamp with time zone NOT NULL,
	created_by integer
);
COMMENT ON TABLE sec.passwd_history IS 'Internal (not ldap) password to login';
COMMENT ON COLUMN sec.passwd_history.id IS 'Identifier of history record';
COMMENT ON COLUMN sec.passwd_history.employee IS 'Link to the employee';
COMMENT ON COLUMN sec.passwd_history.password_hash IS 'Hash of the password';
COMMENT ON COLUMN sec.passwd_history.created_at IS 'Created at';
COMMENT ON COLUMN sec.passwd_history.created_by IS 'Created by (link to employee)';