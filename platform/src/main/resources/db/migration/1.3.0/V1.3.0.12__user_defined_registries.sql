CREATE schema if not exists udr;
CREATE SEQUENCE IF NOT EXISTS udr.REGISTRY_ID_SEQ;
CREATE TABLE IF NOT EXISTS udr.registry (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('udr.REGISTRY_ID_SEQ'),
	"name" varchar(255) not NULL,
	owner_id integer NOT NULL REFERENCES empl.employee (id),
	description text null,
	employeeFields varchar(255)[] not null default ARRAY['displayName', 'currentProject.name'],
	customFields jsonb not null default '[]'::jsonb,
	created_at timestamp with time zone not null,
	created_by integer NULL REFERENCES empl.employee (id),
	deleted_at timestamp with time zone null,
   	deleted_by integer REFERENCES empl.employee (id)
);

CREATE SEQUENCE IF NOT EXISTS udr.REGISTRY_EMPLOYEE_ID_SEQ;
CREATE TABLE IF NOT EXISTS udr.registry_employee (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('udr.REGISTRY_ID_SEQ'),
	employee_id integer NOT NULL REFERENCES empl.employee (id),
    active boolean not null default true,
    customFieldsValues jsonb not null default '[]'::jsonb
);

CREATE SEQUENCE IF NOT EXISTS udr.REGISTRY_ACCESS_ID_SEQ;
CREATE TABLE IF NOT EXISTS udr.registry_access (
    id integer PRIMARY KEY NOT NULL DEFAULT nextval('udr.REGISTRY_ACCESS_ID_SEQ'),
    registry_id integer NOT NULL REFERENCES udr.registry (id),
	employee_id integer NOT NULL REFERENCES empl.employee (id),
    read_permission boolean NOT NULL DEFAULT false,
    write_permission boolean NOT NULL DEFAULT false,
    UNIQUE (registry_id, employee_id)
);

COMMENT ON TABLE udr.registry IS 'User-Defined Registry: Custom employee registers';
COMMENT ON COLUMN udr.registry.id IS 'Unique identifier of the registry';
COMMENT ON COLUMN udr.registry.name IS 'Name of the registry';
COMMENT ON COLUMN udr.registry.owner_id IS 'ID of the registry owner';
COMMENT ON COLUMN udr.registry.description IS 'Description of the registry';
COMMENT ON COLUMN udr.registry.employeeFields IS 'List of standard employee fields in the registry';
COMMENT ON COLUMN udr.registry.customFields IS 'List of custom fields in the registry';
COMMENT ON COLUMN udr.registry.created_at IS 'Timestamp when the registry was created';
COMMENT ON COLUMN udr.registry.created_by IS 'ID of the employee who created the registry';
COMMENT ON COLUMN udr.registry.deleted_at IS 'Timestamp when the registry was deleted';
COMMENT ON COLUMN udr.registry.deleted_by IS 'ID of the employee who deleted the registry';

COMMENT ON TABLE udr.registry_employee IS 'User-Defined Registry Employees: Mapping of employees to custom registries';
COMMENT ON COLUMN udr.registry_employee.id IS 'Unique identifier of the registry employee entry';
COMMENT ON COLUMN udr.registry_employee.employee_id IS 'ID of the employee mapped to the registry';
COMMENT ON COLUMN udr.registry_employee.active IS 'Flag indicating if the employee is active in the registry';
COMMENT ON COLUMN udr.registry_employee.customFieldsValues IS 'Custom field values specific to the employee in the registry';

COMMENT ON TABLE udr.registry_access IS 'User-Defined Registry Access: Access control for user registries';
COMMENT ON COLUMN udr.registry_access.id IS 'Unique identifier of the registry access entry';
COMMENT ON COLUMN udr.registry_access.registry_id IS 'ID of the registry associated with the access';
COMMENT ON COLUMN udr.registry_access.employee_id IS 'ID of the user granted access to the registry';
COMMENT ON COLUMN udr.registry_access.read_permission IS 'Flag indicating if the user has read permission';
COMMENT ON COLUMN udr.registry_access.write_permission IS 'Flag indicating if the user has write permission';

-- Permission
INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_udr','View all and create/update/delete User-Defined Registries'),
	 ('create_udr','Create User-Defined Registries')
	 on conflict do nothing;
INSERT INTO sec.role_perm (role,permission) VALUES
	 ('global_admin','admin_udr'),
	 ('global_admin','create_udr'),
	 ('pm','create_udr'),
	 ('hr','create_udr')
	  on conflict do nothing;

COMMENT ON COLUMN history.history.entity_type IS '
  [empl_manager] - Entity type,
  [working_days] - Working Days Calendar,
  [timesheet_record] - Timesheet Record
  [support_request_group] - Support Request Group
  [support_request] - Support Request
  [udr] - User-Defined Registry (udr.registry)
  [udr_employee] - employees in User-Defined Registry (udr.registry_employee)
  [udr_access] - Access to User-Defined Registry (udr.registry_access)
  ';