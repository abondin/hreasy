CREATE SCHEMA IF NOT EXISTS notify_ms;

CREATE SEQUENCE IF NOT EXISTS notify_ms.NOTIFICATION_ID_SEQ;
CREATE TABLE IF NOT EXISTS notify_ms.notification (
    id bigint PRIMARY KEY NOT NULL DEFAULT nextval('notify_ms.NOTIFICATION_ID_SEQ'),
    event_type varchar(255) NOT NULL,
    recipient_type varchar(32) NOT NULL,
    recipient_login varchar(512) NULL,
    recipient_chat_id varchar(512) NULL,
    employee_id integer NULL,
    dedupe_key varchar(512) NOT NULL,
    priority varchar(32) NOT NULL,
    locale varchar(16) NULL,
    title varchar(1024) NULL,
    body text NOT NULL,
    data jsonb NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

ALTER TABLE notify_ms.notification
    ADD CONSTRAINT notification_dedupe_key_uk UNIQUE (dedupe_key);

CREATE SEQUENCE IF NOT EXISTS notify_ms.NOTIFICATION_DELIVERY_ID_SEQ;
CREATE TABLE IF NOT EXISTS notify_ms.notification_delivery (
    id bigint PRIMARY KEY NOT NULL DEFAULT nextval('notify_ms.NOTIFICATION_DELIVERY_ID_SEQ'),
    notification_id bigint NOT NULL REFERENCES notify_ms.notification (id),
    channel varchar(64) NOT NULL,
    status varchar(64) NOT NULL,
    due_at timestamp with time zone NOT NULL,
    digest_key varchar(512) NULL,
    attempt_count integer NOT NULL DEFAULT 0,
    error_count integer NOT NULL DEFAULT 0,
    max_attempts integer NOT NULL,
    last_success_at timestamp with time zone NULL,
    last_attempt_at timestamp with time zone NULL,
    next_attempt_at timestamp with time zone NULL,
    external_message_id varchar(512) NULL,
    provider_payload_id varchar(512) NULL,
    provider_status_code varchar(64) NULL,
    last_error_code varchar(128) NULL,
    last_error_message text NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);

CREATE INDEX IF NOT EXISTS notification_delivery_status_due_idx
    ON notify_ms.notification_delivery (status, due_at);
CREATE INDEX IF NOT EXISTS notification_delivery_notification_idx
    ON notify_ms.notification_delivery (notification_id);
CREATE INDEX IF NOT EXISTS notification_delivery_digest_idx
    ON notify_ms.notification_delivery (channel, digest_key, due_at);

COMMENT ON SCHEMA notify_ms IS 'HR Easy notification microservice schema';
COMMENT ON TABLE notify_ms.notification IS 'Accepted normalized notification events';
COMMENT ON TABLE notify_ms.notification_delivery IS 'Channel delivery queue with current state and retry counters';
