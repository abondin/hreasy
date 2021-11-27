-- Create security schema
CREATE SCHEMA IF NOT EXISTS sec;

---------------- Roles
CREATE TABLE IF NOT EXISTS sec.role (
	"role" varchar(255) PRIMARY KEY NOT NULL,
	description varchar(1024) NULL
);
COMMENT ON TABLE sec.role IS 'Role - group of permissions to assign to the employee';
COMMENT ON COLUMN sec.role.role IS 'Name of the role';
COMMENT ON COLUMN sec.role.description IS 'Description';

CREATE TABLE IF NOT EXISTS sec.user_role (
	employee_id integer NOT NULL REFERENCES empl.employee (id),
	"role" varchar(255) NOT NULL REFERENCES sec.role (role),
	primary key (employee_id, role)
);
COMMENT ON TABLE sec.user_role IS 'Roles assigned to employee';
COMMENT ON COLUMN sec.user_role.employee_id IS 'Link to employee';
COMMENT ON COLUMN sec.user_role.role IS 'Link to role';

--- Accessible elements

CREATE TABLE IF NOT EXISTS sec.employee_accessible_departments (
	employee_id integer NOT NULL REFERENCES empl.employee (id),
	department_id integer NOT NULL REFERENCES dict.department (id),
	primary key (employee_id, department_id)
);
COMMENT ON TABLE sec.employee_accessible_departments IS 'Accessible Departments';
COMMENT ON COLUMN sec.employee_accessible_departments.employee_id IS 'Link to employee';
COMMENT ON COLUMN sec.employee_accessible_departments.department_id IS 'Link to department';

CREATE TABLE IF NOT EXISTS sec.employee_accessible_projects (
	employee_id integer NOT NULL REFERENCES empl.employee (id),
	project_id integer NOT NULL REFERENCES proj.project (id),
	primary key (employee_id, project_id)
);
COMMENT ON TABLE sec.employee_accessible_projects IS 'Accessible Projects';
COMMENT ON COLUMN sec.employee_accessible_projects.employee_id IS 'Link to employee';
COMMENT ON COLUMN sec.employee_accessible_projects.project_id IS 'Link to project';

CREATE TABLE IF NOT EXISTS sec.employee_accessible_bas (
	employee_id integer NOT NULL REFERENCES empl.employee (id),
	ba_id integer NOT NULL REFERENCES ba.business_account (id),
	primary key (employee_id, ba_id)
);
COMMENT ON TABLE sec.employee_accessible_bas IS 'Accessible Business Accounts';
COMMENT ON COLUMN sec.employee_accessible_bas.employee_id IS 'Link to employee';
COMMENT ON COLUMN sec.employee_accessible_bas.ba_id IS 'Link to business account';




---------------- Permissions
CREATE TABLE IF NOT EXISTS sec.perm (
	"permission" varchar(255) NOT NULL PRIMARY KEY,
	description varchar(1024) NULL
);
COMMENT ON TABLE sec.perm IS 'Permission to grant some action in the system';
COMMENT ON COLUMN sec.perm.permission IS 'Permission name';
COMMENT ON COLUMN sec.perm.description IS 'Description';

CREATE SEQUENCE IF NOT EXISTS sec.ROLE_PERM_ID_SEQ;
CREATE TABLE IF NOT EXISTS sec.role_perm (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('sec.ROLE_PERM_ID_SEQ'),
	"role" varchar(255) NOT NULL REFERENCES sec.role (role),
	"permission" varchar(255) NOT NULL REFERENCES sec.perm (permission)
);
COMMENT ON TABLE sec.user_role IS 'Permissions assigned to role';
COMMENT ON COLUMN sec.user_role.employee_id IS 'Link to role';
COMMENT ON COLUMN sec.user_role.role IS 'Link to permission';

CREATE SEQUENCE IF NOT EXISTS sec.USER_ROLE_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS sec.user_role_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('sec.USER_ROLE_HISTORY_ID_SEQ'),
	employee_id integer NOT NULL,
	roles varchar(255)[] NULL,
	accessible_projects integer[] NULL,
	accessible_bas integer[] NULL,
	accessible_departments integer[] NULL,
	created_at timestamp with time zone NOT NULL,
	created_by integer
);
COMMENT ON TABLE sec.user_role_history IS 'History of roles to user assignments';
COMMENT ON COLUMN sec.user_role_history.id IS 'Primary key';
COMMENT ON COLUMN sec.user_role_history.employee_id IS 'Link to employee';
COMMENT ON COLUMN sec.user_role_history.roles IS 'Array of granted roles';
COMMENT ON COLUMN sec.user_role_history.accessible_projects IS 'Array of granted Projects';
COMMENT ON COLUMN sec.user_role_history.accessible_bas IS 'Array of granted Business Accounts';
COMMENT ON COLUMN sec.user_role_history.accessible_departments IS 'Array of granted Departments';
COMMENT ON COLUMN sec.user_role_history.created_at IS 'DB record created at';
COMMENT ON COLUMN sec.user_role_history.created_by IS 'DB record created by';

CREATE SEQUENCE IF NOT EXISTS sec.LOGIN_HISTORY_ID_SEQ;
CREATE TABLE IF NOT EXISTS sec.login_history (
	id integer PRIMARY KEY NOT NULL DEFAULT nextval('sec.LOGIN_HISTORY_ID_SEQ'),
	login varchar(255) NULL,
	logged_employee_id integer NULL,
	error varchar(4000) NULL,
	login_time timestamp with time zone NOT NULL
);

COMMENT ON TABLE sec.login_history IS 'History of login attempts';
COMMENT ON COLUMN sec.login_history.id IS 'Primary key';
COMMENT ON COLUMN sec.login_history.login IS 'Array of granted roles';
COMMENT ON COLUMN sec.login_history.logged_employee_id IS 'Link to employee';
COMMENT ON COLUMN sec.login_history.error IS 'Attempt error';
COMMENT ON COLUMN sec.login_history.login_time IS 'Time of attempt';


INSERT INTO sec.perm (permission,description) VALUES
	 ('admin_users','Admin user. Assign roles. Assign accessible projects and departments'),
	 ('assign_to_ba_position','Assign/Unassign employee to/from business account position'),
	 ('create_assessment','Schedule new assessment and invite managers'),
	 ('create_project','Admin permission to create information of any project'),
	 ('edit_articles','Create/update and moderate articles and news'),
	 ('edit_business_account','Add/Update business account and create/update BA positions'),
	 ('edit_employee_full','Create/update employee'),
	 ('edit_skills','Add/Delete employee skills of managed project/department'),
	 ('overtime_admin','Admin overtime configuration. Close overtime period and other stuff'),
	 ('overtime_edit','Edit and approve overtimes of given employee'),
	 ('overtime_view','View overtimes of given employee'),
	 ('project_admin_area','Access to project admin area in UI'),
	 ('update_avatar','Update employee avatar'),
	 ('update_current_project','Update current project for employee from my projects or my departments'),
	 ('update_current_project_global','Change current employee project'),
	 ('update_project','Admin permission to update information of any project'),
	 ('vacation_edit','Edit vacations of given employee'),
	 ('vacation_view','View vacations of given employee'),
	 ('view_assessment_full','View all assessment forms without restrictions'),
	 ('view_employee_full','View employee all fields including personal');