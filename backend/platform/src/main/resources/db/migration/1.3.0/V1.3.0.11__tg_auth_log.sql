CREATE TABLE IF NOT EXISTS sec.tg_auth_log (
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    telegram_account varchar(256)  NOT NULL,
    day date not null,
    cnt integer NOT NULL,
    PRIMARY KEY(employee_id, telegram_account, day)
);


COMMENT ON TABLE sec.tg_auth_log IS 'Daily unique telegram chat bot activities for employee';

COMMENT ON COLUMN sec.tg_auth_log.employee_id IS 'Employee';
COMMENT ON COLUMN sec.tg_auth_log.telegram_account IS 'Employee telegram account';
COMMENT ON COLUMN sec.tg_auth_log.day IS 'Statistic collecting by day';
COMMENT ON COLUMN sec.tg_auth_log.cnt IS 'Count of API executions for one day';
