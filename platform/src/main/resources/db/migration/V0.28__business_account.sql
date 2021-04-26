if not exists (select * from sysobjects where name='business_account' and xtype='U')
begin
    create TABLE business_account (
	id int IDENTITY(1,1) PRIMARY KEY,
	name nvarchar(255) NOT NULL,
	responsible_employee int NULL,
	description ntext NULL,
	archived_at datetimeoffset NULL,
	archived_by int NULL,
	created_at datetimeoffset NULL,
	created_by int NULL
    )
    alter table business_account add CONSTRAINT ba_responsible_FK FOREIGN KEY (responsible_employee) REFERENCES employee(id);
    alter table business_account add CONSTRAINT ba_archived_FK FOREIGN KEY (archived_by) REFERENCES employee(id);
    alter table business_account add CONSTRAINT ba_created_FK FOREIGN KEY (created_by) REFERENCES employee(id);
end
GO

if not exists (select * from sysobjects where name='ba_position' and xtype='U')
begin
    create TABLE ba_position (
	id int IDENTITY(1,1) PRIMARY KEY,
	business_account int NOT NULL,
	name nvarchar(255) NOT NULL,
	rate DECIMAL(19,4) NOT NULL,
	description ntext NULL,
	archived_at datetimeoffset NULL,
	archived_by int NULL,
	created_at datetimeoffset NULL,
	created_by int NULL
    )
    alter table ba_position add CONSTRAINT bapos_ba_FK FOREIGN KEY (business_account) REFERENCES business_account(id);
    alter table ba_position add CONSTRAINT bapos_archived_FK FOREIGN KEY (archived_by) REFERENCES employee(id);
    alter table ba_position add CONSTRAINT bapos_created_FK FOREIGN KEY (created_by) REFERENCES employee(id);
end
GO

if not exists (select * from sysobjects where name='ba_assignment' and xtype='U')
begin
    create TABLE ba_assignment (
	id int IDENTITY(1,1) PRIMARY KEY,
	business_account int NOT NULL,
	employee int NULL,
	project int NULL,
	parent_assignment int NULL,
	ba_position int NULL,
	employment_rate real NULL,
	ba_position_rate real NULL,
	comment ntext NULL,
	start_date datetime NULL,
	end_date datetime NULL,
	planned_start_date datetime NULL,
	planned_end_date datetime NULL,
	archived_at datetimeoffset NULL,
	archived_by int NULL,
	closed_at datetimeoffset NULL,
	closed_by int NULL,
	closed_reason nvarchar(255) NULL,
	closed_comment ntext NULL,
	created_at datetimeoffset NULL,
	created_by int NULL
    )
    alter table ba_assignment add CONSTRAINT ba_assignment_ba_FK FOREIGN KEY (business_account) REFERENCES business_account(id);
    alter table ba_assignment add CONSTRAINT ba_assignment_ba_position_FK FOREIGN KEY (ba_position) REFERENCES ba_position(id);
    alter table ba_assignment add CONSTRAINT ba_assignment_project_FK FOREIGN KEY (project) REFERENCES project(id);
    alter table ba_assignment add CONSTRAINT ba_assignment_parent_assignment_FK FOREIGN KEY (parent_assignment) REFERENCES ba_assignment(id);
    alter table ba_assignment add CONSTRAINT ba_assignment_employee_FK FOREIGN KEY (employee) REFERENCES employee(id);
    alter table ba_assignment add CONSTRAINT ba_assignment_archived_FK FOREIGN KEY (archived_by) REFERENCES employee(id);
    alter table ba_assignment add CONSTRAINT ba_assignment_closed_FK FOREIGN KEY (closed_by) REFERENCES employee(id);
    alter table ba_assignment add CONSTRAINT ba_assignment_created_FK FOREIGN KEY (created_by) REFERENCES employee(id);
end
GO

