CREATE SEQUENCE IF NOT EXISTS alloc.resource_allocation_comment_id_seq;

CREATE TABLE alloc.resource_allocation_comment (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('alloc.resource_allocation_comment_id_seq'),
    year integer NOT NULL,
    period integer NOT NULL,
    employee_id integer NOT NULL REFERENCES empl.employee (id),
    project_id integer NOT NULL REFERENCES proj.project (id),
    workstream_id integer NULL REFERENCES proj.project_workstream (id),
    comment text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_by integer NOT NULL REFERENCES empl.employee (id),
    updated_at timestamp with time zone NULL,
    CONSTRAINT resource_allocation_comment_period_check
        CHECK (year = period / 100 AND period % 100 BETWEEN 0 AND 11),
    CONSTRAINT resource_allocation_comment_text_check
        CHECK (char_length(btrim(comment)) BETWEEN 1 AND 4000)
);

COMMENT ON TABLE alloc.resource_allocation_comment IS 'Discussion attached to a monthly resource allocation cell';
COMMENT ON COLUMN alloc.resource_allocation_comment.id IS 'Primary key';
COMMENT ON COLUMN alloc.resource_allocation_comment.year IS 'Calendar year stored explicitly for annual queries';
COMMENT ON COLUMN alloc.resource_allocation_comment.period IS 'Commented period in zero-based YYYYMM format';
COMMENT ON COLUMN alloc.resource_allocation_comment.employee_id IS 'Employee represented by the commented cell';
COMMENT ON COLUMN alloc.resource_allocation_comment.project_id IS 'Project represented by the commented cell';
COMMENT ON COLUMN alloc.resource_allocation_comment.workstream_id IS 'Optional workstream represented by the commented cell';
COMMENT ON COLUMN alloc.resource_allocation_comment.comment IS 'Plain-text comment body';
COMMENT ON COLUMN alloc.resource_allocation_comment.created_at IS 'Comment creation time';
COMMENT ON COLUMN alloc.resource_allocation_comment.created_by IS 'Employee who created the comment';
COMMENT ON COLUMN alloc.resource_allocation_comment.updated_at IS 'Last comment edit time';

CREATE INDEX resource_allocation_comment_year_cell_idx
    ON alloc.resource_allocation_comment
       (year, employee_id, project_id, workstream_id, period, created_at, id);
