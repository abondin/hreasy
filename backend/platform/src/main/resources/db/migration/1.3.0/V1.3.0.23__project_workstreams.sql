ALTER TABLE proj.project ADD COLUMN external_id varchar(255) NULL;
ALTER TABLE proj.project_history ADD COLUMN external_id varchar(255) NULL;

COMMENT ON COLUMN proj.project.external_id IS 'Stable project identifier in external systems';
COMMENT ON COLUMN proj.project_history.external_id IS 'Stable project identifier in external systems';

CREATE UNIQUE INDEX project_external_id_unique
    ON proj.project (external_id)
    WHERE external_id IS NOT NULL;

CREATE SEQUENCE IF NOT EXISTS proj.project_workstream_id_seq;
CREATE TABLE proj.project_workstream (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('proj.project_workstream_id_seq'),
    project_id integer NOT NULL REFERENCES proj.project (id),
    external_id varchar(255) NULL,
    display_name varchar(255) NOT NULL,
    description text NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    updated_at timestamp with time zone NULL,
    updated_by integer NULL REFERENCES empl.employee (id),
    deleted_at timestamp with time zone NULL,
    deleted_by integer NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE proj.project_workstream IS 'Project workstream used for detailed cost planning';
COMMENT ON COLUMN proj.project_workstream.id IS 'Primary key';
COMMENT ON COLUMN proj.project_workstream.project_id IS 'Parent project';
COMMENT ON COLUMN proj.project_workstream.external_id IS 'Stable workstream identifier in external systems';
COMMENT ON COLUMN proj.project_workstream.display_name IS 'Workstream display name';
COMMENT ON COLUMN proj.project_workstream.description IS 'Workstream description';
COMMENT ON COLUMN proj.project_workstream.created_at IS 'Creation time';
COMMENT ON COLUMN proj.project_workstream.created_by IS 'Employee who created the workstream';
COMMENT ON COLUMN proj.project_workstream.updated_at IS 'Last update time';
COMMENT ON COLUMN proj.project_workstream.updated_by IS 'Employee who last updated the workstream';
COMMENT ON COLUMN proj.project_workstream.deleted_at IS 'Soft deletion time';
COMMENT ON COLUMN proj.project_workstream.deleted_by IS 'Employee who soft-deleted the workstream';

CREATE UNIQUE INDEX project_workstream_external_id_unique
    ON proj.project_workstream (project_id, external_id)
    WHERE external_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX project_workstream_active_project_idx
    ON proj.project_workstream (project_id, display_name)
    WHERE deleted_at IS NULL;

ALTER TABLE ovt.overtime_item
    ADD COLUMN workstream_id integer NULL REFERENCES proj.project_workstream (id);
COMMENT ON COLUMN ovt.overtime_item.workstream_id IS 'Optional project workstream';

ALTER TABLE alloc.resource_allocation_revision
    ADD COLUMN workstream_id integer NULL REFERENCES proj.project_workstream (id);
COMMENT ON COLUMN alloc.resource_allocation_revision.workstream_id IS 'Optional project workstream edited by this revision';

ALTER TABLE alloc.resource_allocation
    ADD COLUMN workstream_id integer NULL REFERENCES proj.project_workstream (id);
COMMENT ON COLUMN alloc.resource_allocation.workstream_id IS 'Optional project workstream receiving the allocation';

ALTER TABLE alloc.resource_allocation DROP CONSTRAINT resource_allocation_cell_unique;
CREATE UNIQUE INDEX resource_allocation_cell_unique
    ON alloc.resource_allocation (year, period, employee_id, project_id, workstream_id) NULLS NOT DISTINCT;
