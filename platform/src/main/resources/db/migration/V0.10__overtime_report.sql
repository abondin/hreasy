if not exists (select * from sysobjects where name='overtime_report' and xtype='U')
begin
create TABLE overtime_report (
	id int IDENTITY(1,1) PRIMARY KEY,
	employee_id int NOT NULL,
	period int NOT NULL,
)

ALTER TABLE overtime_report ADD CONSTRAINT overtime_report_employee_FK FOREIGN KEY (employee_id) REFERENCES employee(id);
ALTER TABLE overtime_report ADD CONSTRAINT overtime_report_UN UNIQUE (employee_id, period);

create TABLE overtime_item (
	id int IDENTITY(1,1) PRIMARY KEY,
	report_id int NOT NULL,
	date date NOT NULL,
	project_id int NOT NULL,
	hours int NOT NULL,
	notes nvarchar(1024) NULL,
    created_at datetimeoffset NOT NULL,
    created_employee_id int NOT NULL,
    deleted_at datetimeoffset NULL,
    deleted_employee_id int NULL,
)

ALTER TABLE overtime_item ADD CONSTRAINT overtime_item_report_FK FOREIGN KEY (report_id) REFERENCES overtime_report(id);
ALTER TABLE overtime_item ADD CONSTRAINT overtime_item_created_FK FOREIGN KEY (created_employee_id) REFERENCES employee(id);
ALTER TABLE overtime_item ADD CONSTRAINT overtime_item_deleted_FK FOREIGN KEY (deleted_employee_id) REFERENCES employee(id);

end
GO

