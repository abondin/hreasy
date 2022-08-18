-- HR 1.3 will be store all entities changes in one historical table.
-- In 1.2 only new entities will be logged in that way
-- For big installations this table can be partitioned by entity_type and year
CREATE schema if not exists history;

-- All implemented entities with new history approach
CREATE TYPE history_entity_type AS ENUM
    ('empl_responsible');
COMMENT ON TYPE history_entity_type IS 'All implemented entities with new history approach';


CREATE SEQUENCE IF NOT EXISTS history.history_id_seq;
CREATE TABLE IF NOT EXISTS history.history (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('history.history_id_seq'),
    entity_type history_entity_type not null,
    entity_id integer not null,
    entity_value JSONB null,
	created_at timestamp with time zone NULL,
	created_by integer NULL
);


COMMENT ON TABLE history.history IS 'Historical log for all business entities';
COMMENT ON COLUMN history.history.id IS 'Primary key';
COMMENT ON COLUMN history.history.entity_type IS 'Entity type. See history_entity_type type to get full list of supported values';
COMMENT ON COLUMN history.history.entity_id IS 'ID of the entity';
COMMENT ON COLUMN history.history.entity_value IS 'Serialized to JSON entity';
COMMENT ON COLUMN history.history.created_at IS 'Created at';
COMMENT ON COLUMN history.history.created_by IS 'Created by (link to employee)';
