-- Simple login logging
if not exists (select * from sysobjects where name='sec_login_history' and xtype='U')
begin
    create TABLE sec_login_history (
	id int IDENTITY(1,1) PRIMARY KEY,
	login nvarchar(255),
	logged_employee_id int,
	error nvarchar(4000),
    login_time datetimeoffset NOT NULL
   );
end