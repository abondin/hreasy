CREATE SEQUENCE IF NOT EXISTS empl.import_workflow_id_seq;
CREATE TABLE IF NOT EXISTS empl.import_workflow (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.import_workflow_id_seq'),
    state smallint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    completed_at timestamp with time zone NULL,
    completed_by integer NULL REFERENCES empl.employee (id),
    filename varchar(1024) NULL,
    file_content_length bigint NULL,
    config jsonb NULL,
    data jsonb NULL
);
-- Allow only one active import workflow for user
CREATE UNIQUE INDEX IF NOT EXISTS active_unique ON empl.import_workflow (created_by)
    WHERE state in (0,1,2);

COMMENT ON TABLE empl.import_workflow IS 'Import employees from excel file workflow';
COMMENT ON COLUMN empl.import_workflow.id IS 'Primary key';
COMMENT ON COLUMN empl.import_workflow.state IS '0 - workflow created; 1 - file uploaded; 2 - config set; 3 - changes applied; -1 aborted (by user or internal error)';
COMMENT ON COLUMN empl.import_workflow.created_at IS 'Created at';
COMMENT ON COLUMN empl.import_workflow.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN empl.import_workflow.completed_at IS 'Completed/Canceled/Aborted by error at';
COMMENT ON COLUMN empl.import_workflow.completed_by IS 'Completed/Canceled by (link to employee)';
COMMENT ON COLUMN empl.import_workflow.filename IS 'Original uploaded filename';
COMMENT ON COLUMN empl.import_workflow.file_content_length IS 'Uploaded file size';
COMMENT ON COLUMN empl.import_workflow.config IS 'Workflow configuration';
COMMENT ON COLUMN empl.import_workflow.data IS 'Workflow data changes preview';

INSERT INTO sec.perm (permission,description) VALUES
    ('import_employee','Import employees from file');

INSERT INTO sec.role_perm ("role","permission") VALUES
    ('global_admin','import_employee'),
     ('hr','import_employee');