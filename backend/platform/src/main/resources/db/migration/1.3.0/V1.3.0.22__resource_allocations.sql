CREATE SCHEMA IF NOT EXISTS alloc;

CREATE SEQUENCE IF NOT EXISTS alloc.resource_allocation_revision_id_seq;
CREATE TABLE alloc.resource_allocation_revision (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('alloc.resource_allocation_revision_id_seq'),
    period integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE alloc.resource_allocation_revision IS 'One batch of resource allocation changes';
COMMENT ON COLUMN alloc.resource_allocation_revision.id IS 'Primary key';
COMMENT ON COLUMN alloc.resource_allocation_revision.period IS 'Allocation period in YYYYMM format with zero-based month; 202005 means June 2020';
COMMENT ON COLUMN alloc.resource_allocation_revision.created_at IS 'Revision creation time';
COMMENT ON COLUMN alloc.resource_allocation_revision.created_by IS 'Employee who created the revision';

CREATE SEQUENCE IF NOT EXISTS alloc.resource_allocation_id_seq;
CREATE TABLE alloc.resource_allocation (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('alloc.resource_allocation_id_seq'),
    period integer NOT NULL,
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    project_id integer NOT NULL REFERENCES proj.project (id),
    percent smallint NOT NULL CHECK (percent > 0 AND percent <= 1000),
    revision_id integer NOT NULL REFERENCES alloc.resource_allocation_revision (id),
    CONSTRAINT resource_allocation_cell_unique UNIQUE (period, employee_id, project_id)
);

COMMENT ON TABLE alloc.resource_allocation IS 'Current monthly allocation of an employee to a project';
COMMENT ON COLUMN alloc.resource_allocation.id IS 'Primary key';
COMMENT ON COLUMN alloc.resource_allocation.period IS 'Allocation period in YYYYMM format with zero-based month; 202005 means June 2020';
COMMENT ON COLUMN alloc.resource_allocation.employee_id IS 'Allocated employee';
COMMENT ON COLUMN alloc.resource_allocation.project_id IS 'Project receiving the allocation';
COMMENT ON COLUMN alloc.resource_allocation.percent IS 'Allocation percentage from 1 through 1000';
COMMENT ON COLUMN alloc.resource_allocation.revision_id IS 'Revision that last changed this cell';

CREATE INDEX resource_allocation_period_employee_idx
    ON alloc.resource_allocation (period, employee_id);
CREATE INDEX resource_allocation_period_project_idx
    ON alloc.resource_allocation (period, project_id);

CREATE SEQUENCE IF NOT EXISTS alloc.resource_allocation_change_id_seq;
CREATE TABLE alloc.resource_allocation_change (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('alloc.resource_allocation_change_id_seq'),
    revision_id integer NOT NULL REFERENCES alloc.resource_allocation_revision (id),
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    project_id integer NOT NULL REFERENCES proj.project (id),
    previous_percent smallint NOT NULL CHECK (previous_percent >= 0 AND previous_percent <= 1000),
    new_percent smallint NOT NULL CHECK (new_percent >= 0 AND new_percent <= 1000),
    CONSTRAINT resource_allocation_revision_cell_unique UNIQUE (revision_id, employee_id, project_id)
);

COMMENT ON TABLE alloc.resource_allocation_change IS 'Immutable cell changes included in a resource allocation revision';
COMMENT ON COLUMN alloc.resource_allocation_change.id IS 'Primary key';
COMMENT ON COLUMN alloc.resource_allocation_change.revision_id IS 'Parent revision';
COMMENT ON COLUMN alloc.resource_allocation_change.employee_id IS 'Allocated employee';
COMMENT ON COLUMN alloc.resource_allocation_change.project_id IS 'Project receiving the allocation';
COMMENT ON COLUMN alloc.resource_allocation_change.previous_percent IS 'Percentage before the revision, zero when absent';
COMMENT ON COLUMN alloc.resource_allocation_change.new_percent IS 'Percentage after the revision, zero when removed';

INSERT INTO sec.perm (permission, description) VALUES
    ('resource_allocation_edit', 'View and edit monthly resource allocations'),
    ('resource_allocation_edit_globally', 'Edit resource allocations for every project')
ON CONFLICT DO NOTHING;

INSERT INTO sec.role_perm (role, permission) VALUES
    ('global_admin', 'resource_allocation_edit'),
    ('global_admin', 'resource_allocation_edit_globally'),
    ('pm', 'resource_allocation_edit')
ON CONFLICT DO NOTHING;
