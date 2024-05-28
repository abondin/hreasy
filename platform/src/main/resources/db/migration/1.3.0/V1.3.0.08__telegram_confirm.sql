ALTER TABLE empl.employee
    add column telegram_confirmed_at timestamp;

ALTER TABLE empl.employee_history
    add column telegram_confirmed_at timestamp;

comment on column empl.employee.telegram_confirmed_at is '
Employee has confirmed own telegram account
';
comment on column empl.employee_history.telegram_confirmed_at is '
Employee has confirmed own telegram account
';
