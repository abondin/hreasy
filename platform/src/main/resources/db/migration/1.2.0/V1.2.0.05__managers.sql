-- 08/18/2022 Unfortunately there is no easy to go way for enum mapping in R2DBC without additional codecs
--CREATE TYPE responsible_object_types AS ENUM
--    ('project', 'business_account', 'department');
--CREATE TYPE responsibility_type AS ENUM
--    ('technical', 'organization', 'hr');

CREATE SEQUENCE IF NOT EXISTS empl.manager_id_seq;
CREATE TABLE IF NOT EXISTS empl.manager (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.manager_id_seq'),
    employee integer NOT NULL REFERENCES empl.employee (id),
    object_type varchar(64) NOT NULL,
    object_id integer NOT NULL,
    responsibility_type varchar(64) NOT NULL,
    comment VARCHAR(256) NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NULL REFERENCES empl.employee (id)
);
ALTER TABLE empl.manager  ADD CONSTRAINT manager_unique_constraint UNIQUE
    (employee, object_type, object_id);

COMMENT ON TABLE empl.manager IS 'Manager of the department, business account or project';
COMMENT ON COLUMN empl.manager.id IS 'Primary key';
COMMENT ON COLUMN empl.manager.employee IS 'Link to the employee';
COMMENT ON COLUMN empl.manager.object_type IS '[project, business_account, department]: Employee can be responsible for project, ba or department';
COMMENT ON COLUMN empl.manager.object_id IS 'Link to the object of responsibility (project, ba or department)';
COMMENT ON COLUMN empl.manager.responsibility_type IS '[technical, organization, hr]: Employee can be responsible as manager, team lead or HR';
COMMENT ON COLUMN empl.manager.comment IS 'Comment in free form';
COMMENT ON COLUMN empl.manager.created_at IS 'Created at';
COMMENT ON COLUMN empl.manager.created_by IS 'Created by (link to employee)';

INSERT INTO empl.manager (employee, object_id, object_type, responsibility_type, created_at)
 SELECT ba.responsible_employee,
  ba.id, 'business_account', 'organization', now()
 FROM ba.business_account ba where ba.archived!=true;

INSERT INTO history.history (entity_type, entity_id, entity_value, created_at)
    select 'empl_manager', e.id
    , json_build_object(
        'id',e.id
        ,'employee', e.employee
        ,'objectType', e.object_type
        ,'objectId', e.object_id
        ,'responsibilityType', e.responsibility_type
        ,'comment', e.comment
        ,'createdAt', e.created_at
        ,'createdBy', e.created_by
      )
    , now()
    from empl.manager e;

INSERT INTO sec.perm (permission,description) VALUES
 	 ('admin_managers','Admin managers for department,ba,project employees');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('global_admin', 'admin_managers');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('hr', 'admin_managers');
