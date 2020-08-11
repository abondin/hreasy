IF NOT EXISTS (
  select *
  from   sys.columns
  where  object_id = OBJECT_ID(N'[dbo].[employee]')
         and name = 'ext_erp_id'
)
begin

-- Add employee id in external ERP system to correlate data in two systems
ALTER TABLE employee ADD ext_erp_id int

-- Add role and permissions table to normalize database structure
CREATE TABLE sec_role (
    role  nvarchar(255) NOT NULL,
    description nvarchar(1024) NULL,
    primary key (role)
)
INSERT INTO sec_role (role) select distinct role from sec_role_perm;

ALTER TABLE sec_user_role ADD CONSTRAINT sec_user_role_role_FK FOREIGN KEY (role) REFERENCES sec_role(role)
ALTER TABLE sec_role_perm ADD CONSTRAINT sec_role_perm_role_FK FOREIGN KEY (role) REFERENCES sec_role(role)

CREATE TABLE sec_perm (
    permission  nvarchar(255) NOT NULL,
    description nvarchar(1024) NULL,
    primary key (permission)
)
INSERT INTO sec_perm (permission) select distinct permission from sec_role_perm;

ALTER TABLE sec_role_perm ADD CONSTRAINT sec_role_perm_perm_FK FOREIGN KEY (permission) REFERENCES sec_perm(permission)


  -- User assesible departments
CREATE TABLE employee_accessible_departments (
	employee_id int NOT NULL,
	department_id int NOT NULL,
	primary key (employee_id, department_id)
)

  -- User accessible projects. Means nothing if employee has access to the whole department
CREATE TABLE employee_accessible_projects (
	employee_id int NOT NULL,
	project_id int NOT NULL,
	primary key (employee_id, project_id)
)

ALTER TABLE employee_accessible_departments ADD CONSTRAINT employee_accessible_departments_empl_FK FOREIGN KEY (employee_id) REFERENCES employee(id)
ALTER TABLE employee_accessible_departments ADD CONSTRAINT employee_accessible_departments_dep_FK FOREIGN KEY (department_id) REFERENCES department(id)


ALTER TABLE employee_accessible_projects ADD CONSTRAINT employee_accessible_projects_empl_FK FOREIGN KEY (employee_id) REFERENCES employee(id)
ALTER TABLE employee_accessible_projects ADD CONSTRAINT employee_accessible_projects_project_FK FOREIGN KEY (project_id) REFERENCES project(id)


-- Add not null constraint in next migration. Do not forget to fill it manually
ALTER TABLE project ADD department_id int null;

ALTER TABLE project ADD CONSTRAINT project_department_FK FOREIGN KEY (department_id) REFERENCES department(id);

-- Assign HR and Admin to the all departments
insert into employee_accessible_departments select ee.id, d.id from department d, (select e.id, e.email from employee e
	join sec_user u on u.email = e.email
	where u.id in (select user_id from sec_user_role where role='global_admin' or role='hr')) ee;
end
GO
