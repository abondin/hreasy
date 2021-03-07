if not exists (select * from sysobjects where name='sec_user_role_history' and xtype='U')
begin
    create TABLE sec_user_role_history (
	id int IDENTITY(1,1) PRIMARY KEY,
	employee_id int NOT NULL,
	user_id int NULL,
	roles nvarchar(4000) NULL,
	accessible_projects nvarchar(4000) NULL,
	accessible_departments nvarchar(4000) NULL,
    start_date datetime NULL,
    updated_by int NOT NULL,
    updated_at datetimeoffset NOT NULL
   );

    alter table sec_user_role_history add CONSTRAINT sec_user_role_history_empl_FK
    FOREIGN KEY (employee_id) REFERENCES employee(id);
    alter table sec_user_role_history add CONSTRAINT sec_user_role_history_user_FK
    FOREIGN KEY (user_id) REFERENCES sec_user(id);
end