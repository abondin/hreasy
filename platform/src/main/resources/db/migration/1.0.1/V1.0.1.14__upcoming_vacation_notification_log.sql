CREATE SEQUENCE IF NOT EXISTS notify.UPCOMING_VACATION_NOTIFICATION_LOG_ID_SEQ;
CREATE TABLE IF NOT EXISTS notify.upcoming_vacation_notification_log (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('notify.UPCOMING_VACATION_NOTIFICATION_LOG_ID_SEQ'),
	employee integer NOT NULL REFERENCES empl.employee (id),
	vacation integer NOT NULL REFERENCES vac.vacation (id),
	created_at timestamp with time zone NOT NULL
);

COMMENT ON TABLE notify.upcoming_vacation_notification_log IS 'Log send notification event to avoid repeated delivery';
COMMENT ON COLUMN notify.upcoming_vacation_notification_log.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN notify.upcoming_vacation_notification_log.employee IS 'Link to employee';
COMMENT ON COLUMN notify.upcoming_vacation_notification_log.vacation IS 'Link to vacation';
COMMENT ON COLUMN notify.upcoming_vacation_notification_log.created_at IS 'Created (sent notification) at';