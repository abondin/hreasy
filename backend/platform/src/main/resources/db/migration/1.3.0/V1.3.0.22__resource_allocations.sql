CREATE SCHEMA IF NOT EXISTS alloc;

CREATE SEQUENCE IF NOT EXISTS alloc.resource_allocation_revision_id_seq;
CREATE TABLE alloc.resource_allocation_revision (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('alloc.resource_allocation_revision_id_seq'),
    year integer NOT NULL,
    project_id integer NOT NULL REFERENCES proj.project (id),
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id)
);

COMMENT ON TABLE alloc.resource_allocation_revision IS 'One batch of resource allocation changes';
COMMENT ON COLUMN alloc.resource_allocation_revision.id IS 'Primary key';
COMMENT ON COLUMN alloc.resource_allocation_revision.year IS 'Calendar year edited by this revision';
COMMENT ON COLUMN alloc.resource_allocation_revision.project_id IS 'Project edited by this revision';
COMMENT ON COLUMN alloc.resource_allocation_revision.created_at IS 'Revision creation time';
COMMENT ON COLUMN alloc.resource_allocation_revision.created_by IS 'Employee who created the revision';

CREATE SEQUENCE IF NOT EXISTS alloc.resource_allocation_id_seq;
CREATE TABLE alloc.resource_allocation (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('alloc.resource_allocation_id_seq'),
    year integer NOT NULL,
    period integer NOT NULL,
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    project_id integer NOT NULL REFERENCES proj.project (id),
    percent smallint NOT NULL CHECK (percent > 0 AND percent <= 1000),
    revision_id integer NOT NULL REFERENCES alloc.resource_allocation_revision (id),
    CONSTRAINT resource_allocation_cell_unique UNIQUE (year, period, employee_id, project_id),
    CONSTRAINT resource_allocation_period_year_check CHECK (year = period / 100)
);

COMMENT ON TABLE alloc.resource_allocation IS 'Current monthly allocation of an employee to a project';
COMMENT ON COLUMN alloc.resource_allocation.id IS 'Primary key';
COMMENT ON COLUMN alloc.resource_allocation.year IS 'Calendar year stored explicitly for annual allocation queries';
COMMENT ON COLUMN alloc.resource_allocation.period IS 'Allocation period in YYYYMM format with zero-based month; 202005 means June 2020';
COMMENT ON COLUMN alloc.resource_allocation.employee_id IS 'Allocated employee';
COMMENT ON COLUMN alloc.resource_allocation.project_id IS 'Project receiving the allocation';
COMMENT ON COLUMN alloc.resource_allocation.percent IS 'Allocation percentage from 1 through 1000';
COMMENT ON COLUMN alloc.resource_allocation.revision_id IS 'Revision that last changed this cell';

CREATE INDEX resource_allocation_period_employee_idx
    ON alloc.resource_allocation (year, period, employee_id);
CREATE INDEX resource_allocation_project_year_idx
    ON alloc.resource_allocation (project_id, year, employee_id);

CREATE SEQUENCE IF NOT EXISTS alloc.resource_allocation_change_id_seq;
CREATE TABLE alloc.resource_allocation_change (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('alloc.resource_allocation_change_id_seq'),
    revision_id integer NOT NULL REFERENCES alloc.resource_allocation_revision (id),
    period integer NOT NULL,
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    previous_percent smallint NOT NULL CHECK (previous_percent >= 0 AND previous_percent <= 1000),
    new_percent smallint NOT NULL CHECK (new_percent >= 0 AND new_percent <= 1000),
    CONSTRAINT resource_allocation_revision_cell_unique UNIQUE (revision_id, period, employee_id)
);

COMMENT ON TABLE alloc.resource_allocation_change IS 'Immutable cell changes included in a resource allocation revision';
COMMENT ON COLUMN alloc.resource_allocation_change.id IS 'Primary key';
COMMENT ON COLUMN alloc.resource_allocation_change.revision_id IS 'Parent revision';
COMMENT ON COLUMN alloc.resource_allocation_change.period IS 'Changed period in zero-based YYYYMM format';
COMMENT ON COLUMN alloc.resource_allocation_change.employee_id IS 'Allocated employee';
COMMENT ON COLUMN alloc.resource_allocation_change.previous_percent IS 'Percentage before the revision, zero when absent';
COMMENT ON COLUMN alloc.resource_allocation_change.new_percent IS 'Percentage after the revision, zero when removed';

CREATE TABLE alloc.resource_allocation_closed_period (
    period integer PRIMARY KEY NOT NULL,
    year integer NOT NULL,
    closed_at timestamp with time zone NOT NULL,
    closed_by integer NOT NULL REFERENCES empl.employee (id),
    comment text NULL,
    CONSTRAINT resource_allocation_closed_period_year_check CHECK (year = period / 100)
);

COMMENT ON TABLE alloc.resource_allocation_closed_period IS 'Allocation periods closed for editing';
COMMENT ON COLUMN alloc.resource_allocation_closed_period.period IS 'Closed period in zero-based YYYYMM format';
COMMENT ON COLUMN alloc.resource_allocation_closed_period.year IS 'Calendar year stored explicitly for annual queries';
COMMENT ON COLUMN alloc.resource_allocation_closed_period.closed_at IS 'Time when the period was closed';
COMMENT ON COLUMN alloc.resource_allocation_closed_period.closed_by IS 'Employee who closed the period';
COMMENT ON COLUMN alloc.resource_allocation_closed_period.comment IS 'Optional close reason';

CREATE INDEX resource_allocation_closed_period_year_idx
    ON alloc.resource_allocation_closed_period (year, period);

INSERT INTO sec.perm (permission, description) VALUES
    ('resource_allocation_read', 'View monthly resource allocations'),
    ('resource_allocation_write', 'Edit monthly resource allocations'),
    ('resource_allocation_admin', 'Close and reopen resource allocation periods')
ON CONFLICT DO NOTHING;

INSERT INTO sec.role_perm (role, permission) VALUES
    ('pm', 'resource_allocation_read'),
    ('pm', 'resource_allocation_write'),
    ('finance', 'resource_allocation_read'),
    ('finance', 'resource_allocation_write'),
    ('pm_finance', 'resource_allocation_read'),
    ('pm_finance', 'resource_allocation_write'),
    ('salary_manager', 'resource_allocation_read'),
    ('salary_manager', 'resource_allocation_write'),
    ('global_admin', 'resource_allocation_read'),
    ('global_admin', 'resource_allocation_write'),
    ('global_admin', 'resource_allocation_admin')
ON CONFLICT DO NOTHING;
