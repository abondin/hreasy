CREATE TYPE responsible_object_types AS ENUM
    ('project', 'business_account', 'department');
COMMENT ON TYPE responsible_object_types IS 'Enumeration for empl.responsible#object_type';

CREATE TYPE responsibility_type AS ENUM
    ('technical', 'organization', 'hr');
COMMENT ON TYPE responsible_object_types IS 'Enumeration for empl.responsible#responsibility_type';

CREATE SEQUENCE IF NOT EXISTS empl.responsible_id_seq;
CREATE TABLE IF NOT EXISTS empl.responsible (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('empl.responsible_id_seq'),
    employee integer NOT NULL REFERENCES empl.employee (id),
    object_type responsible_object_types NOT NULL,
    object_id integer NOT NULL,
    responsibility_type responsibility_type NOT NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer NULL REFERENCES empl.employee (id)
);
ALTER TABLE empl.responsible  ADD CONSTRAINT responsible_unique_constraint UNIQUE
    (employee, object_type, object_id);

COMMENT ON TABLE empl.responsible IS 'Internal (not ldap) password to login';
COMMENT ON COLUMN empl.responsible.id IS 'Primary key';
COMMENT ON COLUMN empl.responsible.employee IS 'Link to the employee';
COMMENT ON COLUMN empl.responsible.object_type IS 'Employee can be responsible for project, ba or department';
COMMENT ON COLUMN empl.responsible.object_id IS 'Link to the object of responsibility (project, ba or department)';
COMMENT ON COLUMN empl.responsible.responsibility_type IS 'Employee can be responsible as manager, team lead or HR (technical, organization, hr)';
COMMENT ON COLUMN empl.responsible.created_at IS 'Created at';
COMMENT ON COLUMN empl.responsible.created_by IS 'Created by (link to employee)';

INSERT INTO empl.responsible (employee, object_id, object_type, responsibility_type, created_at)
 SELECT ba.responsible_employee,
  ba.id, 'business_account', 'technical', now()
 FROM ba.business_account ba where ba.archived!=true;

INSERT INTO history.history (entity_type, entity_id, entity_value, created_at)
    select 'empl_responsible', e.id
    , json_build_object(
        'id',e.id
        ,'employee', e.employee
        ,'objectType', e.object_type
        ,'objectId', e.object_id
        ,'responsibilityType', e.responsibility_type
        ,'createdAt', e.created_at
        ,'createdBy', e.created_by
      )
    , now()
    from empl.responsible e;

INSERT INTO sec.perm (permission,description) VALUES
 	 ('admin_responsible_employees','Update responsible for department/ba/project employees');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('global_admin', 'admin_responsible_employees');
INSERT INTO sec.role_perm (role, permission) VALUES
   	 ('hr', 'admin_responsible_employees');
