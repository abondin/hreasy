if not exists (select * from sysobjects where name='vacation' and xtype='U')
begin
create TABLE vacation (
	id int IDENTITY(1,1) PRIMARY KEY,
	employee int NULL,
	year int NOT NULL,
	start_date datetime NULL,
	end_date datetime NULL,
	notes nvarchar(255) NULL
)
alter table vacation add CONSTRAINT vacation_employee FOREIGN KEY (employee) REFERENCES employee(id);
end
GO