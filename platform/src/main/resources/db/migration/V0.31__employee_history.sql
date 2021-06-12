if not exists (select * from sysobjects where name='employee_history' and xtype='U')
begin
CREATE TABLE hr.dbo.employee_history (
	id int IDENTITY(1,1) NOT NULL,
	employee int IDENTITY(1,1) NOT NULL,
	lastname nvarchar(255)  NULL,
	firstname nvarchar(255)  NULL,
	patronymic_name nvarchar(255)  NULL,
	birthday datetime NULL,
	sex nvarchar(255)  NULL,
	date_of_employment datetime NULL,
	department int NULL,
	[position] int NULL,
	[level] int NULL,
	work_type nvarchar(255)  NULL,
	work_day nvarchar(255)  NULL,
	email nvarchar(255)  NULL,
	phone float NULL,
	skype nvarchar(255)  NULL,
	registration_address nvarchar(255)  NULL,
	document_series nvarchar(255)  NULL,
	document_number nvarchar(255)  NULL,
	document_issued_by nvarchar(255)  NULL,
	document_issued_date datetime NULL,
	foreign_passport nvarchar(255)  NULL,
	city_of_residence nvarchar(255)  NULL,
	english_level nvarchar(255)  NULL,
	family_status nvarchar(255)  NULL,
	spouse_name nvarchar(255)  NULL,
	children nvarchar(255)  NULL,
	date_of_dismissal datetime NULL,
	current_project int NULL,
	office_location int NULL,
	ext_erp_id nvarchar(255)  NULL,
	CONSTRAINT PK__employee__history PRIMARY KEY (id)
)
end
GO;


-- hr.dbo.employee foreign keys

ALTER TABLE hr.dbo.employee_history ADD CONSTRAINT employee_history_FK FOREIGN KEY (employee) REFERENCES hr.dbo.employee(id) GO

if not exists (select * from sec_perm where permission='edit_employee_full')
    begin
        insert into sec_perm(permission,description) values ('edit_employee_full','Create/update employee');
        insert into sec_perm(permission,description) values ('view_employee_full','View employee all fields including personal');
        insert into sec_role_perm(role,permission) values ('global_admin','edit_employee_full');
        insert into sec_role_perm(role,permission) values ('global_admin','edit_employee_full');
        insert into sec_role_perm(role,permission) values ('hr','view_employee_full');
        insert into sec_role_perm(role,permission) values ('hr','view_employee_full');
end