if not exists (select * from sysobjects where name='project_poc' and xtype='U')
begin
    create TABLE project_poc (
	id int IDENTITY(1,1) PRIMARY KEY,
	name nvarchar(255) NULL,
	phone nvarchar(255) NULL,
	email nvarchar(255) NULL,
	description ntext NULL
)
end
GO


if not exists (select * from sysobjects where name='project' and xtype='U')
begin
create TABLE project (
	id int IDENTITY(1,1) PRIMARY KEY,
	name nvarchar(255) NULL,
	start_date datetime NULL,
	end_date datetime NULL,
	customer nvarchar(255) NULL,
	person_of_contact int NULL
)
alter table project add CONSTRAINT project_FK FOREIGN KEY (person_of_contact) REFERENCES project_poc(id);
end
GO


if not exists (select * from sysobjects where name='project_vacancy' and xtype='U')
begin
create TABLE project_vacancy (
	id int IDENTITY(1,1) PRIMARY KEY,
	vacancy_position int NULL,
	project int NULL,
	level int NULL,
	vacancy_priority int NULL,
	employee int NULL,
	next_action nvarchar(255),
	start_date datetime NULL,
	end_date datetime NULL,
	notes nvarchar(255) NULL
)
alter table project_vacancy add CONSTRAINT project_vacancy_employee_FK FOREIGN KEY (employee) REFERENCES employee(id);
alter table project_vacancy add CONSTRAINT project_vacancy_position_FK FOREIGN KEY (vacancy_position) REFERENCES dict_vacancy_position(id);
alter table project_vacancy add CONSTRAINT project_vacancy_project_FK FOREIGN KEY (project) REFERENCES project(id);
alter table project_vacancy add CONSTRAINT project_vacancy_level_FK FOREIGN KEY (level) REFERENCES dict_level(id);
alter table project_vacancy add CONSTRAINT project_vacancy_priority_FK FOREIGN KEY (vacancy_priority) REFERENCES dict_vacancy_priority(id);
end
GO

if not exists (select * from sysobjects where name='current_project_assignee' and xtype='U')
begin
create TABLE current_project_assignee (
	id int IDENTITY(1,1) PRIMARY KEY,
	employee int NOT NULL,
	project int NOT NULL,
	notes nvarchar(1024) NULL
)

alter table current_project_assignee add CONSTRAINT current_project_assignee_employee_FK FOREIGN KEY (employee) REFERENCES employee(id);
alter table current_project_assignee add CONSTRAINT current_project_assignee_project_FK FOREIGN KEY (project) REFERENCES project(id);
end
GO

