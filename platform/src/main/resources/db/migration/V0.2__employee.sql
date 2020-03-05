if not exists (select * from sysobjects where name='employee' and xtype='U')
begin
CREATE TABLE employee (
	id int IDENTITY(1,1) PRIMARY KEY,
	lastname nvarchar(255) NULL,
	firstname nvarchar(255) NULL,
	patronymic_name nvarchar(255) NULL,
	birthday datetime NULL,
	sex nvarchar(255) NULL,
	date_of_employment datetime NULL,
	department int NULL,
	[position] int NULL,
	[level] int NULL,
	work_type nvarchar(255) NULL,
	work_day nvarchar(255) NULL,
	email nvarchar(255) NULL,
	phone float NULL,
	skype nvarchar(255) NULL,
	registration_address nvarchar(255) NULL,
	document_series nvarchar(255) NULL,
	document_number nvarchar(255) NULL,
	document_issued_by nvarchar(255) NULL,
	document_issued_date datetime NULL,
	foreign_passport nvarchar(255) NULL,
	city_of_residence nvarchar(255) NULL,
	english_level nvarchar(255) NULL,
	family_status nvarchar(255) NULL,
	spouse_name nvarchar(255) NULL,
	children nvarchar(255) NULL,
	date_of_dismissal datetime NULL
)

ALTER TABLE employee ADD CONSTRAINT employee_dep_FK FOREIGN KEY (department) REFERENCES department(id)
ALTER TABLE employee ADD CONSTRAINT employee_level_FK FOREIGN KEY ([level]) REFERENCES dict_level(id)
ALTER TABLE employee ADD CONSTRAINT employee_position_FK FOREIGN KEY ([position]) REFERENCES dict_position(id)
end
GO

IF OBJECT_ID('v_employee_detailed', 'V') IS NOT NULL
drop view v_employee_detailed;
GO

create view v_employee_detailed as
	select e.*,
	d.name as department_name,
	l.name as level_name,
	p.name as position_name
	from employee e
	left join department d on e.department = d.id
	left join dict_level l on e.level = l.id
	left join dict_position p on e.position = p.id;
