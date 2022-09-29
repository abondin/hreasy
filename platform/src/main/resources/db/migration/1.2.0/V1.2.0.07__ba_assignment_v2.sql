-- Full refactoring of ba.ba_assignment and ba.ba_position tables
-- v1 tables never works. Both empty on real installations. Can be dropped
DROP TABLE ba.ba_assignment_history;
DROP TABLE ba.ba_assignment;
DROP TABLE ba.ba_position_history;
DROP TABLE ba.ba_position;

--- Assignment to business account
CREATE SEQUENCE IF NOT EXISTS ba.BA_ASSIGNMENT_ID_SEQ;
CREATE TABLE IF NOT EXISTS ba.ba_assignment (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('ba.BA_ASSIGNMENT_ID_SEQ'),
	period integer NOT NULL,
	ancestor_assignment integer NULL REFERENCES ba.ba_assignment (id),
	business_account integer NOT NULL REFERENCES ba.business_account (id),
	employee integer NULL REFERENCES empl.employee (id),
	project integer NULL REFERENCES proj.project (id),
	ba_position varchar(256) NULL,
	employment_rate NUMERIC(18,2) NULL,
	employment_rate_factor NUMERIC(3,2) not null default 1.0 CHECK (employment_rate_factor > 0.0 and employment_rate_factor <=1.0),
	ba_position_rate NUMERIC(18,2) NULL,
	ba_position_rate_factor NUMERIC(3,2) not null default 1.0 CHECK (ba_position_rate_factor > 0.0 and ba_position_rate_factor <=1.0),
	"comment" text NULL,
	start_date date NULL,
	end_date date NULL,
	closed_at timestamp with time zone NULL,
	closed_by integer NULL REFERENCES empl.employee (id),
	closed_reason text NULL,
	closed_comment text NULL,
	created_at timestamp with time zone NULL,
	created_by integer NULL REFERENCES empl.employee (id)
);
COMMENT ON TABLE ba.ba_assignment IS 'BA assignment. Links project position and employee';
COMMENT ON COLUMN ba.ba_assignment.id IS 'Primary key to link with other tables';
COMMENT ON COLUMN ba.ba_assignment.period IS 'Assignment period in YYYY format';
COMMENT ON COLUMN ba.ba_assignment.ancestor_assignment IS 'Assignment item is immutable. It is important to show historical perspective. To change assignment you need to close one, and open new with link to ancestor';
COMMENT ON COLUMN ba.ba_assignment.business_account IS 'Link to BA';
COMMENT ON COLUMN ba.ba_assignment.employee IS 'Link to employee';
COMMENT ON COLUMN ba.ba_assignment.project IS 'Link to project';
COMMENT ON COLUMN ba.ba_assignment.ba_position IS 'Position that should be ';
COMMENT ON COLUMN ba.ba_assignment.comment IS 'Comment in free format';
COMMENT ON COLUMN ba.ba_assignment.start_date IS 'Actual assignment start';
COMMENT ON COLUMN ba.ba_assignment.end_date IS 'Actual assignment end';
COMMENT ON COLUMN ba.ba_assignment.employment_rate IS 'Rate of employee including all administrative costs';
COMMENT ON COLUMN ba.ba_assignment.employment_rate_factor IS 'Rate employent activity on given position (1 - full employment, < 1 part-time employment)';
COMMENT ON COLUMN ba.ba_assignment.ba_position_rate IS 'Rate of position';
COMMENT ON COLUMN ba.ba_assignment.ba_position_rate_factor IS 'How many working hours position needs (1 - full employment, < 1 part-time employment)';
COMMENT ON COLUMN ba.ba_assignment.created_at IS 'Created at';
COMMENT ON COLUMN ba.ba_assignment.created_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ba.ba_assignment.closed_at IS 'Created at';
COMMENT ON COLUMN ba.ba_assignment.closed_by IS 'Created by (link to employee)';
COMMENT ON COLUMN ba.ba_assignment.closed_reason IS 'Reason of assignment close';
COMMENT ON COLUMN ba.ba_assignment.closed_comment IS 'Comment on close';

COMMENT ON COLUMN history.history.entity_type IS '[empl_manager] - Entity type, [ba_assignment] - BA assignment. Links project position and employee';

DELETE FROM sec.role_perm
	WHERE "permission"='assign_to_ba_position';

DELETE FROM sec.perm
	WHERE "permission"='assign_to_ba_position';
INSERT INTO sec.perm ("permission",description)
	VALUES ('admin_ba_assignment','Admin business account assignments for ba managers');

INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('global_admin', 'admin_ba_assignment');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('finance', 'admin_ba_assignment');

create or replace view ba.v_ba_assignment as
    select a.*
        , ba.name as business_account_name
        , trim(concat_ws(' ', e.lastname, e.firstname, e.patronymic_name)) as employee_display_name
        , p.name as project_name
        , trim(concat_ws(' ', cl.lastname, cl.firstname, cl.patronymic_name)) as closed_by_display_name
        from
        ba.ba_assignment a
        left join ba.business_account ba on a.business_account = ba.id
        left join empl.employee e on a.employee = e.id
        left join proj.project p on a.project = p.id
        left join empl.employee cl on a.closed_by = cl.id;