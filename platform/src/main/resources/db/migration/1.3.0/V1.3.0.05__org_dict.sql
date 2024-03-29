CREATE SEQUENCE IF NOT EXISTS dict.ORG_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.organization (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.ORG_ID_SEQ'),
	"name" varchar(255) not NULL,
	"description" text null,
	archived boolean not null default false,
	updated_at timestamp with time zone not null,
	updated_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE dict.organization IS 'Organizations dictionary. Only for HR stuff';
COMMENT ON COLUMN dict.organization.id IS 'Id';
COMMENT ON COLUMN dict.organization.name IS 'Name';
COMMENT ON COLUMN dict.organization.description IS 'Description';
COMMENT ON COLUMN dict.organization.updated_at is 'Updated at';
COMMENT ON COLUMN dict.organization.updated_by is 'Updated by (link to employee)';
COMMENT ON COLUMN dict.organization.archived is 'Do not show entry in UI';

CREATE SEQUENCE IF NOT EXISTS dict.ORG_LOG_ID_SEQ;
CREATE TABLE IF NOT EXISTS dict.organization_log (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('dict.ORG_LOG_ID_SEQ'),
	organization_id integer,
	"name" varchar(255) NULL,
	"description" text null,
    archived boolean,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL
);
COMMENT ON TABLE dict.organization_log IS 'Organization log';
COMMENT ON COLUMN dict.organization_log.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN dict.organization_log.organization_id IS 'Link to parent';
COMMENT ON COLUMN dict.organization_log.name IS 'Name';
COMMENT ON COLUMN dict.organization_log.archived is 'Do not show entry in UI';
COMMENT ON COLUMN dict.organization_log.created_at is 'Created at';
COMMENT ON COLUMN dict.organization_log.created_by is 'Created by (link to employee)';

-- Permission to admin level
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_organization','Create/update/delete organizations') on conflict do nothing;
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','admin_organization'),
	 ('hr','admin_organization') on conflict do nothing;
