CREATE schema if not exists support;

CREATE TABLE IF NOT EXISTS support.support_request_group (
    key varchar(64) PRIMARY KEY NOT NULL,
    display_name varchar(256)  NOT NULL,
    description varchar(1024)  NULL,
    configuration JSONB NOT NULL DEFAULT '{emails:[]}'::jsonb,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone NULL,
    deleted_by integer NULL REFERENCES empl.employee (id),
);

CREATE SEQUENCE IF NOT EXISTS support.support_request_id_seq;
CREATE TABLE IF NOT EXISTS support.support_request (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('support.support_request_id_seq'),
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    support_group integer not null references support.support_request_group(id),
    -- 1 - Telegram Bot
    source_type integer NOT NULL,
    message text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone NULL,
    deleted_by integer NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE support.support_request_group IS 'Table for storing support request groups';

COMMENT ON COLUMN support.support_request_group.key IS 'Unique key of the support request group';
COMMENT ON COLUMN support.support_request_group.display_name IS 'Display name of the support request group';
COMMENT ON COLUMN support.support_request_group.description IS 'Description of the support request group';
COMMENT ON COLUMN support.support_request_group.configuration IS 'Configuration is JSON format
only emails list to send request is now supported:
{emails: ['support@company.co', 'hr@company.co']}
';
COMMENT ON COLUMN support.support_request_group.created_at IS 'Timestamp when the support request group was created';
COMMENT ON COLUMN support.support_request_group.created_by IS 'User ID who created the support request group record';
COMMENT ON COLUMN support.support_request_group.deleted_at IS 'Timestamp when the support request group was deleted';
COMMENT ON COLUMN support.support_request_group.deleted_by IS 'User ID who deleted the support request group record';

COMMENT ON TABLE support.support_request IS 'Table for storing support requests';

COMMENT ON COLUMN support.support_request.id IS 'Unique ID of the support request';
COMMENT ON COLUMN support.support_request.employee_id IS 'ID of the employee making the support request';
COMMENT ON COLUMN support.support_request_group.key IS 'Unique key that helps employee identify the request';
COMMENT ON COLUMN support.support_request.support_group IS 'ID of the support group associated with the request';
COMMENT ON COLUMN support.support_request.source_type IS 'Type of the source (1 for Telegram Bot)';
COMMENT ON COLUMN support.support_request.message IS 'Content of the support request message';
COMMENT ON COLUMN support.support_request.created_at IS 'Timestamp when the support request was created';
COMMENT ON COLUMN support.support_request.created_by IS 'User ID who created the support request';
COMMENT ON COLUMN support.support_request.deleted_at IS 'Timestamp when the support request was deleted';
COMMENT ON COLUMN support.support_request.deleted_by IS 'User ID who deleted the support request';

COMMENT ON COLUMN history.history.entity_type IS '
  [empl_manager] - Entity type,
  [working_days] - Working Days Calendar,
  [timesheet_record] - Timesheet Record
  [support_request_group] - Support Request Group
  [support_request] - Support Request
  ';
